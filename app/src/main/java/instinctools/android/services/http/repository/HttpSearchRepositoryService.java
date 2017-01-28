package instinctools.android.services.http.repository;

import android.content.Intent;

import java.util.ArrayList;

import instinctools.android.activity.SearchActivity;
import instinctools.android.constans.Constants;
import instinctools.android.models.github.repositories.Repository;
import instinctools.android.models.github.search.SearchRequest;
import instinctools.android.models.github.search.SearchResponse;
import instinctools.android.services.github.search.GithubServiceSearch;
import instinctools.android.services.http.repository.HttpRepositoryService;

public class HttpSearchRepositoryService extends HttpRepositoryService {
    public HttpSearchRepositoryService() {
        this.mTypeInfo = Constants.REPOSITORY_TYPE_SEARCH;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SearchRequest request = intent.getParcelableExtra(SearchActivity.INTENT_SEARCH_REQUEST);
        if (request.getIn().isEmpty()) {
            onHandleIntent(new ArrayList<Repository>());
            return;
        }

        SearchResponse searchResponse = GithubServiceSearch.getSearchRepository(request);
        if (searchResponse == null) {
            onHandleIntent(new ArrayList<Repository>());
            return;
        }

        onHandleIntent(searchResponse.getRepositories());
    }
}
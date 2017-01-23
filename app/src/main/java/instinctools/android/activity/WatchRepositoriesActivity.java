package instinctools.android.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import instinctools.android.R;
import instinctools.android.adapters.RepositoryAdapter;
import instinctools.android.constans.Constants;
import instinctools.android.database.DBConstants;
import instinctools.android.database.providers.RepositoriesProvider;
import instinctools.android.decorations.DividerItemDecoration;
import instinctools.android.services.HttpUpdateWatchRepositoriesService;

public class WatchRepositoriesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_REPOSITORIES_ID = 1;

    // View
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Models
    private RepositoryAdapter mRepositoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_repositories);

        initView();

        getSupportLoaderManager().initLoader(LOADER_REPOSITORIES_ID, null, this);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_watch_repository_list);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, true));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mRepositoryAdapter = new RepositoryAdapter(this, mRecyclerView, null);
        mRecyclerView.setAdapter(mRepositoryAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_watch_repositories_list);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_watch_repositories_list);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onRefresh() {
        Intent intentService = new Intent(this, HttpUpdateWatchRepositoriesService.class);
        startService(intentService);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_REPOSITORIES_ID)
            return null;

        return new CursorLoader(this,
                RepositoriesProvider.REPOSITORY_CONTENT_URI,
                null, DBConstants.TABLE_REPOSITORIES + "." + DBConstants.REPOSITORY_TYPE + " = ?",
                new String[]{String.valueOf(Constants.REPOSITORY_TYPE_WATCH)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() != 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        } else
            onRefresh();

        // Hidden refresh bar
        mSwipeRefreshLayout.setRefreshing(false);

        mRepositoryAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

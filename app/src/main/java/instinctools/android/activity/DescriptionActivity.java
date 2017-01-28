package instinctools.android.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import instinctools.android.R;
import instinctools.android.adapters.IssueAdapter;
import instinctools.android.adapters.RepositoryAdapter;
import instinctools.android.database.DBConstants;
import instinctools.android.database.providers.RepositoriesProvider;
import instinctools.android.decorations.DividerItemDecoration;
import instinctools.android.imageloader.ImageLoader;
import instinctools.android.imageloader.transformers.CircleImageTransformer;
import instinctools.android.models.github.issues.Issue;
import instinctools.android.models.github.issues.IssueState;
import instinctools.android.models.github.repositories.Repository;
import instinctools.android.services.github.Direction;
import instinctools.android.services.github.GithubServiceListener;
import instinctools.android.services.github.repository.GithubServiceRepository;
import instinctools.android.services.github.user.GithubServiceUser;

public class DescriptionActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String BUNDLE_REPOSITORY_ID = "ID";
    public static final String EXTRA_USERNAME = "USERNAME";

    private static final int LOADER_REPOSITORY_ID = 1;

    // View
    private ViewGroup mLayoutCardView;
    private ProgressBar mProgressBar;
    private TextView mTextViewFullName;
    private TextView mTextViewDescription;
    private TextView mTextViewLanguage;
    private TextView mTextViewDefaultBranch;
    private TextView mTextViewForks;
    private TextView mTextViewStargazers;
    private TextView mTextViewWatchers;
    private TextView mTextViewOpenIssues;
    private TextView mTextViewOwnerLogin;
    private ImageView mImageViewOwnerAvatar;

    private Button mButtonStar;
    private Button mButtonWatch;

    private ProgressBar mProgressBarStar;
    private ProgressBar mProgressBarWatch;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Issues
    private RecyclerView mRecyclerViewIssuesOpened;
    private ProgressBar mProgressBarIssuesOpened;
    private CardView mCardViewIssuesOpened;

    private RecyclerView mRecyclerViewIssuesClosed;
    private ProgressBar mProgressBarIssuesClosed;
    private CardView mCardViewIssuesClosed;

    private IssueAdapter mIssueOpenedAdapter;
    private IssueAdapter mIssueClosedAdapter;

    private boolean mStarred;
    private boolean mWatched;

    private Repository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        mStarred = false;
        mWatched = false;

        initView();

        Intent intent = getIntent();
        if (intent != null) {
            long id = intent.getLongExtra(RepositoryAdapter.EXTRA_REPOSITORY_ID_TAG, -1);

            Bundle bundle = new Bundle();
            bundle.putLong(BUNDLE_REPOSITORY_ID, id);
            getSupportLoaderManager().initLoader(LOADER_REPOSITORY_ID, bundle, this);
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLayoutCardView = (ViewGroup) findViewById(R.id.layout_description_cards);
        mLayoutCardView.setVisibility(View.INVISIBLE);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_description_content);
        //
        mTextViewOwnerLogin = (TextView) findViewById(R.id.text_description_owner_login);
        mImageViewOwnerAvatar = (ImageView) findViewById(R.id.image_description_owner_avatar);
        mImageViewOwnerAvatar.setOnClickListener(this);
        //
        mTextViewFullName = (TextView) findViewById(R.id.text_description_fullname);
        mTextViewDescription = (TextView) findViewById(R.id.text_description_description);
        mTextViewLanguage = (TextView) findViewById(R.id.text_description_language);
        mTextViewDefaultBranch = (TextView) findViewById(R.id.text_description_default_branch);
        //
        mTextViewForks = (TextView) findViewById(R.id.text_description_forks);
        mTextViewStargazers = (TextView) findViewById(R.id.text_description_stargazers);
        mTextViewWatchers = (TextView) findViewById(R.id.text_description_watchers);
        mTextViewOpenIssues = (TextView) findViewById(R.id.text_description_open_issues);
        //
        mButtonStar = (Button) findViewById(R.id.button_description_star_repo);
        mButtonWatch = (Button) findViewById(R.id.button_description_watch_repo);

        mProgressBarStar = (ProgressBar) findViewById(R.id.pb_description_star_repo);
        mProgressBarWatch = (ProgressBar) findViewById(R.id.pb_description_watch_repo);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_description);

        // Opened
        mRecyclerViewIssuesOpened = (RecyclerView) findViewById(R.id.recycler_description_issues_opened);
        mRecyclerViewIssuesOpened.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, false));
        mRecyclerViewIssuesOpened.setVisibility(View.INVISIBLE);

        mIssueOpenedAdapter = new IssueAdapter(this, null);
        mRecyclerViewIssuesOpened.setAdapter(mIssueOpenedAdapter);
        mRecyclerViewIssuesOpened.setLayoutManager(new LinearLayoutManager(this));

        mProgressBarIssuesOpened = (ProgressBar) findViewById(R.id.pb_description_issue_opened);
        mProgressBarIssuesOpened.setVisibility(View.VISIBLE);

        mCardViewIssuesOpened = (CardView) findViewById(R.id.cardview_description_issues_opened);

        // Closed
        mRecyclerViewIssuesClosed = (RecyclerView) findViewById(R.id.recycler_description_issues_closed);
        mRecyclerViewIssuesClosed.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, false));
        mRecyclerViewIssuesClosed.setVisibility(View.INVISIBLE);

        mIssueClosedAdapter = new IssueAdapter(this, null);
        mRecyclerViewIssuesClosed.setAdapter(mIssueClosedAdapter);
        mRecyclerViewIssuesClosed.setLayoutManager(new LinearLayoutManager(this));

        mProgressBarIssuesClosed = (ProgressBar) findViewById(R.id.pb_description_issue_closed);
        mProgressBarIssuesClosed.setVisibility(View.VISIBLE);

        mCardViewIssuesClosed = (CardView) findViewById(R.id.cardview_description_issues_closed);

        updateStarButton(false);
        updateWatchButton(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_REPOSITORY_ID)
            return null;

        return new CursorLoader(this, RepositoriesProvider.REPOSITORY_CONTENT_URI, null, DBConstants.TABLE_REPOSITORIES + "." + DBConstants.REPOSITORY_ID + " = ?", new String[]{String.valueOf(args.getLong(BUNDLE_REPOSITORY_ID))}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mProgressBar.setVisibility(View.GONE);
        mLayoutCardView.setVisibility(View.VISIBLE);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        mRepository = Repository.fromCursor(cursor);
        initRepositoryData();

        if (!cursor.isClosed())
            cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_description_star_repo) {
            updateStarButton(false);

            GithubServiceUser.starredRepository(mRepository.getFullName(), !mStarred, new GithubServiceListener<Boolean>() {
                @Override
                public void onError(int code) {
                    updateStarButton(true);
                }

                @Override
                public void onSuccess(Boolean data) {
                    if (data)
                        mStarred = !mStarred;

                    updateStarButton(true);
                }
            });

        } else if (view.getId() == R.id.button_description_watch_repo) {
            updateWatchButton(false);

            GithubServiceUser.watchedRepository(mRepository.getFullName(), !mWatched, new GithubServiceListener<Boolean>() {
                @Override
                public void onError(int code) {
                    updateWatchButton(true);
                }

                @Override
                public void onSuccess(Boolean data) {
                    if (data)
                        mWatched = !mWatched;

                    updateWatchButton(true);
                }
            });
        } else if (view.getId() == R.id.image_description_owner_avatar) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(EXTRA_USERNAME, mTextViewOwnerLogin.getText().toString());
            startActivity(intent);
        }
    }

    private void updateStarButton(boolean visible) {
        if (mStarred)
            mButtonStar.setText(getString(R.string.title_description_button_unstar));
        else
            mButtonStar.setText(getString(R.string.title_description_button_star));

        if (visible) {
            mButtonStar.setVisibility(View.VISIBLE);
            mProgressBarStar.setVisibility(View.GONE);
        } else {
            mButtonStar.setVisibility(View.INVISIBLE);
            mProgressBarStar.setVisibility(View.VISIBLE);
        }
    }

    private void updateWatchButton(boolean visible) {
        if (mWatched)
            mButtonWatch.setText(getString(R.string.title_description_button_unwatch));
        else
            mButtonWatch.setText(getString(R.string.title_description_button_watch));

        if (visible) {
            mButtonWatch.setVisibility(View.VISIBLE);
            mProgressBarWatch.setVisibility(View.GONE);
        } else {
            mButtonWatch.setVisibility(View.INVISIBLE);
            mProgressBarWatch.setVisibility(View.VISIBLE);
        }
    }

    private void initRepositoryData() {
        getSupportActionBar().setTitle(mRepository.getFullName());

        mTextViewOwnerLogin.setText(mRepository.getRepositoryOwner().getLogin());
        ImageLoader
                .what(mRepository.getRepositoryOwner().getAvatarUrl())
                .error(R.drawable.ic_github_logo)
                .in(mImageViewOwnerAvatar)
                .transformer(new CircleImageTransformer())
                .load();

        mTextViewFullName.setText(mRepository.getName());
        mTextViewDescription.setText(mRepository.getDescription());
        mTextViewLanguage.setText(mRepository.getLanguage());
        mTextViewDefaultBranch.setText(mRepository.getDefaultBranch());
        //
        mTextViewForks.setText(String.valueOf(mRepository.getForks()));
        mTextViewStargazers.setText(String.valueOf(mRepository.getStargazers()));
        mTextViewWatchers.setText(String.valueOf(mRepository.getWatchers()));
        mTextViewOpenIssues.setText(String.valueOf(mRepository.getOpenIssues()));

        // Enable listener
        mButtonStar.setOnClickListener(this);
        mButtonWatch.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        GithubServiceUser.isStarredRepository(mRepository.getFullName(), new GithubServiceListener<Boolean>() {
            @Override
            public void onError(int code) {
                mStarred = false;
                updateStarButton(true);
            }

            @Override
            public void onSuccess(Boolean star) {
                mStarred = star;
                updateStarButton(true);
            }
        });

        GithubServiceUser.isWatchedRepository(mRepository.getFullName(), new GithubServiceListener<Boolean>() {
            @Override
            public void onError(int code) {
                mWatched = false;
                updateWatchButton(true);
            }

            @Override
            public void onSuccess(Boolean watch) {
                mWatched = watch;
                updateWatchButton(true);
            }
        });

        updateIssues();
    }

    private void updateIssues() {
        mCardViewIssuesOpened.setVisibility(View.VISIBLE);
        mCardViewIssuesClosed.setVisibility(View.VISIBLE);

        mProgressBarIssuesOpened.setVisibility(View.VISIBLE);
        mProgressBarIssuesClosed.setVisibility(View.VISIBLE);

        mRecyclerViewIssuesOpened.setVisibility(View.INVISIBLE);
        mRecyclerViewIssuesClosed.setVisibility(View.INVISIBLE);

        GithubServiceRepository.getRepositoryIssues(mRepository.getFullName(), IssueState.OPENED, Direction.DESC, new GithubServiceListener<List<Issue>>() {
            @Override
            public void onError(int code) {
                mCardViewIssuesOpened.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(List<Issue> issues) {
                if (issues == null) {
                    mCardViewIssuesOpened.setVisibility(View.GONE);
                    return;
                }

                mIssueOpenedAdapter.setIssues(issues);
                mIssueOpenedAdapter.notifyDataSetChanged();

                mRecyclerViewIssuesOpened.setVisibility(View.VISIBLE);
                mProgressBarIssuesOpened.setVisibility(View.GONE);
            }
        });

        GithubServiceRepository.getRepositoryIssues(mRepository.getFullName(), IssueState.CLOSED, Direction.DESC, new GithubServiceListener<List<Issue>>() {
            @Override
            public void onError(int code) {
                mCardViewIssuesClosed.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(List<Issue> issues) {
                if (issues == null) {
                    mCardViewIssuesClosed.setVisibility(View.GONE);
                    return;
                }

                mIssueClosedAdapter.setIssues(issues);
                mIssueClosedAdapter.notifyDataSetChanged();

                mRecyclerViewIssuesClosed.setVisibility(View.VISIBLE);
                mProgressBarIssuesClosed.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onRefresh() {
        GithubServiceRepository.getRepository(mRepository.getFullName(), new GithubServiceListener<Repository>() {
            @Override
            public void onError(int code) {
                mSwipeRefreshLayout.setRefreshing(false);
                Snackbar.make(findViewById(R.id.swiperefresh_description), R.string.msg_error_loading_data, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(Repository data) {
                mSwipeRefreshLayout.setRefreshing(false);

                if (data == null)
                    return;

                mRepository = data;
                initRepositoryData();
            }
        });
    }
}

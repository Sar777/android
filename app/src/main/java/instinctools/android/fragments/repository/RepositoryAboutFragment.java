package instinctools.android.fragments.repository;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import instinctools.android.R;
import instinctools.android.activity.ProfileActivity;
import instinctools.android.activity.RepositoryContributorsActivity;
import instinctools.android.activity.RepositoryDescriptionActivity;
import instinctools.android.activity.RepositoryIssuesActivity;
import instinctools.android.imageloader.ImageLoader;
import instinctools.android.imageloader.transformers.CircleImageTransformer;
import instinctools.android.loaders.repository.AsyncRepositoryInfoLoader;
import instinctools.android.models.github.errors.ErrorResponse;
import instinctools.android.models.github.repositories.Repository;
import instinctools.android.services.github.GithubServiceListener;
import instinctools.android.services.github.user.GithubServiceUser;

public class RepositoryAboutFragment extends Fragment implements LoaderManager.LoaderCallbacks<Repository>, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String EXTRA_REPOSITORY_FULLNAME = "FULLNAME";

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

    //
    private TextView mTextViewContributors;
    private TextView mTextViewIssues;

    private boolean mStarred;
    private boolean mWatched;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStarred = false;
        mWatched = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repository_description_about, null);

        mLayoutCardView = (ViewGroup) view.findViewById(R.id.layout_description_cards);
        mLayoutCardView.setVisibility(View.INVISIBLE);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_description_content);
        //
        mTextViewOwnerLogin = (TextView) view.findViewById(R.id.text_description_owner_login);
        mImageViewOwnerAvatar = (ImageView) view.findViewById(R.id.image_description_owner_avatar);
        mImageViewOwnerAvatar.setOnClickListener(this);
        //
        mTextViewFullName = (TextView) view.findViewById(R.id.text_description_fullname);
        mTextViewDescription = (TextView) view.findViewById(R.id.text_description_description);
        mTextViewLanguage = (TextView) view.findViewById(R.id.text_description_language);
        mTextViewDefaultBranch = (TextView) view.findViewById(R.id.text_description_default_branch);
        //
        mTextViewForks = (TextView) view.findViewById(R.id.text_description_forks);
        mTextViewStargazers = (TextView) view.findViewById(R.id.text_description_stargazers);
        mTextViewWatchers = (TextView) view.findViewById(R.id.text_description_watchers);
        mTextViewOpenIssues = (TextView) view.findViewById(R.id.text_description_open_issues);
        //
        mButtonStar = (Button) view.findViewById(R.id.button_description_star_repo);
        mButtonStar.setOnClickListener(this);

        mButtonWatch = (Button) view.findViewById(R.id.button_description_watch_repo);
        mButtonWatch.setOnClickListener(this);

        mProgressBarStar = (ProgressBar) view.findViewById(R.id.pb_description_star_repo);
        mProgressBarWatch = (ProgressBar) view.findViewById(R.id.pb_description_watch_repo);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_repository_description_about);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mTextViewContributors = (TextView) view.findViewById(R.id.text_repository_description_contributors);
        mTextViewContributors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RepositoryContributorsActivity.class);
                intent.putExtra(EXTRA_REPOSITORY_FULLNAME, ((RepositoryDescriptionActivity)getActivity()).getFullName());
                startActivity(intent);
            }
        });

        mTextViewIssues = (TextView) view.findViewById(R.id.text_repository_description_issues);
        mTextViewIssues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RepositoryIssuesActivity.class);
                intent.putExtra(EXTRA_REPOSITORY_FULLNAME, ((RepositoryDescriptionActivity)getActivity()).getFullName());
                startActivity(intent);
            }
        });

        updateStarButton(false);
        updateWatchButton(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getSupportLoaderManager().initLoader(RepositoryDescriptionActivity.LOADER_REPOSITORY_ABOUT_ID, Bundle.EMPTY, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().getSupportLoaderManager().destroyLoader(RepositoryDescriptionActivity.LOADER_REPOSITORY_ABOUT_ID);
    }

    @Override
    public Loader<Repository> onCreateLoader(int id, Bundle args) {
        if (id != RepositoryDescriptionActivity.LOADER_REPOSITORY_ABOUT_ID)
            return null;

        return new AsyncRepositoryInfoLoader(getContext(), ((RepositoryDescriptionActivity)getActivity()).getFullName());
    }

    @Override
    public void onLoadFinished(Loader<Repository> loader, Repository repository) {
        if (repository == null) {
            getActivity().finish();
            return;
        }

        mProgressBar.setVisibility(View.GONE);
        mLayoutCardView.setVisibility(View.VISIBLE);

        RepositoryDescriptionActivity activity = (RepositoryDescriptionActivity)getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(repository.getFullName());
            activity.getSupportActionBar().setSubtitle(repository.getDefaultBranch());
        }

        mTextViewOwnerLogin.setText(repository.getRepositoryOwner().getLogin());
        ImageLoader
                .what(repository.getRepositoryOwner().getAvatarUrl())
                .error(R.drawable.ic_github_logo)
                .in(mImageViewOwnerAvatar)
                .transformer(new CircleImageTransformer())
                .load();

        mTextViewFullName.setText(repository.getName());
        mTextViewDescription.setText(repository.getDescription());
        mTextViewLanguage.setText(repository.getLanguage());
        mTextViewDefaultBranch.setText(repository.getDefaultBranch());
        //
        mTextViewForks.setText(String.valueOf(repository.getForks()));
        mTextViewStargazers.setText(String.valueOf(repository.getStargazers()));
        mTextViewWatchers.setText(String.valueOf(repository.getWatchers()));
        mTextViewOpenIssues.setText(String.valueOf(repository.getOpenIssues()));

        // Enable listener
        mSwipeRefreshLayout.setRefreshing(false);

        GithubServiceUser.isStarredRepository(repository.getFullName(), new GithubServiceListener<Boolean>() {
            @Override
            public void onError(int code, ErrorResponse response) {
                mStarred = false;
                updateStarButton(true);
            }

            @Override
            public void onSuccess(Boolean star) {
                mStarred = star;
                updateStarButton(true);
            }
        });

        GithubServiceUser.isWatchedRepository(repository.getFullName(), new GithubServiceListener<Boolean>() {
            @Override
            public void onError(int code, ErrorResponse response) {
                mWatched = false;
                updateWatchButton(true);
            }

            @Override
            public void onSuccess(Boolean watch) {
                mWatched = watch;
                updateWatchButton(true);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Repository> loader) {

    }

    public void onClick(View view) {
        if (view.getId() == R.id.button_description_star_repo) {
            updateStarButton(false);

            GithubServiceUser.starredRepository(((RepositoryDescriptionActivity)getActivity()).getFullName(), !mStarred, new GithubServiceListener<Boolean>() {
                @Override
                public void onError(int code, ErrorResponse response) {
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

            GithubServiceUser.watchedRepository(((RepositoryDescriptionActivity)getActivity()).getFullName(), !mWatched, new GithubServiceListener<Boolean>() {
                @Override
                public void onError(int code, ErrorResponse response) {
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
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USERNAME, mTextViewOwnerLogin.getText().toString());
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, 0, 0);
            startActivity(intent, options.toBundle());
        }
    }

    private void updateStarButton(boolean visible) {
        if (mStarred)
            mButtonStar.setText(getString(R.string.title_repository_description_button_unstar));
        else
            mButtonStar.setText(getString(R.string.title_repository_description_button_star));

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
            mButtonWatch.setText(getString(R.string.title_repository_description_button_unwatch));
        else
            mButtonWatch.setText(getString(R.string.title_repository_description_button_watch));

        if (visible) {
            mButtonWatch.setVisibility(View.VISIBLE);
            mProgressBarWatch.setVisibility(View.GONE);
        } else {
            mButtonWatch.setVisibility(View.INVISIBLE);
            mProgressBarWatch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        getActivity().getSupportLoaderManager().getLoader(RepositoryDescriptionActivity.LOADER_REPOSITORY_ABOUT_ID).forceLoad();
    }
}

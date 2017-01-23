package instinctools.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import instinctools.android.R;
import instinctools.android.activity.DescriptionActivity;
import instinctools.android.database.DBConstants;
import instinctools.android.models.github.repositories.Repository;

public class RepositoryAdapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private Context mContext;
    private RecyclerView mRecyclerView;

    public static final String EXTRA_REPOSITORY_ID_TAG = "REPOSITORY";

    public RepositoryAdapter(Context context, RecyclerView recyclerView, @Nullable Cursor cursor) {
        super(DBConstants.REPOSITORY_ID, context, cursor);
        mContext = context;
        mRecyclerView = recyclerView;
    }

    @Override
    public void onClick(View view) {
        int position = mRecyclerView.getChildAdapterPosition(view);
        Intent intent = new Intent(mContext, DescriptionActivity.class);
        intent.putExtra(EXTRA_REPOSITORY_ID_TAG, getItemId(position));
        mContext.startActivity(intent);
    }

    private class RepositoryItemHolder extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mDescription;
        private TextView mPrivateTextView;
        private TextView mLanguageTextView;
        private TextView mWatchTextView;
        private TextView mStarTextView;
        private TextView mForkTextView;
        private ImageView mRepositoryType;

        RepositoryItemHolder(View view) {
            super(view);

            mTitle = (TextView) view.findViewById(R.id.text_name);
            mDescription = (TextView) view.findViewById(R.id.text_description);
            mLanguageTextView = (TextView) view.findViewById(R.id.text_language);
            mWatchTextView = (TextView) view.findViewById(R.id.text_repo_watch);
            mStarTextView = (TextView) view.findViewById(R.id.text_repo_star);
            mForkTextView = (TextView) view.findViewById(R.id.text_repo_forks);
            mPrivateTextView = (TextView) view.findViewById(R.id.text_private_repository);
            mRepositoryType = (ImageView) view.findViewById(R.id.image_repository_type);
        }

        private void onBindViewHolder(Cursor cursor) {
            Repository item = Repository.fromCursor(cursor);

            if (item.isPrivate())
                mPrivateTextView.setVisibility(View.VISIBLE);
            else
                mPrivateTextView.setVisibility(View.GONE);

            if (item.isFork())
                mRepositoryType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_github_repo_forked));
            else
                mRepositoryType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_github_repo));

            mTitle.setText(item.getFullName());

            mWatchTextView.setText(String.valueOf(item.getWatchers()));
            mStarTextView.setText(String.valueOf(item.getStargazers()));
            mForkTextView.setText(String.valueOf(item.getForks()));

            if (item.getDescription().isEmpty())
                mDescription.setVisibility(View.GONE);
            else {
                mDescription.setVisibility(View.VISIBLE);
                mDescription.setText(item.getDescription());
            }

            if (item.getLanguage().isEmpty())
                mLanguageTextView.setVisibility(View.GONE);
            else {
                mLanguageTextView.setVisibility(View.VISIBLE);
                mLanguageTextView.setText(item.getLanguage());
            }
        }
    }

    private class HeaderItemHolder extends RecyclerView.ViewHolder {
        HeaderItemHolder(View view) {
            super(view);
        }
    }

    private class EmptyItemHolder extends RecyclerView.ViewHolder {
        EmptyItemHolder(View view) {
            super(view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_header, parent, false);
                return new HeaderItemHolder(view);
            }
            case VIEW_TYPE_EMPTY: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_empty, parent, false);
                return new EmptyItemHolder(view);
            }
            case VIEW_TYPE_ITEM:
                break;
            default:
                throw new UnsupportedOperationException("Unsupported view item type: " + viewType);
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_repository, parent, false);
        view.setOnClickListener(this);
        return new RepositoryItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, Cursor cursor) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            if (holder instanceof RepositoryItemHolder)
                ((RepositoryItemHolder) holder).onBindViewHolder(cursor);
        }
    }
}
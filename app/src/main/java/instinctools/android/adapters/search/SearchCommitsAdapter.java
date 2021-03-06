package instinctools.android.adapters.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import instinctools.android.App;
import instinctools.android.R;
import instinctools.android.adapters.AbstractRecyclerAdapter;
import instinctools.android.imageloader.ImageLoader;
import instinctools.android.imageloader.transformers.CircleImageTransformer;
import instinctools.android.models.github.commits.EventCommit;
import instinctools.android.models.github.errors.ErrorResponse;
import instinctools.android.models.github.user.User;
import instinctools.android.services.github.GithubServiceListener;
import instinctools.android.services.github.user.GithubServiceUser;

public class SearchCommitsAdapter extends AbstractRecyclerAdapter<EventCommit> {
    public SearchCommitsAdapter(@NonNull Context context) {
        super(context);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageViewAuthorAvatar;
        private ImageView mImageViewCommitterAvatar;
        private TextView mTextViewMessage;
        private TextView mTextViewTimeAgo;

        ItemViewHolder(View view) {
            super(view);

            mImageViewAuthorAvatar = (ImageView) view.findViewById(R.id.image_author_avatar);
            mImageViewCommitterAvatar = (ImageView) view.findViewById(R.id.image_committer_avatar);

            mTextViewMessage = (TextView) view.findViewById(R.id.text_commit_message);
            mTextViewTimeAgo = (TextView) view.findViewById(R.id.text_commit_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventCommit commit = getItem(getAdapterPosition());
                    App.launchUrl(mContext, commit.getHtmlUrl());
                }
            });
        }

        void onBindViewHolder(int position) {
            EventCommit commit = getItem(position);

            mImageViewAuthorAvatar.setVisibility(View.VISIBLE);
            mImageViewCommitterAvatar.setVisibility(View.VISIBLE);

            if (commit.getAuthorId() != 0) {
                if (commit.getAuthorId().equals(commit.getCommitterId())) {
                    mImageViewCommitterAvatar.setVisibility(View.INVISIBLE);
                    GithubServiceUser.getUser(commit.getAuthorId(), new GithubServiceListener<User>() {
                        @Override
                        public void onError(int code, @Nullable ErrorResponse response) {
                        }

                        @Override
                        public void onSuccess(User user) {
                            ImageLoader
                                    .what(user.getAvatarUrl())
                                    .transformer(new CircleImageTransformer())
                                    .in(mImageViewAuthorAvatar)
                                    .load();
                        }
                    });
                } else {
                    GithubServiceUser.getUser(commit.getAuthorId(), new GithubServiceListener<User>() {
                        @Override
                        public void onError(int code, @Nullable ErrorResponse response) {
                        }

                        @Override
                        public void onSuccess(User user) {
                            ImageLoader
                                    .what(user.getAvatarUrl())
                                    .transformer(new CircleImageTransformer())
                                    .in(mImageViewAuthorAvatar)
                                    .load();
                        }
                    });

                    if (commit.getCommitterId() != 0)
                        GithubServiceUser.getUser(commit.getCommitterId(), new GithubServiceListener<User>() {
                            @Override
                            public void onError(int code, @Nullable ErrorResponse response) {
                            }

                            @Override
                            public void onSuccess(User user) {
                                ImageLoader
                                        .what(user.getAvatarUrl())
                                        .transformer(new CircleImageTransformer())
                                        .in(mImageViewCommitterAvatar)
                                        .load();
                            }
                        });
                }
            }

            mTextViewMessage.setText(commit.getMessage());

            String commitDate = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(commit.getCommitterDate());
            if (commit.getAuthorId().equals(commit.getCommitterId()))
                mTextViewTimeAgo.setText(String.format("%s committed %s", commit.getCommitterName(), commitDate));
            else
                mTextViewTimeAgo.setText(String.format("%s committed with %s on %s", commit.getAuthorName(), commit.getCommitterName(), commitDate));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (holder == null) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_search_commit, parent, false);
            return new ItemViewHolder(itemView);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder instanceof ItemViewHolder)
            ((ItemViewHolder)holder).onBindViewHolder(position);
    }
}
package com.example.myimagetextclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_POST = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private List<Post> posts;
    private Context context;

    private SharedPreferences preferences;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = new ArrayList<>(posts);
        this.preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public int getItemViewType(int position) {
        return posts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_POST;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_POST) {
            View view = inflater.inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);

        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof PostViewHolder) {
            Post post = posts.get(position);
            PostViewHolder vh = (PostViewHolder) holder;

            /** -------------------------------
             *  1. 显示封面（使用 Glide）
             * -------------------------------*/
            Glide.with(context)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.cover_placeholder)
                    .into(vh.coverImage);

            /** -------------------------------
             *  2. 动态设置瀑布流高度（按宽高比）
             * -------------------------------*/
            if (post.width > 0 && post.height > 0) {

                float ratio = (float) post.height / post.width;

                int screenWidth = context.getResources()
                        .getDisplayMetrics().widthPixels;

                int itemWidth = screenWidth / 2;

                int realHeight = (int) (itemWidth * ratio);

                ViewGroup.LayoutParams params = vh.coverImage.getLayoutParams();
                params.height = realHeight;
                vh.coverImage.setLayoutParams(params);
            }

            /** -------------------------------
             *  3. 标题/正文摘要
             * -------------------------------*/
            if (post.title != null && post.title.length() > 0) {
                vh.titleText.setText(post.title);
            } else {
                // 取正文前 30 字
                if (post.content.length() > 30) {
                    vh.titleText.setText(post.content.substring(0, 30) + "...");
                } else {
                    vh.titleText.setText(post.content);
                }
            }

            /** -------------------------------
             *  4. 作者头像 + 昵称（Glide 网络头像）
             * -------------------------------*/
            Glide.with(context)
                    .load(post.authorAvatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(vh.avatarImage);

            vh.authorText.setText(post.authorName);

            /** -------------------------------
             *  5. 点赞功能（SharedPreferences）
             * -------------------------------*/
            Set<String> likedSet =
                    preferences.getStringSet("liked_posts", new HashSet<>());

            boolean liked = likedSet.contains(post.postId);

            vh.likeIcon.setImageResource(liked
                    ? R.drawable.ic_liked
                    : R.drawable.ic_like);

            vh.likeCountText.setText(String.valueOf(post.likeCount));

            vh.likeIcon.setOnClickListener(v -> {

                Set<String> likedNow =
                        preferences.getStringSet("liked_posts", new HashSet<>());
                SharedPreferences.Editor editor = preferences.edit();

                if (likedNow.contains(post.postId)) {
                    likedNow.remove(post.postId);
                    post.likeCount -= 1;
                    vh.likeIcon.setImageResource(R.drawable.ic_like);

                } else {
                    likedNow.add(post.postId);
                    post.likeCount += 1;
                    vh.likeIcon.setImageResource(R.drawable.ic_liked);
                }

                vh.likeCountText.setText(String.valueOf(post.likeCount));

                editor.putStringSet("liked_posts", likedNow);
                editor.apply();
            });

            /** -------------------------------
             *  6. 点击进入详情页
             * -------------------------------*/
            vh.itemView.setOnClickListener(v -> {

                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("post_index", position);
                context.startActivity(intent);
            });
        }
    }

    /** 刷新全量数据 */
    public void setPosts(List<Post> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    /** 分页追加更多数据 */
    public void addPosts(List<Post> newPosts) {
        int start = posts.size();
        posts.addAll(newPosts);
        notifyItemRangeInserted(start, newPosts.size());
    }

    /** 加载更多 footer */
    public void addLoadingFooter() {
        posts.add(null);
        notifyItemInserted(posts.size() - 1);
    }

    /** 移除加载更多 footer */
    public void removeLoadingFooter() {
        if (posts.size() > 0 && posts.get(posts.size() - 1) == null) {
            int index = posts.size() - 1;
            posts.remove(index);
            notifyItemRemoved(index);
        }
    }

    /** -------------------------------
     *  ViewHolder: Post 项
     * -------------------------------*/
    static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView coverImage;
        TextView titleText;
        ImageView avatarImage;
        TextView authorText;
        ImageView likeIcon;
        TextView likeCountText;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            coverImage = itemView.findViewById(R.id.post_image);
            titleText = itemView.findViewById(R.id.post_title);
            avatarImage = itemView.findViewById(R.id.post_avatar);
            authorText = itemView.findViewById(R.id.post_user);
            likeIcon = itemView.findViewById(R.id.post_like_icon);
            likeCountText = itemView.findViewById(R.id.post_like_count);
        }
    }

    /** -------------------------------
     *  ViewHolder: Loading 项
     * -------------------------------*/
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}



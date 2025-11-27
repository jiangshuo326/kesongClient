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
        // 获取SharedPreferences用于存储点赞/关注状态
        this.preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public int getItemViewType(int position) {
        // 判断当前项是作品数据还是加载中提示
        return posts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_POST;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            Post post = posts.get(position);
            PostViewHolder postHolder = (PostViewHolder) holder;
            // 设置封面图（裁剪显示）
            if (!post.imageResIds.isEmpty()) {
                postHolder.coverImage.setImageResource(post.imageResIds.get(0));
            }
            // 随机调整封面高度，实现瀑布流错位效果
            int mod = post.id % 3;
            int heightDp;
            if (mod == 0) {
                heightDp = 240;
            } else if (mod == 1) {
                heightDp = 180;
            } else {
                heightDp = 200;
            }
            float density = context.getResources().getDisplayMetrics().density;
            ViewGroup.LayoutParams params = postHolder.coverImage.getLayoutParams();
            params.height = (int) (heightDp * density);
            postHolder.coverImage.setLayoutParams(params);
            // 设置标题（无标题则截取正文内容代替）
            if (post.title != null && post.title.length() > 0) {
                postHolder.titleText.setText(post.title);
            } else {
                String content = post.content;
                String snippet = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                postHolder.titleText.setText(snippet);
            }
            // 作者头像和昵称
            postHolder.avatarImage.setImageResource(post.avatarResId);
            postHolder.authorText.setText(post.author);
            // 点赞数量和状态
            Set<String> likedSet = preferences.getStringSet("liked_posts", new HashSet<String>());
            boolean liked = likedSet != null && likedSet.contains(String.valueOf(post.id));
            postHolder.likeCountText.setText(String.valueOf(post.likeCount));
            if (liked) {
                postHolder.likeIcon.setImageResource(R.drawable.ic_liked);
            } else {
                postHolder.likeIcon.setImageResource(R.drawable.ic_like);
            }
            // 点赞按钮点击事件：更新点赞状态、本地持久化
            postHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Set<String> likedPosts = preferences.getStringSet("liked_posts", new HashSet<String>());
                    if (likedPosts == null) likedPosts = new HashSet<>();
                    SharedPreferences.Editor editor = preferences.edit();
                    if (likedPosts.contains(String.valueOf(post.id))) {
                        // 已点赞 -> 取消点赞
                        likedPosts.remove(String.valueOf(post.id));
                        postHolder.likeIcon.setImageResource(R.drawable.ic_like);
                        post.likeCount -= 1;
                        postHolder.likeCountText.setText(String.valueOf(post.likeCount));
                    } else {
                        // 未点赞 -> 添加点赞
                        likedPosts.add(String.valueOf(post.id));
                        postHolder.likeIcon.setImageResource(R.drawable.ic_liked);
                        post.likeCount += 1;
                        postHolder.likeCountText.setText(String.valueOf(post.likeCount));
                    }
                    editor.putStringSet("liked_posts", likedPosts);
                    editor.apply();
                }
            });
            // 卡片点击事件：打开作品详情页
            postHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("post_index", position);
                    context.startActivity(intent);
                }
            });
        }
        // LoadingViewHolder无需绑定数据
    }

    // 刷新列表数据（下拉刷新时调用）
    public void setPosts(List<Post> newPosts) {
        this.posts.clear();
        this.posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    // 追加列表数据（上滑加载更多时调用）
    public void addPosts(List<Post> newPosts) {
        int start = posts.size();
        this.posts.addAll(newPosts);
        notifyItemRangeInserted(start, newPosts.size());
    }

    // 添加加载中尾部占位
    public void addLoadingFooter() {
        this.posts.add(null);
        notifyItemInserted(posts.size() - 1);
    }

    // 移除加载中尾部占位
    public void removeLoadingFooter() {
        if (!posts.isEmpty() && posts.get(posts.size() - 1) == null) {
            int removeIndex = posts.size() - 1;
            posts.remove(removeIndex);
            notifyItemRemoved(removeIndex);
        }
    }

    // 作品项 ViewHolder
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


    // 加载中提示项 ViewHolder
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

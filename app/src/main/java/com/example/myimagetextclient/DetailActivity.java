package com.example.myimagetextclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailActivity extends AppCompatActivity {

    private Post post;
    private SharedPreferences preferences;

    private ImageView backButton;
    private ImageView avatarImage;
    private TextView authorName;
    private TextView followButton;
    private ViewPager2 viewPager;
    private ProgressBar imageProgress;

    private TextView titleText;
    private TextView contentText;
    private TextView dateText;

    private EditText commentInput;
    private ImageView likeButton;
    private ImageView shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        /** ---------------------------
         *   1. 获取 Post 对象
         * ---------------------------*/
        int index = getIntent().getIntExtra("post_index", -1);
        if (index < 0 || index >= PostData.currentPosts.size()) {
            finish();
            return;
        }
        post = PostData.currentPosts.get(index);

        /** ---------------------------
         *   2. 绑定控件
         * ---------------------------*/
        backButton = findViewById(R.id.detail_back);
        avatarImage = findViewById(R.id.detail_avatar);
        authorName = findViewById(R.id.detail_author_name);
        followButton = findViewById(R.id.btn_follow);
        viewPager = findViewById(R.id.view_pager);
        imageProgress = findViewById(R.id.image_progress);

        titleText = findViewById(R.id.detail_title);
        contentText = findViewById(R.id.detail_content);
        dateText = findViewById(R.id.detail_date);

        commentInput = findViewById(R.id.comment_input);
        likeButton = findViewById(R.id.detail_like_button);
        shareButton = findViewById(R.id.detail_share_button);

        /** ---------------------------
         *   3. 返回按钮
         * ---------------------------*/
        backButton.setOnClickListener(v -> finish());

        /** ---------------------------
         *   4. 加载头像（网络）
         * ---------------------------*/
        Glide.with(this)
                .load(post.authorAvatarUrl)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(avatarImage);

        authorName.setText(post.authorName == null ? "未知用户" : post.authorName);

        /** ---------------------------
         *   5. 关注按钮
         * ---------------------------*/
        Set<String> followed = preferences.getStringSet("followed_authors", new HashSet<>());
        boolean isFollowed = followed.contains(post.authorName);

        updateFollowButton(isFollowed);

        followButton.setOnClickListener(v -> {
            Set<String> followedSet = preferences.getStringSet("followed_authors", new HashSet<>());
            SharedPreferences.Editor editor = preferences.edit();

            if (followedSet.contains(post.authorName)) {
                followedSet.remove(post.authorName);
                updateFollowButton(false);
            } else {
                followedSet.add(post.authorName);
                updateFollowButton(true);
            }

            editor.putStringSet("followed_authors", followedSet);
            editor.apply();
        });

        /** ---------------------------
         *   6. 详情页顶部图片（支持单图）
         * ---------------------------*/
        ImagePagerAdapter adapter = new ImagePagerAdapter(post);
        viewPager.setAdapter(adapter);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = screenWidth;
        viewPager.setLayoutParams(params);

        // 单图 → 隐藏进度条
        imageProgress.setVisibility(View.GONE);

        /** ---------------------------
         *   7. 标题
         * ---------------------------*/
        if (post.title != null && post.title.length() > 0) {
            titleText.setText(post.title);
        } else {
            titleText.setVisibility(View.GONE);
        }

        /** ---------------------------
         *   8. 正文（话题高亮）
         * ---------------------------*/
        setContentWithTopics(post.content);

        /** ---------------------------
         *   9. 发布时间
         * ---------------------------*/
        dateText.setText(formatPublishTime(post.publishTime));

        /** ---------------------------
         *   10. 点赞按钮
         * ---------------------------*/
        Set<String> liked = preferences.getStringSet("liked_posts", new HashSet<>());
        boolean isLiked = liked.contains(String.valueOf(post.postId));
        likeButton.setImageResource(isLiked ? R.drawable.ic_liked : R.drawable.ic_like);

        likeButton.setOnClickListener(v -> {
            Set<String> likedSet = preferences.getStringSet("liked_posts", new HashSet<>());
            SharedPreferences.Editor editor = preferences.edit();

            if (likedSet.contains(String.valueOf(post.postId))) {
                likedSet.remove(String.valueOf(post.postId));
                likeButton.setImageResource(R.drawable.ic_like);
            } else {
                likedSet.add(String.valueOf(post.postId));
                likeButton.setImageResource(R.drawable.ic_liked);
            }
            editor.putStringSet("liked_posts", likedSet);
            editor.apply();
        });

        /** ---------------------------
         *   11. 分享
         * ---------------------------*/
        shareButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, post.title != null ? post.title : post.content);
            startActivity(Intent.createChooser(intent, "分享到"));
        });

        /** ---------------------------
         *   12. 评论（提示）
         * ---------------------------*/
        commentInput.setOnClickListener(v ->
                Toast.makeText(this, "评论功能未实现", Toast.LENGTH_SHORT).show());
    }

    /** 更新关注按钮样式 */
    private void updateFollowButton(boolean followed) {
        if (followed) {
            followButton.setText("已关注");
            followButton.setBackgroundColor(Color.LTGRAY);
            followButton.setTextColor(Color.BLACK);
        } else {
            followButton.setText("关注");
            followButton.setBackgroundColor(Color.RED);
            followButton.setTextColor(Color.WHITE);
        }
    }

    /** 话题高亮 */
    private void setContentWithTopics(String content) {

        SpannableString spanText = new SpannableString(content);

        Matcher matcher = Pattern.compile("#([^#]+)#").matcher(content);

        while (matcher.find()) {
            final String topicText = matcher.group(1);

            spanText.setSpan(new ForegroundColorSpan(Color.parseColor("#2196F3")),
                    matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            spanText.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(DetailActivity.this, TopicActivity.class);
                    intent.putExtra("topic", topicText);
                    startActivity(intent);
                }
            }, matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        contentText.setText(spanText);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /** 时间格式 */
    private String formatPublishTime(long timeMillis) {
        Calendar now = Calendar.getInstance();
        Calendar t = Calendar.getInstance();
        t.setTimeInMillis(timeMillis);

        if (now.get(Calendar.YEAR) != t.get(Calendar.YEAR)) {
            return String.format("%02d-%02d", t.get(Calendar.MONTH) + 1, t.get(Calendar.DAY_OF_MONTH));
        }

        int dayNow = now.get(Calendar.DAY_OF_YEAR);
        int dayT = t.get(Calendar.DAY_OF_YEAR);

        if (dayNow == dayT) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(t.getTime());
        }
        if (dayNow - dayT == 1) {
            return "昨天";
        }
        if (dayNow - dayT < 7) {
            return (dayNow - dayT) + "天前";
        }
        return String.format("%02d-%02d", t.get(Calendar.MONTH) + 1, t.get(Calendar.DAY_OF_MONTH));
    }

    /** ---------------------------
     *   ViewPager 图片适配器（网络版）
     * ---------------------------*/
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.Holder> {
        private final Post post;

        public ImagePagerAdapter(Post post) {
            this.post = post;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView img = new ImageView(parent.getContext());
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new Holder(img);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Glide.with(holder.imageView.getContext())
                    .load(post.imageUrl)
                    .placeholder(R.drawable.cover_placeholder)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return 1; // API 只有一张图
        }

        class Holder extends RecyclerView.ViewHolder {
            ImageView imageView;
            Holder(View v) {
                super(v);
                imageView = (ImageView) v;
            }
        }
    }
}

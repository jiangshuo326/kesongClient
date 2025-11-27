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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
    private ImageView backButton;
    private ImageView avatarImage;
    private TextView authorName;
    private Button followButton;
    private ViewPager2 viewPager;
    private ProgressBar imageProgress;
    private TextView titleText;
    private TextView contentText;
    private TextView dateText;
    private EditText commentInput;
    private ImageView likeButton;
    private ImageView commentButton;
    private ImageView favoriteButton;
    private ImageView shareButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // 获取传递的作品索引，找到对应Post对象
        int index = getIntent().getIntExtra("post_index", -1);
        if (index >= 0 && index < PostData.currentPosts.size()) {
            post = PostData.currentPosts.get(index);
        }
        // 绑定布局中的各个UI组件
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
        commentButton = findViewById(R.id.detail_comment_button);
        favoriteButton = findViewById(R.id.detail_favorite_button);
        shareButton = findViewById(R.id.detail_share_button);
        preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        if (post == null) {
            // 若未获取到作品数据，关闭详情页
            finish();
            return;
        }
        // 顶部作者信息
        avatarImage.setImageResource(post.avatarResId);
        authorName.setText(post.author);
        // 设置关注按钮初始状态
        Set<String> followedSet = preferences.getStringSet("followed_authors", new HashSet<String>());
        boolean isFollowed = followedSet != null && followedSet.contains(post.author);
        updateFollowButton(isFollowed);
        // 关注按钮点击事件：切换关注/取消关注状态
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<String> followed = preferences.getStringSet("followed_authors", new HashSet<String>());
                if (followed == null) followed = new HashSet<>();
                SharedPreferences.Editor editor = preferences.edit();
                if (followed.contains(post.author)) {
                    // 取消关注
                    followed.remove(post.author);
                    updateFollowButton(false);
                } else {
                    // 关注作者
                    followed.add(post.author);
                    updateFollowButton(true);
                }
                editor.putStringSet("followed_authors", followed);
                editor.apply();
            }
        });
        // 返回按钮点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 关闭详情页，返回列表
            }
        });

        // 设置图片ViewPager
        ImagePagerAdapter imageAdapter = new ImagePagerAdapter(post.imageResIds);
        viewPager.setAdapter(imageAdapter);
        // 设置ViewPager高度为屏幕宽度，实现1:1显示比例（简化处理）
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = screenWidth;
        viewPager.setLayoutParams(params);
        // 配置图片进度条（多图时显示）
        int imageCount = post.imageResIds.size();
        if (imageCount > 1) {
            imageProgress.setVisibility(View.VISIBLE);
            imageProgress.setMax(100);
            imageProgress.setProgress(100 / imageCount);
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    imageProgress.setProgress((position + 1) * 100 / imageCount);
                }
            });
        } else {
            imageProgress.setVisibility(View.GONE);
        }

        // 标题和正文内容
        if (post.title != null && post.title.length() > 0) {
            titleText.setText(post.title);
        } else {
            titleText.setVisibility(View.GONE);
        }
        // 正文中话题词高亮和点击处理
        SpannableString spanText = new SpannableString(post.content);
        Pattern pattern = Pattern.compile("#([^#]+)#");
        Matcher matcher = pattern.matcher(post.content);
        while (matcher.find()) {
            final String topic = matcher.group(1);
            // 设置高亮颜色
            spanText.setSpan(new ForegroundColorSpan(Color.parseColor("#2196F3")),
                    matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 设置可点击跳转
            spanText.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // 点击话题文本，打开话题页面
                    Intent intent = new Intent(DetailActivity.this, TopicActivity.class);
                    intent.putExtra("topic", topic);
                    startActivity(intent);
                }
            }, matcher.start(), matcher.end(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        contentText.setText(spanText);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());
        // 发布时间文本
        dateText.setText(formatPublishTime(post.publishTime));

        // 底部点赞按钮初始状态
        Set<String> likedSet = preferences.getStringSet("liked_posts", new HashSet<String>());
        boolean liked = likedSet != null && likedSet.contains(String.valueOf(post.id));
        if (liked) {
            likeButton.setImageResource(R.drawable.ic_liked);
        } else {
            likeButton.setImageResource(R.drawable.ic_like);
        }
        // 点赞按钮点击事件：更新点赞状态并同步修改列表数据
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<String> likedPosts = preferences.getStringSet("liked_posts", new HashSet<String>());
                if (likedPosts == null) likedPosts = new HashSet<>();
                SharedPreferences.Editor editor = preferences.edit();
                if (likedPosts.contains(String.valueOf(post.id))) {
                    likedPosts.remove(String.valueOf(post.id));
                    likeButton.setImageResource(R.drawable.ic_like);
                    post.likeCount--;
                } else {
                    likedPosts.add(String.valueOf(post.id));
                    likeButton.setImageResource(R.drawable.ic_liked);
                    post.likeCount++;
                }
                editor.putStringSet("liked_posts", likedPosts);
                editor.apply();
            }
        });
        // 分享按钮点击事件
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareContent = "分享一个作品： " +
                        (post.title != null && post.title.length() > 0 ? post.title : post.content);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
                startActivity(Intent.createChooser(shareIntent, "分享到"));
            }
        });
        // 评论输入框点击事件（仅提示，无实际评论功能）
        commentInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DetailActivity.this, "评论功能暂未实现", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 更新关注按钮文本和样式
    private void updateFollowButton(boolean isFollowed) {
        if (isFollowed) {
            followButton.setText("已关注");
            followButton.setBackgroundColor(Color.LTGRAY);
            followButton.setTextColor(Color.BLACK);
        } else {
            followButton.setText("关注");
            followButton.setBackgroundColor(Color.RED);
            followButton.setTextColor(Color.WHITE);
        }
    }

    // 格式化发布时间
    private String formatPublishTime(long timeMillis) {
        Calendar now = Calendar.getInstance();
        Calendar postTime = Calendar.getInstance();
        postTime.setTimeInMillis(timeMillis);
        if (now.get(Calendar.YEAR) != postTime.get(Calendar.YEAR)) {
            // 不同年份：返回月日
            int month = postTime.get(Calendar.MONTH) + 1;
            int day = postTime.get(Calendar.DAY_OF_MONTH);
            return String.format("%02d-%02d", month, day);
        }
        // 同一年
        int nowDay = now.get(Calendar.DAY_OF_YEAR);
        int postDay = postTime.get(Calendar.DAY_OF_YEAR);
        if (nowDay == postDay) {
            // 当天：HH:mm
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(postTime.getTime());
        } else if (nowDay - postDay == 1) {
            // 昨天：昨天 HH:mm
            SimpleDateFormat sdf = new SimpleDateFormat("昨天 HH:mm", Locale.getDefault());
            return sdf.format(postTime.getTime());
        } else if (nowDay - postDay < 7) {
            // 7天内：x天前
            int diffDays = nowDay - postDay;
            return diffDays + "天前";
        } else {
            // 超过7天：MM-dd
            int month = postTime.get(Calendar.MONTH) + 1;
            int day = postTime.get(Calendar.DAY_OF_MONTH);
            return String.format("%02d-%02d", month, day);
        }
    }

    // 图片ViewPager的适配器
    private class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private List<Integer> imageResIds;
        public ImagePagerAdapter(List<Integer> imageResIds) {
            this.imageResIds = imageResIds;
        }
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 动态创建ImageView作为页视图
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }
        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.imageView.setImageResource(imageResIds.get(position));
        }
        @Override
        public int getItemCount() {
            return imageResIds.size();
        }
        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}

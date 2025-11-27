package com.example.myimagetextclient;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostData {
    // 当前内存中的作品列表（供详情页等使用）
    public static List<Post> currentPosts = new ArrayList<>();

    private static final int INITIAL_COUNT = 10; // 首次加载作品数
    private static final int PAGE_COUNT = 5;     // 每次加载更多作品数
    private static final int MAX_PAGE = 3;       // 最大页数（包含初始页）
    private static int currentPage = 1;
    // 示例作者昵称列表
    private static final List<String> AUTHOR_NAMES = Arrays.asList("旅行达人", "美食专家", "摄影师", "户外爱好者", "科技迷");
    // 示例正文模板（每条包含一个话题词）
    private static final List<String> CONTENT_TEMPLATES = Arrays.asList(
            "今天打卡了#北京美食#，分享我的体验。",
            "一次难忘的旅行：记录了#旅游攻略#。",
            "整理了一些#摄影技巧#，希望对大家有用。",
            "#露营体验#可以让人彻底放松！",
            "刚入手了#黑科技#产品，真是颠覆想象！",
            "周末和朋友去郊外#露营体验#，收获满满！"
    );
    // 示例标题模板（与正文模板顺序对应）
    private static final List<String> TITLE_TEMPLATES = Arrays.asList(
            "北京美食探店",
            "难忘的旅程分享",
            "摄影小技巧合集",
            "郊外露营记",
            "新奇科技产品测评",
            "周末露营体验"
    );

    // 获取初始作品列表（模拟网络请求）
    public static List<Post> getInitialPosts(Context context) {
        currentPage = 1;
        List<Post> posts = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int i = 1; i <= INITIAL_COUNT; i++) {
            int id = i;
            // 作者昵称和头像（根据id简单取模映射）
            String author = AUTHOR_NAMES.get((id - 1) % AUTHOR_NAMES.size());
            int avatarIndex = ((id - 1) % AUTHOR_NAMES.size()) + 1;
            int avatarRes = context.getResources().getIdentifier("avatar" + avatarIndex, "drawable", context.getPackageName());
            // 标题和正文
            int templateIndex = (id - 1) % CONTENT_TEMPLATES.size();
            String content = CONTENT_TEMPLATES.get(templateIndex);
            String title = "";
            if (id % 3 != 0) { // 模拟部分作品有标题
                title = TITLE_TEMPLATES.get(templateIndex);
            }
            // 随机点赞数
            int likeCount = 100 + (id * 5);
            // 发布时间：模拟不同时间场景
            long publishTime = now;
            if (i == 1) {
                publishTime = now - 2 * 60 * 60 * 1000;          // 2小时前
            } else if (i == 2) {
                publishTime = now - 26 * 60 * 60 * 1000;         // 昨天
            } else if (i == 3) {
                publishTime = now - 3 * 24 * 60 * 60 * 1000;     // 3天前
            } else if (i == 4) {
                publishTime = now - 6 * 24 * 60 * 60 * 1000;     // 6天前
            } else if (i == 5) {
                publishTime = now - 8 * 24 * 60 * 60 * 1000;     // 8天前（超过7天）
            } else {
                int randomDays = 1 + (id * 7 % 10);
                publishTime = now - randomDays * 24 * 60 * 60 * 1000;
            }
            // 图片列表（每3个作品有3张图，其余单图）
            List<Integer> imageResIds = new ArrayList<>();
            if (id % 3 == 0) {
                int baseIndex = (id - 1) % 6 + 1;
                for (int j = 0; j < 3; j++) {
                    int imgIndex = ((baseIndex - 1 + j) % 6) + 1;
                    int resId = context.getResources().getIdentifier("cover" + imgIndex, "drawable", context.getPackageName());
                    imageResIds.add(resId);
                }
            } else {
                int imgIndex = (id - 1) % 6 + 1;
                int resId = context.getResources().getIdentifier("cover" + imgIndex, "drawable", context.getPackageName());
                imageResIds.add(resId);
            }
            Post post = new Post(id, title, content, author, avatarRes, imageResIds, likeCount, publishTime);
            posts.add(post);
        }
        return posts;
    }

    // 获取下一页作品列表（加载更多）
    public static List<Post> getMorePosts(Context context) {
        List<Post> newPosts = new ArrayList<>();
        if (currentPage < MAX_PAGE) {
            currentPage++;
            int startId = (currentPage - 1) * INITIAL_COUNT + 1;
            for (int i = 0; i < PAGE_COUNT; i++) {
                int id = startId + i;
                // 生成新Post，复用模板数据
                String author = AUTHOR_NAMES.get((id - 1) % AUTHOR_NAMES.size());
                int avatarIndex = ((id - 1) % AUTHOR_NAMES.size()) + 1;
                int avatarRes = context.getResources().getIdentifier("avatar" + avatarIndex, "drawable", context.getPackageName());
                int templateIndex = (id - 1) % CONTENT_TEMPLATES.size();
                String content = CONTENT_TEMPLATES.get(templateIndex);
                String title = "";
                if (id % 3 != 0) {
                    title = TITLE_TEMPLATES.get(templateIndex);
                }
                int likeCount = 50 + (id * 3);
                long now = System.currentTimeMillis();
                long publishTime = now - (id % 5 + 1) * 24 * 60 * 60 * 1000;
                List<Integer> imageResIds = new ArrayList<>();
                if (id % 3 == 0) {
                    int baseIndex = (id - 1) % 6 + 1;
                    for (int j = 0; j < 3; j++) {
                        int imgIndex = ((baseIndex - 1 + j) % 6) + 1;
                        int resId = context.getResources().getIdentifier("cover" + imgIndex, "drawable", context.getPackageName());
                        imageResIds.add(resId);
                    }
                } else {
                    int imgIndex = (id - 1) % 6 + 1;
                    int resId = context.getResources().getIdentifier("cover" + imgIndex, "drawable", context.getPackageName());
                    imageResIds.add(resId);
                }
                Post post = new Post(id, title, content, author, avatarRes, imageResIds, likeCount, publishTime);
                newPosts.add(post);
            }
        }
        return newPosts;
    }
}

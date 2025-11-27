package com.example.myimagetextclient;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class PostData {

    /** ---------------------------
     *   当前内存缓存（供详情页使用）
     * ---------------------------*/
    public static List<Post> currentPosts = new ArrayList<>();

    /** 是否还有更多分页内容 */
    public static boolean hasMore = true;

    /** 下一页的 offset（接口暂时未提供 offset 参数，所以仅记录） */
    private static int nextOffset = 0;

    /**
     * ===============================
     *  ✔ 1. 首次加载首页数据（20 条）
     * ===============================
     */
    public static void loadInitial(Context context, LoadCallback callback) {

        String url = "https://college-training-camp.bytedance.com/feed/?count=20&accept_video_clip=false";

        NetworkManager.getJson(url, new NetworkManager.Callback() {
            @Override
            public void onSuccess(String json) {

                PostResponse response =
                        JsonHelper.toObject(json, PostResponse.class);

                if (response != null && response.status_code == 0) {

                    List<Post> list = convertFromApi(response.post_list);

                    currentPosts = list;
                    hasMore = (response.has_more == 1);
                    nextOffset = list.size();

                    callback.onLoaded(list);

                } else {
                    callback.onFailed("API 数据解析失败");
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailed("网络错误：" + e.getMessage());
            }
        });
    }


    /**
     * ===============================
     *  ✔ 2. 加载更多（10 条）
     * ===============================
     */
    public static void loadMore(Context context, LoadCallback callback) {

        if (!hasMore) {
            callback.onLoaded(new ArrayList<>());
            return;
        }

        String url = "https://college-training-camp.bytedance.com/feed/?count=10&accept_video_clip=false";

        NetworkManager.getJson(url, new NetworkManager.Callback() {
            @Override
            public void onSuccess(String json) {

                PostResponse response =
                        JsonHelper.toObject(json, PostResponse.class);

                if (response != null && response.status_code == 0) {

                    List<Post> more = convertFromApi(response.post_list);

                    currentPosts.addAll(more);
                    hasMore = (response.has_more == 1);
                    nextOffset += more.size();

                    callback.onLoaded(more);

                } else {
                    callback.onFailed("API 数据解析失败");
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailed("网络错误：" + e.getMessage());
            }
        });
    }


    /**
     * ==========================================
     *  ✔ 3. API -> UI 模型转换（PostItem → Post）
     * ==========================================
     */
    public static List<Post> convertFromApi(List<PostItem> list) {

        List<Post> result = new ArrayList<>();
        if (list == null) return result;

        for (PostItem item : list) {

            Post post = new Post();    // 使用无参构造器

            post.postId = item.post_id;
            post.title = item.title;
            post.content = item.content;
            post.publishTime = item.create_time;

            /** 作者信息 */
            if (item.author != null) {
                post.authorName = item.author.nickname;
                post.authorAvatarUrl = item.author.avatar;
            }

            /** 第一张图或视频信息 */
            if (item.clips != null && !item.clips.isEmpty()) {

                Clip clip = item.clips.get(0);

                post.imageUrl = clip.url;
                post.width = clip.width;
                post.height = clip.height;

                post.isVideo = (clip.type == 1);
            }

            result.add(post);
        }

        return result;
    }


    /** ==========================================
     *  回调接口：成功/失败
     * ========================================== */
    public interface LoadCallback {
        void onLoaded(List<Post> posts);

        void onFailed(String msg);
    }
}



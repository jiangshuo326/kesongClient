package com.example.myimagetextclient;

import java.util.ArrayList;
import java.util.List;

public class Post {

    /** -------------------------------
     *  API 原始字段
     * -------------------------------*/
    public String postId;            // API 作品 ID（字符串）
    public String title;             // 标题
    public String content;           // 正文
    public long publishTime;         // create_time 时间戳

    /** 作者信息 */
    public String authorId;          // author.user_id
    public String authorName;        // author.nickname
    public String authorAvatarUrl;   // author.avatar

    /** 片段（图片或视频） */
    public boolean isVideo = false;  // clip.type == 1 → 视频
    public String imageUrl;          // 第一张图/视频封面
    public int width;                // 宽
    public int height;               // 高

    /** 扩展字段：未来多图用 */
    public List<String> imageUrls = new ArrayList<>();

    /** -------------------------------
     *  App 本地字段（用于 UI）
     * -------------------------------*/
    public int likeCount = 0;        // 本地点赞计数

    public int autoId;               // 本地自动生成 ID（用于随机高度）

    public Post() {

    }
}



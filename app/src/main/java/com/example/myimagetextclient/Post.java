package com.example.myimagetextclient;

import java.util.List;

public class Post {
    public int id;                       // 作品ID
    public String title;                // 标题
    public String content;              // 正文内容
    public String author;               // 作者昵称
    public int avatarResId;             // 作者头像资源ID
    public List<Integer> imageResIds;   // 图片资源ID列表
    public int likeCount;               // 点赞数量
    public long publishTime;            // 发布时间戳（毫秒）

    public Post(int id, String title, String content, String author,
                int avatarResId, List<Integer> imageResIds,
                int likeCount, long publishTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.avatarResId = avatarResId;
        this.imageResIds = imageResIds;
        this.likeCount = likeCount;
        this.publishTime = publishTime;
    }
}

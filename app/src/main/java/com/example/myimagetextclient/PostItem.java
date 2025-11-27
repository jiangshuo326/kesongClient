package com.example.myimagetextclient;

import java.util.List;

public class PostItem {
    public String post_id;
    public String title;
    public String content;
    public List<HashTag> hashtag;
    public long create_time;
    public Author author;
    public List<Clip> clips;
    public Music music;
}

package com.example.myimagetextclient;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TopicActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        TextView topicText = findViewById(R.id.topic_text);
        String topic = getIntent().getStringExtra("topic");
        if (topic != null) {
            // 显示话题名称
            topicText.setText("话题: #" + topic + "#");
        }
    }
}

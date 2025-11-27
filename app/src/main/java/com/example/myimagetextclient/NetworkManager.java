package com.example.myimagetextclient;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkManager {

    private static final OkHttpClient client = new OkHttpClient();

    /** ================================
     *  封装好的 API 调用函数
     *  ================================ */
    public static void getFeed(int count, boolean acceptVideoClip, Callback callback) {

        // 拼 URL
        String url = "https://college-training-camp.bytedance.com/feed/?"
                + "count=" + count
                + "&accept_video_clip=" + acceptVideoClip;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    callback.onFailure(new Exception("HTTP Error " + response.code()));
                    return;
                }

                String body = response.body().string();
                callback.onSuccess(body);  // 把 JSON 返回
            }
        });
    }

    /** ================================
     *  统一回调
     *  ================================ */
    public interface Callback {
        void onSuccess(String responseBody);

        void onFailure(Exception e);
    }

    public static void getJson(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(new Exception("HTTP Error " + response.code()));
                    return;
                }

                String body = response.body().string();
                callback.onSuccess(body);
            }
        });
    }

}


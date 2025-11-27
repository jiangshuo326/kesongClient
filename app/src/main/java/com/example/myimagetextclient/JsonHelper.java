package com.example.myimagetextclient;

import com.google.gson.Gson;

public class JsonHelper {
    private static final Gson gson = new Gson();

    public static <T> T toObject(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}

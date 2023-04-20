package ru.whitebeef.beeflibrary.utils;

import com.google.gson.Gson;

public class GsonUtils {

    public static String parseObject(Object o) {
        return new Gson().toJson(o);
    }

    public static Object parseJSON(String json, Class<?> clazz) {
        return new Gson().fromJson(json, clazz);
    }

}

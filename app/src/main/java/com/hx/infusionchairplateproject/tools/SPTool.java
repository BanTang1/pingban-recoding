package com.hx.infusionchairplateproject.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.hx.infusionchairplateproject.EntiretyApplication;

/**
 * SharedPreferences 工具类
 */
public class SPTool {
    private static final String PREFS_NAME = "config";
    private static final Context context = EntiretyApplication.context;

    public static void putString(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }
}

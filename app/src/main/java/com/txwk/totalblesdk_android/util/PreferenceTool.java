package com.txwk.totalblesdk_android.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.txwk.totalblesdk_android.BuildConfig;
import java.util.Map;

/**
 * 共享参数工具类
 */
public class PreferenceTool {

    public static final String PREF_CONFIG = BuildConfig.APPLICATION_ID + "_config";
    private static SharedPreferences.Editor editor;
    private static SharedPreferences pref;

    public static SharedPreferences getPref(){
        return pref;
    }

    public static void init(Context context) {
        pref = context.getSharedPreferences(PREF_CONFIG, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static void clear() {
        if (editor == null) return;
        editor.clear();
    }

    public static void commit() {
        if (editor == null) return;
        editor.commit();
    }

    public static void apply() {
        if (editor == null) return;
        editor.apply();
    }

    public static boolean contains(String key) {
        return pref != null && pref.contains(key);
    }

    public static float getFloat(String key, float defValue) {
        return pref != null ? pref.getFloat(key, defValue) : defValue;
    }

    public static int getInt(String key, int defValue) {
        return pref != null ? pref.getInt(key, defValue) : defValue;
    }

    public static long getLong(String key, long defValue) {
        return pref != null ? pref.getLong(key, defValue) : defValue;
    }

    public static String getString(String key) {
        return pref != null ? pref.getString(key, "") : "";
    }

    public static String getString(String key, String defValue) {
        return pref != null ? pref.getString(key, defValue) : defValue;
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return pref != null ? pref.getBoolean(key, defValue) : defValue;
    }

    public static Map<String, ?> getAll() {
        return pref != null ? pref.getAll() : null;
    }



    public static void putFloat(String key, float value) {
        if (editor == null) return;
        editor.putFloat(key, value);
    }

    public static void putInt(String key, int value) {
        if (editor == null) return;
        editor.putInt(key, value);
    }

    public static void putLong(String key, long value) {
        if (editor == null) return;
        editor.putLong(key, value);
    }

    public static void putString(String key, String value) {
        if (editor == null) return;
        editor.putString(key, value);
    }

    public static void putBoolean(String key, boolean value) {
        if (editor == null) return;
        editor.putBoolean(key, value);
    }

    public static void remove(String key) {
        if (editor == null) return;
        editor.remove(key);
    }

}

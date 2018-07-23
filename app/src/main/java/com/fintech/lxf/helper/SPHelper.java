package com.fintech.lxf.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SPHelper {

    private static final String DATA_NAME = "MySharePre";

    private SharedPreferences mSharedPre;

    private static volatile SPHelper instance = null;

    private SPHelper() {
    }

    public static SPHelper getInstance() {
        if (instance == null) {
            synchronized (SPHelper.class) {
                if (instance == null) {
                    instance = new SPHelper();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        mSharedPre = context.getSharedPreferences(DATA_NAME, Context.MODE_PRIVATE);
    }

    public Boolean getBoolean(String key) {
        if (!isInit()) {
            return false;
        }
        return mSharedPre.getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        if (!isInit()) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPre.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        if (!isInit()) {
            return 0;
        }
        return mSharedPre.getInt(key, 1);
    }

    public void putInt(String key, int value) {
        if (!isInit()) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPre.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putFloat(String key, float value) {
        if (!isInit()) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPre.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloat(String key) {
        if (!isInit()) {
            return 0;
        }
        return mSharedPre.getFloat(key, 1);
    }

    public String getString(String key) {
        if (!isInit()) {
            return "";
        }
        return mSharedPre.getString(key, "");
    }

    public void putString(String key, String value) {
        if (!isInit()) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPre.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private boolean isInit() {
        return mSharedPre != null;
    }
}

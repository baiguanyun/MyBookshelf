package com.kunfei.bookshelf.help;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.MApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Preferences存储管理
 */
public class PreferencesHelper {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson mGson;

    @SuppressLint("CommitPrefEdits")
    public PreferencesHelper() {
        prefs = PreferenceManager.getDefaultSharedPreferences(MApplication.getInstance());
        editor = prefs.edit();

        mGson = new Gson();
    }

    public static PreferencesHelper getInstance() {
        return HolderClass.INSTANCE;
    }

    public Gson getGson() {
        return mGson;
    }

    public String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void putBoolean(String key, Boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key, Boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    public void putObject(String key, Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }

        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key is empty or null");
        }

        editor.putString(key, mGson.toJson(object));
        editor.commit();
    }

    public <T> T getObject(String key, Class<T> classOfT) {
        String json = prefs.getString(key, null);

        if (json == null) {
            return null;
        }
        try {
            return mGson.fromJson(json, classOfT);
        } catch (Exception e) {
            throw new IllegalArgumentException("Object storaged with key " + key + " is instanceof other class");
        }
    }

    /**
     * 保存List
     *
     * @param tag
     * @param datalist
     */
    public void setDataList(String tag, List<?> datalist) {
        if (null == datalist || datalist.size() <= 0)
            return;
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        editor.putString(tag, strJson);
        editor.apply();
    }

    /**
     * 获取List
     *
     * @param tag
     * @return
     */
    public List<?> getDataList(String tag) {
        List<?> datalist = new ArrayList<>();
        String strJson = prefs.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<?>>() {
        }.getType());
        return datalist;

    }

    private static final class HolderClass {
        private static final PreferencesHelper INSTANCE = new PreferencesHelper();
    }

}

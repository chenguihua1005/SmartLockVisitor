package com.socketble.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.socketble.manager.AppManager;

import java.util.HashSet;
import java.util.Set;


public class SharedPreferenceUtil {

    /**
     * get instance of SharedPreference by file name
     *
     * @param name  name of the SharedPreference storage file. (i.e group control, user login etc)
     * @return  instance of the particular SharedPreference
     */
    public static SharedPreferences getSharedPreferenceInstanceByName(String name) {

        return AppManager.getInstance().getApplication().
                getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * get/set methods for particular SharePreference
     *
     * @param name name of the SharedPreference storage file. (i.e group control, user login etc)
     * @param key
     * @param defaultValue
     * @return
     */
    public static void setPrefFloat(final String name, final String key, final float defaultValue) {
        getSharedPreferenceInstanceByName(name).edit().putFloat(key, defaultValue).commit();
    }

    public static float getPrefFloat(final String name, final String key, final float defaultValue) {
        return getSharedPreferenceInstanceByName(name).getFloat(key, defaultValue);
    }

    public static void setPrefInt(final String name, final String key, final int defaultValue) {
        getSharedPreferenceInstanceByName(name).edit().putInt(key, defaultValue).commit();
    }

    public static int getPrefInt(final String name, final String key, final int defaultValue) {
        return getSharedPreferenceInstanceByName(name).getInt(key, defaultValue);
    }

    public static long getPrefLong(final String name, final String key, final long defaultValue) {
        return getSharedPreferenceInstanceByName(name).getLong(key, defaultValue);
    }

    public static void setPrefBoolean(final String name, final String key, final boolean defaultValue) {
        getSharedPreferenceInstanceByName(name).edit().putBoolean(key, defaultValue).commit();
    }

    public static boolean getPrefBoolean(final String name, final String key, final boolean defaultValue) {
        return getSharedPreferenceInstanceByName(name).getBoolean(key, defaultValue);
    }

    public static String getPrefString(final String name, String key, final String defaultValue) {
        return getSharedPreferenceInstanceByName(name).getString(key, defaultValue);
    }

    public static void setPrefString(final String name, final String key, final String defaultValue) {
        getSharedPreferenceInstanceByName(name).edit().putString(key, defaultValue).apply();
    }

    public static void setPrefLong(final String name, final String key, final long defaultValue) {
        getSharedPreferenceInstanceByName(name).edit().putLong(key, defaultValue).commit();
    }

    public static void setPrefStringSet(final String name, final String key, final Set<String> defaultValue) {
        getSharedPreferenceInstanceByName(name).edit().putStringSet(key, defaultValue).commit();
    }

    public static Set<String> getPrefStringSet(final String name, final String key, final HashSet<String> defaultValue) {
        return getSharedPreferenceInstanceByName(name).getStringSet(key, defaultValue);
    }

    public static void clearPreference(Context context, final SharedPreferences p) {
        final SharedPreferences.Editor editor = p.edit();
        editor.clear();
        editor.commit();
    }

    public static void clearPreference(final SharedPreferences p) {
        final SharedPreferences.Editor editor = p.edit();
        editor.clear();
        editor.commit();
    }

}

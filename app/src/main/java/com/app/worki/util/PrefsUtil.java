package com.app.worki.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsUtil {
    private static final String name = "worki";

    private static SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static boolean clearAllPrefs(Context context) {
        try {
            getSharedPrefs(context).edit().clear().apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeValue(Context context, String key) {
        getSharedPrefs(context).edit().remove(key).apply();
    }

    private static final String Login = "Login";
    public static void setLogin(Context context, boolean b){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putBoolean(Login, b).apply();
    }
    public static boolean isLogin(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getBoolean(Login, false);
    }

    private static final String Username = "Username";
    public static void setUsername(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(Username, s).apply();
    }
    public static String getUsername(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(Username, "");
    }

    private static final String UserType = "UserType";
    public static void setUserType(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(UserType, s).apply();
    }
    public static String getUserType(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(UserType, "");
    }

    private static final String Photo = "Photo";
    public static void setPhoto(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(Photo, s).apply();
    }
    public static String getPhoto(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(Photo, "");
    }


}

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

    private static final String UserId = "UserId";
    public static void setUserId(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(UserId, s).apply();
    }
    public static String getUserId(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(UserId, "");
    }

    private static final String Lat = "Lat";
    public static void setLat(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(Lat, s).apply();
    }
    public static String getLat(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(Lat, "0");
    }

    private static final String Lng = "Lng";
    public static void setLng(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(Lng, s).apply();
    }
    public static String getLng(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(Lng, "0");
    }

    private static final String Radius = "Radius";
    public static void setRadius(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(Radius, s).apply();
    }
    public static String getRadius(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(Radius, "0");
    }

    private static final String Logs = "Logs";
    public static void setLogs(Context context, String s){
        SharedPreferences sp = getSharedPrefs(context);
        String logs = getLogs(context);
        logs = logs+"\n"+s;
        sp.edit().putString(Logs, logs).apply();
    }
    public static String getLogs(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(Logs, "");
    }


}

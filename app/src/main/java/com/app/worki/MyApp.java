package com.app.worki;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import java.lang.reflect.Method;

public class MyApp extends Application {
    public static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

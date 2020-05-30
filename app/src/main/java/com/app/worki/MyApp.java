package com.app.worki;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.annotation.AcraMailSender;

import java.lang.reflect.Method;

@AcraMailSender(
        mailTo = "saif052m@gmail.com"
)
public class MyApp extends MultiDexApplication {
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}

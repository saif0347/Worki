package com.app.worki.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.app.worki.receiver.AlarmReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmUtil {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");

    public static void setNextAlarm(Context context, boolean now){
        Intent intent = new Intent(context, AlarmReceiver.class);
        if(now){
            AlarmUtil.setAlarm(context, 1000, 1*60*1000, intent);
        }
        for(int i=1; i<=24; i++){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE, 0);
            Date todayDate = new Date();
            Date alarmDate = new Date();
            alarmDate.setTime(calendar.getTimeInMillis());
            alarmDate = Utils.getOnlyFutureTime(todayDate, alarmDate);
            LogUtil.loge("alarm: "+timeFormat.format(alarmDate));
            AlarmUtil.setAlarm(context, i, alarmDate.getTime() - todayDate.getTime(), intent);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Calendar.HOUR_OF_DAY, i);
            calendar1.set(Calendar.MINUTE, 30);
            Date todayDate1 = new Date();
            Date alarmDate1 = new Date();
            alarmDate1.setTime(calendar1.getTimeInMillis());
            alarmDate1 = Utils.getOnlyFutureTime(todayDate1, alarmDate1);
            LogUtil.loge("alarm: "+timeFormat.format(alarmDate1));
            AlarmUtil.setAlarm(context, i+30, alarmDate1.getTime() - todayDate1.getTime(), intent);
        }
    }

    public static void setAlarm(Context context, int reqCode, long offset, Intent myIntent) {
        LogUtil.loge("after min: "+(offset/(1000*60)));
        if (offset > 0) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reqCode, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(alarmManager == null)
                return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+offset, pendingIntent);
                alarmManager.setAlarmClock(info, pendingIntent);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+offset, pendingIntent);
            }
            else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+offset, pendingIntent);
            }
        }
    }

    public static void cancelAlarm(Context context, int reqCode, Intent myIntent){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reqCode, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager == null)
            return;
        alarmManager.cancel(pendingIntent);
    }

    public static boolean hasAlarm(Context context, int reqCode, Intent myIntent){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reqCode, myIntent, PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent != null)
            return true;
        else
            return false;
    }
}

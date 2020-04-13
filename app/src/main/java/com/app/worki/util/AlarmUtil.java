package com.app.worki.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.app.worki.receiver.AlarmReceiver;

public class AlarmUtil {

    public static void setNextAlarm(Context context, boolean now){
        Intent intent = new Intent(context, AlarmReceiver.class);
        if(now)
            AlarmUtil.setAlarm(context, 1, 1*60*1000, intent);
        else
            AlarmUtil.setAlarm(context, 1, 30*60*1000, intent);
    }

    public static void setAlarm(Context context, int reqCode, long offset, Intent myIntent) {
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

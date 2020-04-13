package com.app.worki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.app.worki.util.AlarmUtil;

public class OnBootAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // set next alarm
            AlarmUtil.setNextAlarm(context, false);
        }
        else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            // set next alarm
            AlarmUtil.setNextAlarm(context, false);
        }
    }

}

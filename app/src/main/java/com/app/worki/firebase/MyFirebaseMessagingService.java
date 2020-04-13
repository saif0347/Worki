package com.app.worki.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.app.worki.R;
import com.app.worki.UserHome;
import com.app.worki.util.PrefsUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("message", "new message came");
        if(PrefsUtil.isLogin(getApplicationContext())){
            showNotification(remoteMessage);
        }
    }

    public void showNotification(RemoteMessage remoteMessage){
        Log.e("message", "notification came");
        Intent intent = new Intent(this, UserHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Random r = new Random();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, r.nextInt(10000), intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(remoteMessage.getData().get("title"));
        notificationBuilder.setContentText(remoteMessage.getData().get("message"));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        // include vibrate permission in AndroidManifest file
        notificationBuilder.setVibrate(new long[] {1000, 1000});
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(getResources().getColor(R.color.white));
            notificationBuilder.setSmallIcon(R.drawable.worki);//36x36 and transparent white
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.worki));
        }
        else {
            notificationBuilder.setSmallIcon(R.drawable.worki);
        }
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager, notificationBuilder);
        }
        notificationManager.notify(r.nextInt(10000), notificationBuilder.build());
        sendBroadcast(new Intent("Notification"));
    }

    private void createNotificationChannel(NotificationManager mNotifyMgr, NotificationCompat.Builder mBuilder) {
        String channelId = "channel-worki";
        String channelName = "channel-worki";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mChannel.enableVibration(true);
            mNotifyMgr.createNotificationChannel(mChannel);
            mBuilder.setChannelId(channelId);
        }
    }
}
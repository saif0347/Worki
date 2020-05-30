package com.app.worki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import com.app.worki.Transparent;
import com.app.worki.model.LocationModel;
import com.app.worki.util.AlarmUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LocationUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PrefsUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.POWER_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {
    private boolean serviceStarted = false;
    private int count = 0;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(!PrefsUtil.isLogin(context)){
            return;
        }
        if(PrefsUtil.getUserType(context).equals("admin")){
            return;
        }

        // set next alarm
        AlarmUtil.setNextAlarm(context, false);

        if(!LocationUtil.isGpsOn(context)){
            return;
        }

        //Intent mIntent = new Intent(context, MyIntentService.class);
        //MyIntentService.enqueueWork(context, mIntent);

        Intent transparent = new Intent(context, Transparent.class);
        transparent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(transparent);

        checkUserLocation(context);
        getWakeLock(context);
    }

    private void getWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Worki::workitag");
        wakeLock.acquire(30000);
    }

    private void checkUserLocation(final Context context) {
        double lat = Double.parseDouble(PrefsUtil.getLat(context));
        double lng = Double.parseDouble(PrefsUtil.getLng(context));
        int radius = Integer.parseInt(PrefsUtil.getRadius(context));
        String userId = PrefsUtil.getUserId(context);
        if(lat==0 && lng==0 && radius==0) {
            return;
        }
        // find current location
        serviceStarted = true;
        count = 0;
        LocationUtil.startGpsService(context, location -> {
            LogUtil.loge("lat: "+location.getLatitude());
            LogUtil.loge("lng: "+location.getLongitude());
            if(!serviceStarted) {
                Log.e("tag", "awara service");
                return;
            }

            // check distance
            int distance = LocationUtil.getDistanceFromLatLonMeters(location.getLatitude(), location.getLongitude(), lat, lng);
            LogUtil.loge("distance: "+distance+" meter");
            int status;
            if(distance <= radius){
                status = 1;
            }
            else{
                status = 0;
            }

            // update status
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            hashMap.put("distance", ""+distance);
            hashMap.put("status_time", ""+System.currentTimeMillis());
            FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.users, userId, new FirestoreUtil.AddUpdateResult() {
                @Override
                public void success() {
                }
                @Override
                public void fail(String error) {
                }
            });

            // testing add to logs
            Date date = new Date();
            date.setTime(System.currentTimeMillis());
            String time = new SimpleDateFormat("hh:mm:ss").format(date);
            HashMap<String, Object> log = new HashMap<>();
            String s = location.getLatitude()+":"+location.getLongitude()+"|"+distance+"|"+PrefsUtil.getUsername(context);
            log.put(time, s);
//            FirestoreUtil.addUpdateDoc(log, "testing", "testing", new FirestoreUtil.AddUpdateResult() {
//                @Override
//                public void success() {
//                }
//                @Override
//                public void fail(String error) {
//                }
//            });

            // check iteration
            if(count >= 3){
                LocationUtil.stopGps();
                serviceStarted = false;
            }
            count++;
            LogUtil.loge("count: "+count);
            PrefsUtil.setLogs(context, time+"="+s);
        });
        loadSettings(context);
    }

    private void loadSettings(Context context) {
        FirestoreUtil.getDocData(FirestoreUtil.location, FirestoreUtil.location, new FirestoreUtil.LoadResult() {
            @Override
            public void success(DocumentSnapshot snapshot) {
                LocationModel model = snapshot.toObject(LocationModel.class);
                if (model != null) {
                    PrefsUtil.setLat(context, model.getLat());
                    PrefsUtil.setLng(context, model.getLng());
                    PrefsUtil.setRadius(context, model.getRadius());
                }
            }
            @Override
            public void error(String error) {}
        });
    }
}
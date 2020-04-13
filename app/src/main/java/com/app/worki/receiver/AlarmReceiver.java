package com.app.worki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.PowerManager;
import android.util.Log;

import com.app.worki.EditProfile;
import com.app.worki.model.LocationModel;
import com.app.worki.model.LogModel;
import com.app.worki.service.MyIntentService;
import com.app.worki.util.AlarmUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LocationUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PrefsUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

        getWakeLock(context);
        checkUserLocation(context);
    }

    private void getWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Worki::workitag");
        wakeLock.acquire(30000);
    }

    private void checkUserLocation(final Context context) {
        // find target location
        FirestoreUtil.getDocData(FirestoreUtil.location, FirestoreUtil.location, new FirestoreUtil.LoadResult() {
            @Override
            public void success(DocumentSnapshot snapshot) {
                LocationModel model = snapshot.toObject(LocationModel.class);
                if (model != null) {
                    double lat = Double.parseDouble(model.getLat());
                    double lng = Double.parseDouble(model.getLng());
                    int radius = Integer.parseInt(model.getRadius());
                    // find user
                    FirestoreUtil.getDocsFiltered(FirestoreUtil.users, "username", PrefsUtil.getUsername(context), new FirestoreUtil.LoadResultDocs() {
                        @Override
                        public void success(QuerySnapshot querySnapshot) {
                            if(querySnapshot.size() == 0)
                                return;
                            DocumentSnapshot userSnapshot = querySnapshot.getDocuments().get(0);
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
                                userSnapshot.getReference().update(hashMap);

                                // testing add to logs
                                Date date = new Date();
                                date.setTime(System.currentTimeMillis());
                                String time = new SimpleDateFormat("hh:mm:ss").format(date);
                                HashMap<String, Object> log = new HashMap<>();
                                log.put(time, location.getLatitude()+":"+location.getLongitude()+"|"+distance+"|"+PrefsUtil.getUsername(context));
                                FirestoreUtil.addUpdateDoc(log, "testing", "testing", new FirestoreUtil.AddUpdateResult() {
                                    @Override
                                    public void success() {
                                    }
                                    @Override
                                    public void fail(String error) {
                                    }
                                });

                                // check iteration
                                if(count >= 3){
                                    LocationUtil.stopGps();
                                    serviceStarted = false;
                                }
                                count++;
                                LogUtil.loge("count: "+count);
                            });
                        }
                        @Override
                        public void error(String error) {
                        }
                    });
                }
            }
            @Override
            public void error(String error) {
            }
        });
    }
}
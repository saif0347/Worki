package com.app.worki.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.app.worki.model.LocationModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LocationUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PrefsUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MyIntentService extends JobIntentService {
    private boolean serviceStarted = false;
    private int count = 0;

    static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent intent) {
        LogUtil.loge("enqueueWork");
        enqueueWork(context, MyIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        LogUtil.loge("onHandleWork");
        checkUserLocation(this);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                                    //stopSelf();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.loge("onDestroy service");
    }
}

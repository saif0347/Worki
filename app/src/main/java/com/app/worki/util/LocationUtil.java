package com.app.worki.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtil {
    public interface LocationResult {
        void currentLocationSuccess(Location location);
        void currentLocationFailed(String message);
    }

    public static boolean isGpsOn(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }

    private static boolean dialogOpen = false;

    public static void showGpsDisabledAlert(final Context context, String title, String msg) {
        if(dialogOpen)
            return;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(callGPSSettingIntent);
                dialogInterface.dismiss();
                dialogOpen = false;
            }
        });
        dialog.show();
        dialogOpen = true;
    }

    public static void getCurrentLocation(final Context context, final LocationResult locationResult) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation().addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null)
                    locationResult.currentLocationSuccess(location);
                else
                    locationResult.currentLocationFailed("");
            }
        }).addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                locationResult.currentLocationFailed(e.getMessage());
            }
        });
    }

    public static String getAddressFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String address = "";
        try {
            List<Address> arrayList = geocoder.getFromLocation(latitude, longitude, 1);
            address = arrayList.get(0).getLocality()+":"+arrayList.get(0).getCountryName();
            Log.e("tag", "country: "+arrayList.get(0).getCountryName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static String getCoordinatesFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        String coordinates = "0,0,0";
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            coordinates = location.getLatitude() + "," + location.getLongitude() + ",0";
            return coordinates;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordinates;
    }

    //----------------------------------------GPS Service-------------------------------------------------------

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_UPDATE_INTERVAL = 5000;
    public static FusedLocationProviderClient mFusedLocationClient;
    private static LocationCallback locationCallback;

    public interface GpsResult {
        void gpsLocation(Location location);
    }

    public static void startGpsService(Context context, final GpsResult gpsResult) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                super.onLocationResult(locationResult);
                gpsResult.gpsLocation(locationResult.getLastLocation());
            }
        };
        mFusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //
            }
        });
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public static void stopGps(){
        LogUtil.loge("stopGps");
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public static int getDistanceFromLatLonMeters(double lat1, double lon1, double lat2, double lon2) {
//        LogUtil.loge("lat1: "+lat1);
//        LogUtil.loge("lng1: "+lon1);
//        LogUtil.loge("lat2: "+lat2);
//        LogUtil.loge("lng2: "+lon2);
//        double R = 6371; // Radius of the earth in km
//        double dLat = deg2rad(lat2-lat1);  // deg2rad below
//        double dLon = deg2rad(lon2-lon1);
//        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        double d = R * c; // Distance in km
//        return (int)Math.abs(d/1000);
        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);
        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);
        float distance = locationA.distanceTo(locationB);
        return Math.abs((int)distance);
    }

    public static double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
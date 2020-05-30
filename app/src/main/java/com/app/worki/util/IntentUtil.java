package com.app.worki.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import java.io.File;
import java.lang.reflect.Method;

public class IntentUtil {

    private void sendSms(Context context, String number) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + number));
        context.startActivity(sendIntent);
    }

    public static void uninstallApp(Activity activity, String packageName){
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);
    }

    public static void openUrlInBrowser(Context context, String url){
        if(url.startsWith("http")){
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(i);
        }
    }

    public static void openUrlInChrome(Activity activity, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            activity.startActivity(intent);
        }
        catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            activity.startActivity(intent);
        }
    }

    public static void openApp(Activity activity, String packageName){
        Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            activity.startActivity(launchIntent);
        }
    }

    public static void openDocument(Activity activity, String path) {
        uriFix();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(path);
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimetype == null) {
            intent.setDataAndType(Uri.fromFile(file), "text/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimetype);
        }
        activity.startActivity(Intent.createChooser(intent, "Choose an Application:"));
    }

    public static void openExcelFile(Activity activity, String path) {
        uriFix();
        Uri uri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            activity.startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "No Application Available to View Excel.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareImage(Activity activity, String path) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + path));
        activity.startActivity(Intent.createChooser(share, "Share"));
    }

    public static void shareText(Activity activity, String title, String text){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(i, "Share"));
    }

    private static void uriFix(){
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


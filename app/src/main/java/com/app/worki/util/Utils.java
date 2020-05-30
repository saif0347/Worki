package com.app.worki.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.text.InputType;
import android.widget.AbsListView;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.weiwangcn.betterspinner.library.BetterSpinner;

public class Utils {

    @NonNull
    public static String txt(EditText editText){
        return editText.getText().toString();
    }

    public static String getPhotoName(){
        return (10000 + new Random().nextInt(10000) - new Random().nextInt(10000)) + ".png";
    }

    public static String getRandomNumber(){
        return (1000000 + new Random().nextInt(1000000) - new Random().nextInt(1000000))+"";
    }

    @NonNull
    public static String txt(TextView textView){
        return textView.getText().toString();
    }

    @NonNull
    public static String txt(Button button){
        return button.getText().toString();
    }

    public static int len(EditText editText){
        return editText.getText().toString().length();
    }

    public static int len(TextView textView){
        return textView.getText().toString().length();
    }

    public static int len(Button button){
        return button.getText().toString().length();
    }

    @SuppressLint("MissingPermission")
    public static void vibratePhone(Activity activity){
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }

    public interface TextWatch{
        void textChanged(String text);
    }

    public static void setTextWatcher(final EditText editText, final TextWatch textWatch){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                textWatch.textChanged(editable.toString());
            }
        });
    }

    public static ArrayAdapter<String> setSpinnerData(Activity activity, BetterSpinner spinner, String [] data){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, data);
        spinner.setAdapter(adapter);
        return adapter;
        /*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, spinnerList);
        spinner.setAdapter(adapter);

        custom_spinner.xml >>
        <?xml version="1.0" encoding="utf-8"?>
        <TextView
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="13sp"
            android:gravity="left"
            android:text="Hello"
            android:padding="5dip" />
        */
    }

    public interface SearchClick{
        void search(String text);
    }
    //android:imeOptions="actionSearch"
    public static void searchWithKeyboardButton(final Activity activity, final EditText editText, final SearchClick searchClick){
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(editText.getText().toString().isEmpty())
                        return true;
                    else{
                        String text = editText.getText().toString();
                        searchClick.search(text);
                        hideKeyboard(activity);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static void scrollToTop(final ScrollView scrollView){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
            }
        });
    }

    public static String roundDouble(double num){
        return String.format(Locale.getDefault(), "%.2f", num);
    }

    public static String readTextFileFromAssets(Context context, String fileName){
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String mLine = reader.readLine();
            while (mLine != null) {
                if (!mLine.isEmpty()) {
                    builder.append(reader.readLine());
                    builder.append("\n");
                }
            }
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getHugeNumberFormat(int number){
        String s = "";
        int n = 0;
        if(number < 1000){
            return ""+number;
        }
        else if(number >= 1000 && number < 1000000) {
            s = "" + (number / 1000) + "K";
            n = number % 1000;
            if(n == 0)
                return s;
            return n + s;
        }
        else{
            s = "" + (number / 1000000) + "M";
            n = number % 1000000;
            if(n == 0)
                return s;
            return n + s;
        }
    }

    public static String getMd5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String getCountryName(Context context){
        String country;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            country = context.getResources().getConfiguration().getLocales().get(0).getDisplayCountry();
        } else {
            country = context.getResources().getConfiguration().locale.getDisplayCountry();
        }
        return country;
    }

    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static void deleteDirectoryWithFiles(String folderPath){
        File file = new File(folderPath);
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String aChildren : children) {
                new File(file, aChildren).delete();
            }
        }
    }

    public static Typeface createTypeface(Activity activity, String fontPath) {
        return Typeface.createFromAsset(activity.getAssets(), fontPath);
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    public static boolean checkExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static void changeImageViewColor(Activity activity, ImageView imageView, int color){
        DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(activity, color));
    }


    public static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public static void clickEffect(final View view){
        view.setAlpha(0.5f);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(1f);
            }
        }, 100);
    }

    public static boolean isPackageExisted(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }

    public static String[] getArrayFromList(ArrayList<String> list) {
        if(list.size() > 0){
            String[] array = new String[list.size()];
            for(int i = 0; i<list.size(); i++){
                array[i] = list.get(i);
            }
            return array;
        }
        return new String[0];
    }

    public static ArrayList<String> getListFromArray(String[] array) {
        ArrayList<String> list = new ArrayList<>();
        if(array.length > 0){
            list.addAll(Arrays.asList(array));
        }
        return list;
    }

    public static int getScreenWidth(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static void hideKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideNavigation(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public static void setScreenOnFlags(Activity activity){
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static int convertPixelToDp(int px, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px * scale + 0.5f);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Log.e("map", pair.getKey() + ":" + pair.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public static void showPassword(EditText password){
        password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        password.setSelection(password.length());
    }

    public static void hidePassword(EditText password){
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSelection(password.length());
    }

    public static void scrollToListviewPosition(final AbsListView view, final int position) {
        View child = getChildAtPosition(view, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !view.canScrollVertically(1)))) {
            return;
        }
        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    view.setOnScrollListener(null);
                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }
            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
            }
        });
        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.smoothScrollToPositionFromTop(position, 0);
            }
        });
    }

    public static View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }

    public static String getTimeAgo(long seconds) {
        if(seconds < 0){
            return "0s ago";
        } else if (seconds < 60) {
            return seconds + "s ago";
        } else if (seconds < 3600) {
            return "" + (seconds / 60) + "m ago";
        } else if (seconds < 86400) {
            return "" + (seconds / 3600) + "h ago";
        } else if (seconds < 604800) {
            return "" + (seconds / 86400) + "d ago";
        } else if (seconds < 2419200){
            return "" + (seconds / 604800) + "w ago";
        } else if(seconds < 29030400){
            return "" + (seconds / 2419200) + "M ago";
        } else {
            return "" + (seconds / 29030400) + "Y ago";
        }
    }

    public static Date getOnlyFutureTime(Date todayTime, Date azanTime_){
        Date azanTime = azanTime_;
        long diff = azanTime.getTime() - todayTime.getTime();
        if (diff < 0) {
            // Next Day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(azanTime);
            calendar.add(Calendar.DATE, 1);
            azanTime = calendar.getTime();
            return azanTime;
        } else {
            // Today
            return azanTime;
        }
    }
}

package com.app.worki;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class Transparent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transparent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED); // can be ignored
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // can be ignored
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); // can be ignored
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // can be ignored
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // this is required
        new Handler().postDelayed(() -> {
            finish();
        }, 100);
    }
}

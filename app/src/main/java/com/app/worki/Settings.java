package com.app.worki;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Settings extends AppCompatActivity {
    @BindView(R.id.lat)
    EditText lat;
    @BindView(R.id.lng)
    EditText lng;
    @BindView(R.id.radius)
    EditText radius;
    @BindView(R.id.update)
    Button update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.update)
    public void onViewClicked() {
        Utils.clickEffect(update);
    }
}

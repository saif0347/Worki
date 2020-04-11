package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdminLogin extends AppCompatActivity {
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.login)
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login)
    public void onViewClicked() {
        Utils.clickEffect(login);
        Intent admin = new Intent(this, AdminHome.class);
        startActivity(admin);
    }
}

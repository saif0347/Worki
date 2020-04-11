package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Login extends AppCompatActivity {
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.admin)
    TextView admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
    }

    @OnClick({R.id.login, R.id.admin})
    public void onViewClicked(View view) {
        Utils.clickEffect(view);
        switch (view.getId()) {
            case R.id.login:
                Intent user = new Intent(this, UserHome.class);
                startActivity(user);
                break;
            case R.id.admin:
                Intent admin = new Intent(this, AdminLogin.class);
                startActivity(admin);
                break;
        }
    }
}

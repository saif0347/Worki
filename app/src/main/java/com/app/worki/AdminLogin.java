package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.model.AdminModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.PrefsUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.DocumentSnapshot;

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
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login)
    public void onViewClicked() {
        Utils.clickEffect(login);
        if (username.getText().toString().isEmpty()) {
            username.setError("Required");
            return;
        }
        if (password.getText().toString().isEmpty()) {
            password.setError("Required");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocData(FirestoreUtil.admin, FirestoreUtil.admin, new FirestoreUtil.LoadResult() {
            @Override
            public void success(DocumentSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                AdminModel model = snapshot.toObject(AdminModel.class);
                if (model != null) {
                    if (model.getUsername().equals(Utils.txt(username)) && model.getPassword().equals(Utils.txt(password))) {
                        PrefsUtil.setLogin(AdminLogin.this, true);
                        PrefsUtil.setUserType(AdminLogin.this, "admin");
                        PrefsUtil.setUsername(AdminLogin.this, model.getUsername());
                        Intent admin = new Intent(AdminLogin.this, AdminHome.class);
                        startActivity(admin);
                        finishAffinity();
                    }
                    else {
                        Toast.makeText(AdminLogin.this, "Wrong Info", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminLogin.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

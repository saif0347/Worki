package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.model.UserModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PrefsUtil;
import com.app.worki.util.Utils;
import com.app.worki.util.ValidUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

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
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    String pushToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        if (PrefsUtil.isLogin(this)) {
            if (PrefsUtil.getUserType(this).equals("admin")) {
                Intent adminHome = new Intent(this, AdminHome.class);
                startActivity(adminHome);
                finish();
            } else {
                Intent userHome = new Intent(this, UserHome.class);
                startActivity(userHome);
                finish();
            }
        }

        FirestoreUtil.getPushToken(this, new FirestoreUtil.FirebasePushToken() {
            @Override
            public void pushToken(String token) {
                pushToken = token;
            }
        });
    }

    @OnClick({R.id.login, R.id.admin})
    public void onViewClicked(View view) {
        Utils.clickEffect(view);
        switch (view.getId()) {
            case R.id.login:
                loginUser();
                break;
            case R.id.admin:
                Intent admin = new Intent(this, AdminLogin.class);
                startActivity(admin);
                break;
        }
    }

    private void loginUser() {
        if(username.getText().toString().isEmpty()){
            username.setError("Full name Required");
            return;
        }
        if(!ValidUtil.checkTextOnly(Utils.txt(username))){
            username.setError("Invalid full name");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocsFiltered(FirestoreUtil.users, "username", Utils.txt(username).toLowerCase(), new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                if(querySnapshot.size() == 0){
                    LogUtil.loge("does not exist: create user");
                    UserModel userModel = new UserModel();
                    userModel.setType("user");
                    userModel.setUsername(Utils.txt(username).toLowerCase());
                    userModel.setAdmin_name("");
                    userModel.setPhoto("photo.png");
                    userModel.setToken(pushToken);
                    userModel.setDate_added(""+System.currentTimeMillis());
                    userModel.setStatus(0);
                    userModel.setStatus_time("");
                    FirestoreUtil.addDoc(userModel, FirestoreUtil.users, new FirestoreUtil.AddDocResult() {
                        @Override
                        public void success(DocumentReference docRef) {
                            LogUtil.loge("created: login");
                            updateToken(docRef);
                            moveToHome(userModel);
                        }
                        @Override
                        public void fail(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, ""+error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    LogUtil.loge("exist: login");
                    for (QueryDocumentSnapshot snapshot : querySnapshot) {
                        updateToken(snapshot.getReference());
                        UserModel userModel = snapshot.toObject(UserModel.class);
                        moveToHome(userModel);
                    }
                }
            }
            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, ""+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateToken(DocumentReference reference) {
        PrefsUtil.setUserId(this, reference.getId());
        LogUtil.loge("update token");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", pushToken);
        FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.users, reference.getId(), new FirestoreUtil.AddUpdateResult() {
            @Override
            public void success() {
                LogUtil.loge("token updated");
            }
            @Override
            public void fail(String error) {
                Toast.makeText(Login.this, ""+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveToHome(UserModel userModel) {
        progressBar.setVisibility(View.VISIBLE);
        PrefsUtil.setLogin(Login.this, true);
        PrefsUtil.setUserType(Login.this, "user");
        PrefsUtil.setUsername(Login.this, userModel.getUsername());
        PrefsUtil.setPhoto(this, userModel.getPhoto());
        Intent user = new Intent(Login.this, UserHome.class);
        startActivity(user);
        finishAffinity();
    }
}

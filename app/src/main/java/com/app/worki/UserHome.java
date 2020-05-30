package com.app.worki;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.worki.adapter.MessagesAdapter;
import com.app.worki.model.LocationModel;
import com.app.worki.model.MessageModel;
import com.app.worki.util.AlarmUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LocationUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PopupUtil;
import com.app.worki.util.PrefsUtil;
import com.app.worki.util.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.grpc.okhttp.internal.Util;

public class UserHome extends AppCompatActivity {
    @BindView(R.id.noresult)
    TextView noresult;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    ArrayList<MessageModel> models = new ArrayList<>();
    MessagesAdapter messagesAdapter;
    String pushToken = "";
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        ButterKnife.bind(this);

        registerReceiver(Notification, new IntentFilter("Notification"));

        checkPermissions();
        AlarmUtil.setNextAlarm(this, true);

        loadSettings();

        LogUtil.loge("log: " + PrefsUtil.getLogs(this));

        FirestoreUtil.getPushToken(this, token -> {
            pushToken = token;
            LogUtil.loge("update token");
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("token", pushToken);
            FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.users, PrefsUtil.getUserId(UserHome.this), new FirestoreUtil.AddUpdateResult() {
                @Override
                public void success() {
                    LogUtil.loge("token updated");
                }

                @Override
                public void fail(String error) {
                }
            });
        });

        fab.setOnClickListener(v -> {
            Intent feedback = new Intent(UserHome.this, Feedback.class);
            startActivity(feedback);
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            // permission granted
            // check GPS enabled
            if (!LocationUtil.isGpsOn(this)) {
                LocationUtil.showGpsDisabledAlert(this, "Enable GPS", "Please enable GPS and keep it on.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadSettings() {
        FirestoreUtil.getDocData(FirestoreUtil.location, FirestoreUtil.location, new FirestoreUtil.LoadResult() {
            @Override
            public void success(DocumentSnapshot snapshot) {
                LocationModel model = snapshot.toObject(LocationModel.class);
                if (model != null) {
                    PrefsUtil.setLat(UserHome.this, model.getLat());
                    PrefsUtil.setLng(UserHome.this, model.getLng());
                    PrefsUtil.setRadius(UserHome.this, model.getRadius());
                }
            }

            @Override
            public void error(String error) {
            }
        });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocsFilteredDesc(FirestoreUtil.messages, "username", PrefsUtil.getUsername(this), "time", new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                progressBar.setVisibility(View.GONE);
                models.clear();
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    MessageModel model = snapshot.toObject(MessageModel.class);
                    models.add(model);
                }
                messagesAdapter = new MessagesAdapter(UserHome.this, models);
                listView.setAdapter(messagesAdapter);
                if (models.size() == 0) {
                    noresult.setVisibility(View.VISIBLE);
                } else {
                    noresult.setVisibility(View.GONE);
                }
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    Intent msgInfo = new Intent(UserHome.this, MessageDetails.class);
                    msgInfo.putExtra("model", models.get(position));
                    startActivity(msgInfo);
                });
            }

            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                PopupUtil.showAlertPopup(this, "Sign out", "Giving up on us?", new String[]{"Sign out", "Cancel"}, new PopupUtil.AlertPopup() {
                    @Override
                    public void positive(DialogInterface dialog) {
                        dialog.dismiss();
                        PopupUtil.showAlertPopup(UserHome.this, "Sign out", "It's ok, no hard feelings here. Just please, don't give up on yourself", new String[]{"OK"}, new PopupUtil.AlertPopup() {
                            @Override
                            public void positive(DialogInterface dialog) {
                                dialog.dismiss();
                                PrefsUtil.setLogin(UserHome.this, false);
                                PrefsUtil.setUserType(UserHome.this, "");
                                PrefsUtil.setUsername(UserHome.this, "");
                                PrefsUtil.setUserId(UserHome.this, "");
                                finishAffinity();
                                Intent login = new Intent(UserHome.this, Login.class);
                                startActivity(login);
                            }

                            @Override
                            public void negative(DialogInterface dialog) {
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void negative(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(Notification);
    }

    private BroadcastReceiver Notification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("onReceive", "Notification");
            loadData();
        }
    };
}

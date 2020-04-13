package com.app.worki;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import com.app.worki.model.MessageModel;
import com.app.worki.util.AlarmUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LocationUtil;
import com.app.worki.util.PrefsUtil;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserHome extends AppCompatActivity {
    @BindView(R.id.noresult)
    TextView noresult;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    ArrayList<MessageModel> models = new ArrayList<>();
    MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        ButterKnife.bind(this);

        registerReceiver(Notification, new IntentFilter("Notification"));

        checkPermissions();
        AlarmUtil.setNextAlarm(this, true);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        else{
            // permission granted
            // check GPS enabled
            if(!LocationUtil.isGpsOn(this)){
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
                if(models.size() == 0){
                    noresult.setVisibility(View.VISIBLE);
                }
                else{
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
            case R.id.profile:
                Intent profile = new Intent(this, EditProfile.class);
                startActivity(profile);
                return true;
            case R.id.logout:
                PrefsUtil.setLogin(UserHome.this, false);
                PrefsUtil.setUserType(UserHome.this, "");
                PrefsUtil.setUsername(UserHome.this, "");
                finishAffinity();
                Intent login = new Intent(UserHome.this, Login.class);
                startActivity(login);
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

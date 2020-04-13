package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.worki.adapter.UsersAdapter;
import com.app.worki.model.UserModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.PrefsUtil;
import com.app.worki.util.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminHome extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.noresult)
    TextView noresult;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    ArrayList<UserModel> models = new ArrayList<>();
    UsersAdapter usersAdapter;
    @BindView(R.id.settings)
    ImageView settings;
    @BindView(R.id.logout)
    ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setClickListeners();
    }

    private void setClickListeners() {
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                Intent settings = new Intent(AdminHome.this, Settings.class);
                startActivity(settings);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                PrefsUtil.setLogin(AdminHome.this, false);
                PrefsUtil.setUserType(AdminHome.this, "");
                PrefsUtil.setUsername(AdminHome.this, "");
                finishAffinity();
                Intent login = new Intent(AdminHome.this, Login.class);
                startActivity(login);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendMsg = new Intent(AdminHome.this, SendMessage.class);
                startActivity(sendMsg);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocs(FirestoreUtil.users, new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                progressBar.setVisibility(View.GONE);
                models.clear();
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    UserModel model = snapshot.toObject(UserModel.class);
                    models.add(model);
                }
                if(models.size() == 0)
                    noresult.setVisibility(View.VISIBLE);
                else
                    noresult.setVisibility(View.GONE);
                usersAdapter = new UsersAdapter(AdminHome.this, models);
                listView.setAdapter(usersAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent profile = new Intent(AdminHome.this, UserProfile.class);
                        profile.putExtra("model", models.get(position));
                        startActivity(profile);
                    }
                });
            }
            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}

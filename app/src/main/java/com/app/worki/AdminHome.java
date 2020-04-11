package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.worki.adapter.UsersAdapter;
import com.app.worki.model.UserModel;
import com.app.worki.util.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                Intent settings = new Intent(AdminHome.this, Settings.class);
                startActivity(settings);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent sendMsg = new Intent(AdminHome.this, SendMessage.class);
                startActivity(sendMsg);
            }
        });

        fillDummy();
        usersAdapter = new UsersAdapter(this, models);
        listView.setAdapter(usersAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent profile = new Intent(AdminHome.this, UserProfile.class);
                startActivity(profile);
            }
        });
    }

    private void fillDummy() {
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
        models.add(new UserModel());
    }
}

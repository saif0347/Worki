package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.adapter.MessagesAdapter;
import com.app.worki.model.MessageModel;

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

        fillDummy();
        messagesAdapter = new MessagesAdapter(this, models);
        listView.setAdapter(messagesAdapter);
    }

    private void fillDummy() {
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
        models.add(new MessageModel());
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
        }
        return super.onOptionsItemSelected(item);
    }
}

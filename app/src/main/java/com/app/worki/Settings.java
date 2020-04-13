package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.adapter.NotesAdapter;
import com.app.worki.adapter.TemplatesAdapter;
import com.app.worki.model.LocationModel;
import com.app.worki.model.NoteModel;
import com.app.worki.model.TemplateModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

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
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.add_template)
    Button addTemplate;
    ArrayList<TemplateModel> tempModels = new ArrayList<>();
    TemplatesAdapter templatesAdapter;
    @BindView(R.id.listView)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        loadTemplates();
    }

    public void loadTemplates() {
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocs(FirestoreUtil.templates, new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                progressBar.setVisibility(View.GONE);
                tempModels.clear();
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    TemplateModel model = snapshot.toObject(TemplateModel.class);
                    tempModels.add(model);
                }
                templatesAdapter = new TemplatesAdapter(Settings.this, tempModels);
                listView.setAdapter(templatesAdapter);
            }
            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });

        addTemplate.setOnClickListener(v -> {
            Utils.clickEffect(v);
            Intent addTemp = new Intent(Settings.this, AddTemplate.class);
            startActivity(addTemp);
        });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocData(FirestoreUtil.location, FirestoreUtil.location, new FirestoreUtil.LoadResult() {
            @Override
            public void success(DocumentSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                LocationModel model = snapshot.toObject(LocationModel.class);
                if (model != null) {
                    lat.setText(model.getLat());
                    lng.setText(model.getLng());
                    radius.setText(model.getRadius());
                }
            }

            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Settings.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.update)
    public void onViewClicked() {
        Utils.clickEffect(update);
        if (lat.getText().toString().isEmpty()) {
            lat.setError("Required");
            return;
        }
        if (lng.getText().toString().isEmpty()) {
            lng.setError("Required");
            return;
        }
        if (radius.getText().toString().isEmpty()) {
            radius.setError("Required");
            return;
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lat", Utils.txt(lat));
        hashMap.put("lng", Utils.txt(lng));
        hashMap.put("radius", Utils.txt(radius));
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.location, FirestoreUtil.location, new FirestoreUtil.AddUpdateResult() {
            @Override
            public void success() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Settings.this, "Location Updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Settings.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

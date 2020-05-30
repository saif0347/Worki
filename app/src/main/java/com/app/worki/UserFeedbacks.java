package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.adapter.FeedbackAdapter;
import com.app.worki.model.FeedbackModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.PrefsUtil;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserFeedbacks extends AppCompatActivity {
    @BindView(R.id.noresult)
    TextView noresult;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    ArrayList<FeedbackModel> models = new ArrayList<>();
    FeedbackAdapter feedbackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feedbacks);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.getDocs(FirestoreUtil.feedbacks, new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                progressBar.setVisibility(View.GONE);
                models.clear();
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    FeedbackModel model = snapshot.toObject(FeedbackModel.class);
                    models.add(model);
                }
                feedbackAdapter = new FeedbackAdapter(UserFeedbacks.this, models);
                listView.setAdapter(feedbackAdapter);
                if(models.size() == 0){
                    noresult.setVisibility(View.VISIBLE);
                }
                else{
                    noresult.setVisibility(View.GONE);
                }
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    Intent feedbackInfo = new Intent(UserFeedbacks.this, FeedbackDetails.class);
                    feedbackInfo.putExtra("model", models.get(position));
                    startActivity(feedbackInfo);
                });
            }
            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}

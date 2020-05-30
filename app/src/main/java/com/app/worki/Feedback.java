package com.app.worki;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.model.FeedbackModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.PrefsUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.DocumentReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Feedback extends AppCompatActivity {
    @BindView(R.id.title)
    EditText title;
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.send)
    Button send;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);

        send.setOnClickListener(v -> {
            Utils.clickEffect(v);
            if(title.getText().toString().isEmpty()){
                title.setError("Required");
                return;
            }
            if(message.getText().toString().isEmpty()){
                message.setError("Required");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            FeedbackModel model = new FeedbackModel();
            model.setUsername(PrefsUtil.getUsername(Feedback.this));
            model.setTitle(Utils.txt(title));
            model.setMessage(Utils.txt(message));
            model.setEmail(Utils.txt(email));
            model.setTime(""+System.currentTimeMillis());
            FirestoreUtil.addDoc(model, FirestoreUtil.feedbacks, new FirestoreUtil.AddDocResult() {
                @Override
                public void success(DocumentReference docRef) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Feedback.this, "Thank you for the feedback!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override
                public void fail(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Feedback.this, ""+error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

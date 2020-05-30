package com.app.worki;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.app.worki.model.FeedbackModel;
import com.app.worki.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedbackDetails extends AppCompatActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    FeedbackModel model;
    @BindView(R.id.username)
    TextView username;
    @BindView(R.id.email)
    TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_details);
        ButterKnife.bind(this);

        model = (FeedbackModel) getIntent().getSerializableExtra("model");

        username.setText(model.getUsername());
        title.setText(model.getTitle());
        message.setText(model.getMessage());
        email.setText(model.getEmail());
        if (model.getEmail().isEmpty()) {
            email.setVisibility(View.GONE);
        }

        email.setOnClickListener(v -> {
            Utils.clickEffect(v);
            if (!model.getEmail().isEmpty()){
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", model.getEmail(), null));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{model.getEmail()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send email"));
            }
        });
    }
}

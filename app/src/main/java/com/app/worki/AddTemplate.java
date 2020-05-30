package com.app.worki;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.app.worki.model.TemplateModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.DocumentReference;
import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddTemplate extends AppCompatActivity {
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.addNote)
    Button addNote;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.title)
    EditText title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_template);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.addNote)
    public void onViewClicked() {
        Utils.clickEffect(addNote);
        if(title.getText().toString().isEmpty()){
            title.setError("Required");
            return;
        }
        if (message.getText().toString().isEmpty()) {
            message.setError("Required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        TemplateModel model = new TemplateModel();
        model.setId("");
        model.setTitle(Utils.txt(title));
        model.setText(Utils.txt(message));
        FirestoreUtil.addDoc(model, FirestoreUtil.templates, new FirestoreUtil.AddDocResult() {
            @Override
            public void success(DocumentReference docRef) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddTemplate.this, "Template Added!", Toast.LENGTH_SHORT).show();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", docRef.getId());
                docRef.update(hashMap);
                finish();
            }

            @Override
            public void fail(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddTemplate.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

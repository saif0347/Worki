package com.app.worki;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.model.NoteModel;
import com.app.worki.model.UserModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddNote extends AppCompatActivity {
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.addNote)
    Button addNote;
    UserModel userModel;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        ButterKnife.bind(this);

        userModel = (UserModel) getIntent().getSerializableExtra("model");
    }

    @OnClick(R.id.addNote)
    public void onViewClicked() {
        Utils.clickEffect(addNote);
        if (message.getText().toString().isEmpty()) {
            message.setError("Required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        NoteModel model = new NoteModel();
        model.setId("");
        model.setNote(Utils.txt(message));
        model.setUsername(userModel.getUsername());
        model.setTime("" + System.currentTimeMillis());
        FirestoreUtil.addDoc(model, FirestoreUtil.notes, new FirestoreUtil.AddDocResult() {
            @Override
            public void success(DocumentReference docRef) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddNote.this, "Note Added!", Toast.LENGTH_SHORT).show();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", docRef.getId());
                docRef.update(hashMap);
                finish();
            }

            @Override
            public void fail(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddNote.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

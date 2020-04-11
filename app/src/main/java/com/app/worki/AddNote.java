package com.app.worki;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddNote extends AppCompatActivity {
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.addNote)
    Button addNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.addNote)
    public void onViewClicked() {
        Utils.clickEffect(addNote);
    }
}

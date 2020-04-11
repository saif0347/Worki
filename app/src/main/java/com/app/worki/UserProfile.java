package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.adapter.MessagesAdapter;
import com.app.worki.adapter.NotesAdapter;
import com.app.worki.model.MessageModel;
import com.app.worki.model.NoteModel;
import com.app.worki.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {
    @BindView(R.id.photo)
    CircleImageView photo;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.add_note)
    Button addNote;
    @BindView(R.id.listView)
    ListView listView;
    ArrayList<MessageModel> messageModels = new ArrayList<>();
    MessagesAdapter messagesAdapter;
    ArrayList<NoteModel> notesModels = new ArrayList<>();
    NotesAdapter notesAdapter;
    @BindView(R.id.send_msg)
    Button sendMsg;
    @BindView(R.id.messages)
    Button messages;
    @BindView(R.id.notes)
    Button notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(0);
            }
        });
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clickEffect(v);
                Intent sendMessage = new Intent(UserProfile.this, SendMessage.class);
                startActivity(sendMessage);
            }
        });
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(1);
            }
        });

        setSelection(0);
    }

    private void setSelection(int position) {
        if(position == 0){
            messages.setBackground(getResources().getDrawable(R.drawable.btn_sel));
            messages.setTextColor(getResources().getColor(R.color.white));
            notes.setBackground(getResources().getDrawable(R.drawable.btn_unsel));
            notes.setTextColor(getResources().getColor(R.color.colorPrimary));
            sendMsg.setVisibility(View.VISIBLE);
            addNote.setVisibility(View.GONE);
            loadMessages();
        }
        else{
            messages.setBackground(getResources().getDrawable(R.drawable.btn_unsel));
            messages.setTextColor(getResources().getColor(R.color.colorPrimary));
            notes.setBackground(getResources().getDrawable(R.drawable.btn_sel));
            notes.setTextColor(getResources().getColor(R.color.white));
            sendMsg.setVisibility(View.GONE);
            addNote.setVisibility(View.VISIBLE);
            loadNotes();
        }
    }

    private void loadNotes() {
        fillDummyNotes();
        notesAdapter = new NotesAdapter(this, notesModels);
        listView.setAdapter(notesAdapter);
    }

    private void loadMessages() {
        fillDummyMessages();
        messagesAdapter = new MessagesAdapter(this, messageModels);
        listView.setAdapter(messagesAdapter);
    }

    private void fillDummyMessages() {
        messageModels.add(new MessageModel());
        messageModels.add(new MessageModel());
        messageModels.add(new MessageModel());
        messageModels.add(new MessageModel());
        messageModels.add(new MessageModel());
        messageModels.add(new MessageModel());
    }


    private void fillDummyNotes() {
        notesModels.add(new NoteModel());
        notesModels.add(new NoteModel());
        notesModels.add(new NoteModel());
        notesModels.add(new NoteModel());
        notesModels.add(new NoteModel());
    }

    @OnClick(R.id.add_note)
    public void onViewClicked() {
        Utils.clickEffect(addNote);
        Intent addNote = new Intent(this, AddNote.class);
        startActivity(addNote);
    }
}

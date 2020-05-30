package com.app.worki;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.adapter.MessagesAdapter;
import com.app.worki.adapter.NotesAdapter;
import com.app.worki.model.MessageModel;
import com.app.worki.model.NoteModel;
import com.app.worki.model.UserModel;
import com.app.worki.util.FirebaseStorageUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    UserModel userModel;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    int selection = 0;
    @BindView(R.id.edit)
    Button edit;
    @BindView(R.id.admin_name)
    TextView adminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        userModel = (UserModel) getIntent().getSerializableExtra("model");

        edit.setOnClickListener(v -> {
            Utils.clickEffect(v);
            Intent edit = new Intent(UserProfile.this, EditProfile.class);
            edit.putExtra("model", userModel);
            startActivity(edit);
        });

        messages.setOnClickListener(v -> setSelection(0));
        notes.setOnClickListener(v -> setSelection(1));
        sendMsg.setOnClickListener(v -> {
            Utils.clickEffect(v);
            Intent sendMessage = new Intent(UserProfile.this, SendMessage.class);
            sendMessage.putExtra("model", userModel);
            startActivity(sendMessage);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        setSelection(selection);
    }

    public void loadData() {
        name.setText(userModel.getUsername());
        if(!userModel.getAdmin_name().isEmpty()){
            adminName.setVisibility(View.VISIBLE);
            adminName.setText(userModel.getAdmin_name());
        }
        else{
            adminName.setVisibility(View.GONE);
        }

        if (userModel.getStatus() == 1) {
            status.setText(getResources().getString(R.string.active));
            status.setTextColor(getResources().getColor(R.color.green));
        } else {
            status.setText(getResources().getString(R.string.inactive));
            status.setTextColor(getResources().getColor(R.color.red));
        }
        if (!userModel.getPhoto().equals("photo.png"))
            FirebaseStorageUtil.showImage(this, photo, FirebaseStorageUtil.getStorageReference(new String[]{userModel.getUsername(), userModel.getPhoto()}));

        // refresh
        // find user
        FirestoreUtil.getDocsFiltered(FirestoreUtil.users, "username", userModel.getUsername(), new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    UserModel userModel = snapshot.toObject(UserModel.class);
                    if(!userModel.getAdmin_name().isEmpty()){
                        adminName.setVisibility(View.VISIBLE);
                        adminName.setText(userModel.getAdmin_name());
                    }
                    else{
                        adminName.setVisibility(View.GONE);
                    }
                    if (!userModel.getPhoto().equals("photo.png"))
                        FirebaseStorageUtil.showImage(UserProfile.this, photo, FirebaseStorageUtil.getStorageReference(new String[]{userModel.getUsername(), userModel.getPhoto()}));
                }
            }

            @Override
            public void error(String error) {
            }
        });
    }

    private void setSelection(int position) {
        selection = position;
        if (position == 0) {
            messages.setBackground(getResources().getDrawable(R.drawable.btn_sel));
            messages.setTextColor(getResources().getColor(R.color.white));
            notes.setBackground(getResources().getDrawable(R.drawable.btn_unsel));
            notes.setTextColor(getResources().getColor(R.color.colorPrimary));
            sendMsg.setVisibility(View.VISIBLE);
            addNote.setVisibility(View.GONE);
            loadMessages();
        } else {
            messages.setBackground(getResources().getDrawable(R.drawable.btn_unsel));
            messages.setTextColor(getResources().getColor(R.color.colorPrimary));
            notes.setBackground(getResources().getDrawable(R.drawable.btn_sel));
            notes.setTextColor(getResources().getColor(R.color.white));
            sendMsg.setVisibility(View.GONE);
            addNote.setVisibility(View.VISIBLE);
            loadNotes();
        }
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        if (messagesAdapter != null)
            listView.setAdapter(messagesAdapter);
        FirestoreUtil.getDocsFilteredDesc(FirestoreUtil.messages, "username", userModel.getUsername(), "time", new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                progressBar.setVisibility(View.GONE);
                messageModels.clear();
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    MessageModel model = snapshot.toObject(MessageModel.class);
                    messageModels.add(model);
                }
                messagesAdapter = new MessagesAdapter(UserProfile.this, messageModels);
                listView.setAdapter(messagesAdapter);
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    if (selection != 0)
                        return;
                    Intent msgInfo = new Intent(UserProfile.this, MessageDetails.class);
                    msgInfo.putExtra("model", messageModels.get(position));
                    startActivity(msgInfo);
                });
            }

            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void loadNotes() {
        progressBar.setVisibility(View.VISIBLE);
        if (notesAdapter != null)
            listView.setAdapter(notesAdapter);
        FirestoreUtil.getDocsFilteredDesc(FirestoreUtil.notes, "username", userModel.getUsername(), "time", new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                progressBar.setVisibility(View.GONE);
                notesModels.clear();
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    NoteModel model = snapshot.toObject(NoteModel.class);
                    notesModels.add(model);
                }
                notesAdapter = new NotesAdapter(UserProfile.this, notesModels);
                listView.setAdapter(notesAdapter);
                listView.setOnItemClickListener(null);
            }

            @Override
            public void error(String error) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @OnClick(R.id.add_note)
    public void onViewClicked() {
        Utils.clickEffect(addNote);
        Intent addNote = new Intent(this, AddNote.class);
        addNote.putExtra("model", userModel);
        startActivity(addNote);
    }
}

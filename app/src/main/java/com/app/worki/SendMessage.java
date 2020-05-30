package com.app.worki;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.model.MessageModel;
import com.app.worki.model.TemplateModel;
import com.app.worki.model.UserModel;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.SendPushUtil;
import com.app.worki.util.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendMessage extends AppCompatActivity {
    @BindView(R.id.sel_user)
    BetterSpinner selUser;
    @BindView(R.id.sel_message)
    BetterSpinner selMessage;
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.url)
    EditText url;
    @BindView(R.id.send)
    Button send;
    UserModel userModel;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    ArrayList<UserModel> userModels = new ArrayList<>();
    ArrayList<TemplateModel> templateModels = new ArrayList<>();
    @BindView(R.id.title)
    EditText title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        ButterKnife.bind(this);

        userModel = (UserModel) getIntent().getSerializableExtra("model");

        Utils.setSpinnerData(this, selUser, new String[]{"Loading"});
        Utils.setSpinnerData(this, selMessage, new String[]{"Loading"});

        loadUsers();
        loadTemplates();
    }

    private void loadUsers() {
        FirestoreUtil.getDocs(FirestoreUtil.users, new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                String[] users = new String[querySnapshot.size()];
                int i = 0;
                String selected = "";
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    UserModel model = snapshot.toObject(UserModel.class);
                    userModels.add(model);
                    users[i] = model.getUsername();
                    if (userModel != null) {
                        if (userModel.getUsername().equals(model.getUsername())) {
                            // selected
                            selected = model.getUsername();
                        }
                    }
                    i++;
                }
                Utils.setSpinnerData(SendMessage.this, selUser, users);
                selUser.setText(selected);
            }
            @Override
            public void error(String error) {
            }
        });
    }

    private void loadTemplates() {
        FirestoreUtil.getDocs(FirestoreUtil.templates, new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                String[] templates = new String[querySnapshot.size()];
                int i = 0;
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    TemplateModel model = snapshot.toObject(TemplateModel.class);
                    templateModels.add(model);
                    templates[i] = model.getTitle();
                    i++;
                }
                Utils.setSpinnerData(SendMessage.this, selMessage, templates);
                selMessage.setOnItemClickListener((parent, view, position, id) -> {
                    title.setText(templateModels.get(position).getTitle());
                    message.setText(templateModels.get(position).getText());
                });
            }
            @Override
            public void error(String error) {
            }
        });
    }

    @OnClick(R.id.send)
    public void onViewClicked() {
        Utils.clickEffect(send);
        String selectedUser = selUser.getText().toString();
        if (selectedUser.equals("Loading")) {
            return;
        }
        if(title.getText().toString().isEmpty()){
            title.setError("Required");
            return;
        }
        if (message.getText().toString().isEmpty()) {
            message.setError("Required");
            return;
        }

        for (UserModel model : userModels) {
            if (selectedUser.equals(model.getUsername())) {
                sendMessage(model);
                break;
            }
        }
    }

    private void sendMessage(UserModel model) {
        MessageModel messageModel = new MessageModel();
        messageModel.setId("");
        messageModel.setUsername(model.getUsername());
        messageModel.setTitle(Utils.txt(title));
        messageModel.setMsg(Utils.txt(message));
        messageModel.setUrl(Utils.txt(url));
        messageModel.setTime(""+System.currentTimeMillis());
        progressBar.setVisibility(View.VISIBLE);
        FirestoreUtil.addDoc(messageModel, FirestoreUtil.messages, new FirestoreUtil.AddDocResult() {
            @Override
            public void success(DocumentReference docRef) {
                progressBar.setVisibility(View.GONE);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", docRef.getId());
                docRef.update(hashMap);
                // send notification
                sendNotification(model, messageModel);
            }
            @Override
            public void fail(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SendMessage.this, ""+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(UserModel model, MessageModel messageModel) {
        Toast.makeText(SendMessage.this, "Message Sent!", Toast.LENGTH_SHORT).show();
        selUser.setText("");
        selMessage.setText("");
        title.setText("");
        message.setText("");
        url.setText("");

        JSONArray tokens = new JSONArray();
        tokens.put(model.getToken());
        JSONObject data = new JSONObject();
        try {
            data.put("title", messageModel.getTitle());
            data.put("message", messageModel.getMsg());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SendPushUtil.sendFirebasePush(this, data, tokens, new SendPushUtil.PushResult() {
            @Override
            public void success() {
                LogUtil.loge("push sent");
            }
            @Override
            public void fail(String message) {
                LogUtil.loge(""+message);
            }
        });
    }
}

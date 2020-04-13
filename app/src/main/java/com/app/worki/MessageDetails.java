package com.app.worki;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.model.MessageModel;
import com.app.worki.util.IntentUtil;
import com.app.worki.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.okhttp.internal.Util;

public class MessageDetails extends AppCompatActivity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.url)
    TextView url;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    MessageModel messageModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);
        ButterKnife.bind(this);

        messageModel = (MessageModel)getIntent().getSerializableExtra("model");

        title.setText(messageModel.getTitle());
        message.setText(messageModel.getMsg());
        url.setText(messageModel.getUrl());
        if(messageModel.getUrl().isEmpty()){
            url.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.url)
    public void onViewClicked() {
        Utils.clickEffect(url);
        if(!messageModel.getUrl().isEmpty())
            IntentUtil.openUrlInBrowser(this, messageModel.getUrl());
    }
}

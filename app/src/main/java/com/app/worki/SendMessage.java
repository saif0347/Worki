package com.app.worki;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.app.worki.util.Utils;
import com.weiwangcn.betterspinner.library.BetterSpinner;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        ButterKnife.bind(this);

        Utils.setSpinnerData(this, selUser, new String[]{"Loading"});
        Utils.setSpinnerData(this, selMessage, new String[]{"Loading"});
    }

    @OnClick(R.id.send)
    public void onViewClicked() {
        Utils.clickEffect(send);
    }
}

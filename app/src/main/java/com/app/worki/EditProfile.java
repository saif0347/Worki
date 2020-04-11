package com.app.worki;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.worki.util.ImageUtil;
import com.app.worki.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    @BindView(R.id.photo)
    CircleImageView photo;
    @BindView(R.id.browse)
    Button browse;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.update)
    Button update;
    private final int CAMERA = 100;
    private final int STORAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE);
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }

    @OnClick({R.id.browse, R.id.update})
    public void onViewClicked(View view) {
        Utils.clickEffect(view);
        switch (view.getId()) {
            case R.id.browse:
                ImageUtil.showSourcePopup(this);
                break;
            case R.id.update:
                break;
        }
    }

    String filePath;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bmp;
        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case ImageUtil.GALL_CODE:
                    bmp = ImageUtil.getGalleryImage(EditProfile.this, data);
                    if(bmp != null){
                        photo.setImageBitmap(bmp);
                        filePath = ImageUtil.filePath;
                    }
                    break;
                case ImageUtil.CAM_CODE:
                    bmp = ImageUtil.getCameraImage(EditProfile.this, data);
                    if(bmp != null){
                        photo.setImageBitmap(bmp);
                        filePath = ImageUtil.filePath;
                    }
                    break;
            }
        }
    }
}

package com.app.worki;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.worki.model.UserModel;
import com.app.worki.util.FirebaseStorageUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.Utils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

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
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    String docId;
    UserModel userModel;
    @BindView(R.id.admin_name)
    EditText adminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        userModel = (UserModel) getIntent().getSerializableExtra("model");

        checkPermissions();
        loadData();
    }

    private void loadData() {
        name.setText(userModel.getUsername());
        adminName.setText(userModel.getAdmin_name());
        if (!userModel.getPhoto().equals("photo.png"))
            FirebaseStorageUtil.showImage(this, photo, FirebaseStorageUtil.getStorageReference(new String[]{userModel.getUsername(), userModel.getPhoto()}));
        // find user docId
        FirestoreUtil.getDocsFiltered(FirestoreUtil.users, "username", userModel.getUsername(), new FirestoreUtil.LoadResultDocs() {
            @Override
            public void success(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot snapshot : querySnapshot) {
                    docId = snapshot.getId();
                }
            }
            @Override
            public void error(String error) {
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                //ImageUtil.showSourcePopup(this);
                ImagePicker.Companion.with(this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
                break;
            case R.id.update:
                uploadPhoto();
                break;
        }
    }

    private void uploadPhoto() {
        if (fileUri == null) {
            updateNameOnly();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        final String photoName = Utils.getPhotoName();
        FirebaseStorageUtil.uploadFile(fileUri, new String[]{userModel.getUsername(), photoName}, new FirebaseStorageUtil.UploadResult() {
            @Override
            public void uploadSuccess() {
                progressBar.setVisibility(View.GONE);
                // update photo name
                if (docId == null)
                    return;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("photo", photoName);
                hashMap.put("admin_name", Utils.txt(adminName));
                FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.users, docId, new FirestoreUtil.AddUpdateResult() {
                    @Override
                    public void success() {
                        Toast.makeText(EditProfile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                        String oldPhoto = userModel.getPhoto();
                        deleteOldPhoto(oldPhoto);
                    }
                    @Override
                    public void fail(String error) {
                        Toast.makeText(EditProfile.this, "" + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void uploadError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfile.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNameOnly() {
        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("admin_name", Utils.txt(adminName));
        FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.users, docId, new FirestoreUtil.AddUpdateResult() {
            @Override
            public void success() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void fail(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfile.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteOldPhoto(String oldPhoto) {
        if (oldPhoto == null)
            return;
        if (oldPhoto.isEmpty())
            return;
        FirebaseStorageUtil
                .getStorageReference(new String[]{userModel.getUsername(), oldPhoto})
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // File deleted successfully
                    LogUtil.loge("deleted");
                }).addOnFailureListener(exception -> {
            // Uh-oh, an error occurred!
            LogUtil.loge("did not delete");
        });
    }

    Uri fileUri = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            Uri uri = data.getData();
            photo.setImageURI(uri);
            fileUri = uri;
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            ImagePicker.Companion.getError(data);
            Toast.makeText(EditProfile.this, "Error: " + ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(EditProfile.this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void startCropper(Uri uri) {
        CropImage.activity(uri)
                .setMinCropResultSize(1000, 1000)
                .setMaxCropResultSize(1000, 1000)
                .start(this);
    }
}

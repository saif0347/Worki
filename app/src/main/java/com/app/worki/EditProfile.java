package com.app.worki;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.worki.util.FirebaseStorageUtil;
import com.app.worki.util.FirestoreUtil;
import com.app.worki.util.ImageUtil;
import com.app.worki.util.LogUtil;
import com.app.worki.util.PrefsUtil;
import com.app.worki.util.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        checkPermissions();
        loadData();
    }

    private void loadData() {
        name.setText(PrefsUtil.getUsername(this));
        if(!PrefsUtil.getPhoto(this).isEmpty())
            FirebaseStorageUtil.showImage(this, photo, FirebaseStorageUtil.getStorageReference(new String[]{PrefsUtil.getUsername(this), PrefsUtil.getPhoto(this)}));
        // find user docId
        FirestoreUtil.getDocsFiltered(FirestoreUtil.users, "username", PrefsUtil.getUsername(EditProfile.this), new FirestoreUtil.LoadResultDocs() {
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
                uploadPhoto();
                break;
        }
    }

    private void uploadPhoto() {
        if (fileUri == null)
            return;
        progressBar.setVisibility(View.VISIBLE);
        final String photoName = Utils.getPhotoName();
        FirebaseStorageUtil.uploadFile(fileUri, new String[]{PrefsUtil.getUsername(this), photoName}, new FirebaseStorageUtil.UploadResult() {
            @Override
            public void uploadSuccess() {
                progressBar.setVisibility(View.GONE);
                // update photo name
                if(docId == null)
                    return;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("photo", photoName);
                FirestoreUtil.addUpdateDoc(hashMap, FirestoreUtil.users, docId, new FirestoreUtil.AddUpdateResult() {
                    @Override
                    public void success() {
                        Toast.makeText(EditProfile.this, "Photo Updated!", Toast.LENGTH_SHORT).show();
                        String oldPhoto = PrefsUtil.getPhoto(EditProfile.this);
                        deleteOldPhoto(oldPhoto);
                        PrefsUtil.setPhoto(EditProfile.this, photoName);
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

    private void deleteOldPhoto(String oldPhoto) {
        if(oldPhoto == null)
            return;
        if(oldPhoto.isEmpty())
            return;
        FirebaseStorageUtil
                .getStorageReference(new String[]{PrefsUtil.getUsername(this), oldPhoto})
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        LogUtil.loge("deleted");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                LogUtil.loge("did not delete");
            }
        });
    }

    Uri fileUri = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bmp;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                case ImageUtil.GALL_CODE:
                    bmp = ImageUtil.getGalleryImage(EditProfile.this, data);
                    if (bmp != null) {
                        photo.setImageBitmap(bmp);
                        fileUri = ImageUtil.fileUri;
                    }
                    startCropper(fileUri);
                    break;

                case ImageUtil.CAM_CODE:
                    bmp = ImageUtil.getCameraImage(EditProfile.this, data);
                    if (bmp != null) {
                        photo.setImageBitmap(bmp);
                        fileUri = ImageUtil.fileUri;
                    }
                    startCropper(fileUri);
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if(result != null)
                        fileUri = result.getUri();

            }
        }
    }

    public void startCropper(Uri uri){
        CropImage.activity(uri)
                .setMinCropResultSize(1000, 1000)
                .setMaxCropResultSize(1000, 1000)
                .start(this);
    }
}

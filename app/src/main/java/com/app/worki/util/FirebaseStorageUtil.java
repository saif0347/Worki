package com.app.worki.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;

/*
    implementation 'com.google.firebase:firebase-storage:16.0.5'
    implementation 'com.firebaseui:firebase-ui-storage:0.6.0'
 */

public class FirebaseStorageUtil {
    private static final String storageUrl = "gs://worki-7b9bc.appspot.com/";

    private static FirebaseAuth getAuth(){
        return FirebaseAuth.getInstance();
    }

    private static FirebaseUser getUser(){
        return getAuth().getCurrentUser();
    }

    private static FirebaseStorage getStorage(){
        return FirebaseStorage.getInstance();
    }

    // url >> firebase storage url
    public static StorageReference getStorageReference(){
        return getStorage().getReferenceFromUrl(storageUrl);
    }

    // url >> firebase storage url
    // children [] >> folders and image name
    public static StorageReference getStorageReference(@NonNull String[] children){
        StorageReference storageReference = getStorage().getReferenceFromUrl(storageUrl);
        for (String child : children) {
            storageReference = storageReference.child(child);
        }
        return storageReference;
    }

    public interface UploadResult{
        void uploadSuccess();
        void uploadError(String error);
    }

    // url >> firebase storage url
    // children [] >> folders and image name
    public static void uploadFile(@NonNull Uri uri, String [] children, @NonNull final UploadResult uploadResult){
        final StorageReference storageReference;
        if(children == null)
            storageReference = getStorageReference();
        else
            storageReference = getStorageReference(children);

        UploadTask uploadTask = storageReference.putFile(uri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                uploadResult.uploadError(exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadResult.uploadSuccess();
                //updateImagePath(storageReference);
            }
        });
    }

    public static void updateUserImagePath(StorageReference storageReference){
        if(getUser() == null) {
            return;
        }
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(getUser().getDisplayName())
                .setPhotoUri(Uri.parse(storageReference.toString()))
                .build();
        getUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "User profile updated.");
                        }
                    }
                });
    }

    public static void showImage(Activity ctx, ImageView imageView, StorageReference storageReference){
        Glide.with(ctx)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .into(imageView);
    }

    public interface DownloadResult{
        void downloadSuccess();
        void downloadError(String error);
    }

    public static void downloadFile(@NonNull String outputFilePath, @NonNull StorageReference storageReference,
                                    @NonNull final DownloadResult downloadResult) {
        final File localFile = new File(outputFilePath);
        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ","local file created: " +localFile.toString());
                downloadResult.downloadSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ","error: " +exception.toString());
                downloadResult.downloadError(exception.getMessage());
            }
        });
    }

    public static void deleteFile(Context context, String [] children) {
        FirebaseStorageUtil
                .getStorageReference(children)
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
}

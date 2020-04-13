package com.app.worki.util;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import com.app.worki.MyApp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class FirestoreUtil {
    public static final String admin = "admin";
    public static final String location = "location";
    public static final String templates = "templates";
    public static final String users = "users";
    public static final String messages = "messages";
    public static final String notes = "notes";

    public interface FirebasePushToken{
        void pushToken(String token);
    }

    public static void getPushToken(Context context, final FirebasePushToken firebasePushToken){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener((Activity)context,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String pushToken = instanceIdResult.getToken();
                LogUtil.loge("pushToken: "+pushToken);
                firebasePushToken.pushToken(pushToken);
            }
        });
    }

    private static FirebaseAuth getAuth(){
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore getFirestore(){
        return FirebaseFirestore.getInstance();
    }

    public static boolean isLogin(){
        return getAuth().getCurrentUser() != null;
    }

    public static void logout(){
        if(isLogin())
            getAuth().signOut();
    }

    public static FirebaseUser getUser(){
        return getAuth().getCurrentUser();
    }

    public interface LoginResult{
        void onSuccess(FirebaseUser user);
        void onFail(String error);
    }

    public static void registerUser(@NonNull final String name, @NonNull final String email, @NonNull final String password,
                                    @NonNull final AddUpdateResult addUpdateResult){
        getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult() == null){
                                addUpdateResult.fail("result null");
                                return;
                            }
                            final FirebaseUser user = task.getResult().getUser();
                            if(user == null) {
                                addUpdateResult.fail("user null");
                                return;
                            }
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // send verification email
                                                user.sendEmailVerification();
                                                addUpdateResult.success();
                                            }
                                            else {
                                                addUpdateResult.fail("update name failed: deleting user");
                                                deleteUser(user, null);
                                            }
                                        }
                                    });
                        }
                        else {
                            addUpdateResult.fail("Unable to register! Maybe this user already exists.");
                        }
                    }
                });
    }

    public static void loginWithEmailPassword(@NonNull final String email, @NonNull final String password, @NonNull final LoginResult loginResult){
        getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            loginResult.onSuccess(getAuth().getCurrentUser());
                        }
                        else if(task.isCanceled()){
                            loginResult.onFail("Login Cancelled!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loginResult.onFail(e.getMessage());
                    }
                });
    }

    public static void loginWithCredentials(AuthCredential credential, final LoginResult loginResult){
        getAuth().signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        loginResult.onSuccess(authResult.getUser());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loginResult.onFail(e.getMessage());
                    }
                });
    }

    public interface LoadResult{
        void success(DocumentSnapshot snapshot);
        void error(String error);
    }

    public interface LoadResultDocs{
        void success(QuerySnapshot querySnapshot);
        void error(String error);
    }

    public static void getDocsFiltered(@NonNull String collection, String key, String value, @NonNull final LoadResultDocs loadResult){
        if(!NetworkUtil.isInternetOn()){
            loadResult.error("Network Problem!");
            return;
        }
        getFirestore().collection(collection)
                .whereEqualTo(key, value)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error("unable to get data");
                    }
                });
    }

    // 2 keys, 2 values
    public static void getDocsFiltered(@NonNull String collection, String[] keys, String[] values, @NonNull final LoadResultDocs loadResult){
        getFirestore().collection(collection)
                .whereEqualTo(keys[0], values[0])
                .whereEqualTo(keys[1], values[1])
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error("unable to get data");
                    }
                });
    }

    public static void getDocsFilteredDesc(@NonNull String collection, String key, String value, String orderBy, @NonNull final LoadResultDocs loadResult){
        if(!NetworkUtil.isInternetOn()){
            loadResult.error("Network Problem!");
            return;
        }
        getFirestore().collection(collection)
                .whereEqualTo(key, value)
                .orderBy(orderBy, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error(e.getMessage());
                    }
                });
    }

    // 2 keys, 2 values
    public static void getDocsFilteredDesc(@NonNull String collection, String[] keys, String[] values, String orderBy, @NonNull final LoadResultDocs loadResult){
        getFirestore().collection(collection)
                .whereEqualTo(keys[0], values[0])
                .whereEqualTo(keys[1], values[1])
                .orderBy(orderBy, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error(e.getMessage());
                    }
                });
    }

    public static void getDocsFilteredAsec(@NonNull String collection, String key, String value, String orderBy, @NonNull final LoadResultDocs loadResult){
        getFirestore().collection(collection)
                .whereEqualTo(key, value)
                .orderBy(orderBy, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error(e.getMessage());
                    }
                });
    }

    public static void getDocs(@NonNull String collection, @NonNull final LoadResultDocs loadResult){
        if(!NetworkUtil.isInternetOn()){
            loadResult.error("Network Problem!");
            return;
        }
        getFirestore().collection(collection).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error("unable to get data");
                    }
                });
    }

    public static void getDocs(@NonNull String[] colDocCol, @NonNull final LoadResultDocs loadResult){
        if(colDocCol.length < 3) {
            loadResult.error("colDocCol length must be >= 3");
            return;
        }
        if(colDocCol.length%2 == 0) {
            loadResult.error("colDocCol length must be Odd");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        colRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) {
                                loadResult.error("data null");
                                return;
                            }
                            loadResult.success(querySnapshot);
                        }
                        else{
                            loadResult.error("unable to get data");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadResult.error("unable to get data");
                    }
                });
    }

    public static void getDocData(@NonNull String collection, @NonNull String document, @NonNull final LoadResult loadResult){
        if(!NetworkUtil.isInternetOn()){
            loadResult.error("Network Problem!");
            return;
        }
        DocumentReference docRef = getFirestore().collection(collection).document(document);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document == null) {
                        loadResult.error("document null");
                        return;
                    }
                    if (document.exists()) {
                        loadResult.success(document);
                    }
                    else {
                        loadResult.error("document not exist");
                    }
                }
                else {
                    loadResult.error("unable to get data");
                }
            }
        });
    }

    public static void getDocData(@NonNull String[] colDocCol, @NonNull final LoadResult loadResult){
        if(colDocCol.length < 2) {
            loadResult.error("colDocCol invalid length");
            return;
        }
        if(colDocCol.length%2 != 0) {
            loadResult.error("colDocCol invalid length");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document == null) {
                        loadResult.error("document null");
                        return;
                    }
                    if (document.exists()) {
                        loadResult.success(document);
                    }
                    else {
                        loadResult.error("document not exist");
                    }
                }
                else {
                    loadResult.error("unable to get data");
                }
            }
        });
    }

    public static void getDocDataRealTime(@NonNull String collection, @NonNull String document, @NonNull final LoadResult loadResult){
        DocumentReference docRef = getFirestore().collection(collection).document(document);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    loadResult.error(e.getMessage());
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    loadResult.success(snapshot);
                }
                else {
                    loadResult.error("Data is null");
                }
            }
        });
    }

    //colDocCol >> collection, document, collection.....
    public static void getDocDataRealTime(@NonNull String [] colDocCol, @NonNull final LoadResult loadResult){
        if(colDocCol.length < 2) {
            loadResult.error("colDocCol invalid length");
            return;
        }
        if(colDocCol.length%2 != 0) {
            loadResult.error("colDocCol invalid length");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    loadResult.error(e.getMessage());
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    loadResult.success(snapshot);
                }
                else {
                    loadResult.error("Data is null");
                }
            }
        });
    }

    public static ArrayList<HashMap<String,String>> readData(Map<String, Object> data){
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        Set<String> keys = data.keySet();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.toArray()[i].toString();
            HashMap<String, String> entry = (HashMap<String, String>) data.get(key);
            list.add(entry);
        }
        return list;
    }

    public interface AddDocResult{
        void success(DocumentReference docRef);
        void fail(String error);
    }

    public interface AddUpdateResult{
        void success();
        void fail(String error);
    }

    // single entry document >> ROW behavior
    public static void addDoc(@NonNull Object object, @NonNull String collection, @NonNull final AddDocResult addResult){
        if(!NetworkUtil.isInternetOn()){
            addResult.fail("Network Problem!");
            return;
        }
        getFirestore()
                .collection(collection)
                .add(object)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        addResult.success(documentReference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addResult.fail(e.getMessage());
                    }
                });
    }

    // single entry document >> ROW behavior
    public static void addDoc(@NonNull Object object, @NonNull String[] colDocCol, @NonNull final AddDocResult addResult){
        if(colDocCol.length < 3) {
            addResult.fail("colDocCol length must be >= 3");
            return;
        }
        if(colDocCol.length%2 == 0) {
            addResult.fail("colDocCol length must be Odd");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        colRef.add(object)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        addResult.success(documentReference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addResult.fail(e.getMessage());
                    }
                });
    }

    // single entry document >> ROW behavior
    public static void addUpdateDoc(@NonNull Object object, @NonNull String collection, @NonNull String document,
                              @NonNull final AddUpdateResult addUpdateResult){
        getFirestore()
                .collection(collection)
                .document(document)
                .set(object, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    // single entry document >> ROW behavior
    public static void addUpdateDoc(@NonNull HashMap<String, Object> hashMap, @NonNull String collection, @NonNull String document,
                                    @NonNull final AddUpdateResult addUpdateResult){
        if(!NetworkUtil.isInternetOn()){
            addUpdateResult.fail("Network Problem!");
            return;
        }
        getFirestore()
                .collection(collection)
                .document(document)
                .update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    // single entry document >> ROW behavior
    public static void addUpdateDoc(@NonNull Object object, @NonNull String[] colDocCol, @NonNull final AddUpdateResult addUpdateResult){
        if(colDocCol.length < 2) {
            addUpdateResult.fail("colDocCol invalid length");
            return;
        }
        if(colDocCol.length%2 != 0) {
            addUpdateResult.fail("colDocCol invalid length");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        docRef.set(object, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    // multiple entry document >> TABLE behavior
    public static void addUpdateDocData(@NonNull LinkedHashMap<String, Object> hashMap, @NonNull String key,
                                     @NonNull String collection, @NonNull String document,
                                     @NonNull final AddUpdateResult addUpdateResult){
        LinkedHashMap<String, Object> parent = new LinkedHashMap<>();
        parent.put(key, hashMap);
        getFirestore().collection(collection)
                .document(document)
                .set(parent, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    // multiple entry document >> TABLE behavior
    public static void addUpdateDocData(@NonNull LinkedHashMap<String, Object> hashMap, @NonNull String key,
                                     @NonNull String [] colDocCol, @NonNull final AddUpdateResult addUpdateResult){
        LinkedHashMap<String, Object> parent = new LinkedHashMap<>();
        parent.put(key, hashMap);
        if(colDocCol.length < 2) {
            addUpdateResult.fail("colDocCol invalid length");
            return;
        }
        if(colDocCol.length%2 != 0) {
            addUpdateResult.fail("colDocCol invalid length");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        docRef.set(parent, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    public interface DeleteResult{
        void success();
        void fail(String error);
    }

    public static void deleteDocument(@NonNull String collection, @NonNull String document,
                                      @NonNull final DeleteResult deleteResult){
        if(!NetworkUtil.isInternetOn()){
            deleteResult.fail("Network Problem!");
            return;
        }
        getFirestore().collection(collection)
                .document(document)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteResult.fail(e.getMessage());
                    }
                });
    }

    public static void deleteDocument(@NonNull String [] colDocCol, @NonNull final DeleteResult deleteResult){
        if(colDocCol.length < 2) {
            deleteResult.fail("colDocCol invalid length");
            return;
        }
        if(colDocCol.length%2 != 0) {
            deleteResult.fail("colDocCol invalid length");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        docRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteResult.fail(e.getMessage());
                    }
                });
    }

    public static void deleteDocData(@NonNull String key, @NonNull String collection, @NonNull String document, @NonNull final DeleteResult deleteResult){
        LinkedHashMap<String, Object> entry = new LinkedHashMap<>();
        entry.put(key, FieldValue.delete());
        getFirestore().collection(collection)
                .document(document)
                .update(entry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteResult.fail(e.getMessage());
                    }
                });
    }

    public static void deleteDocData(@NonNull String key, @NonNull String [] colDocCol, @NonNull final DeleteResult deleteResult){
        LinkedHashMap<String, Object> entry = new LinkedHashMap<>();
        entry.put(key, FieldValue.delete());
        if(colDocCol.length < 2) {
            deleteResult.fail("colDocCol invalid length");
            return;
        }
        if(colDocCol.length%2 != 0) {
            deleteResult.fail("colDocCol invalid length");
            return;
        }
        CollectionReference colRef = getFirestore().collection(colDocCol[0]);
        DocumentReference docRef = colRef.document(colDocCol[1]);
        for (int i = 2; i < colDocCol.length; i++) {
            if(i%2 == 0){
                //collection
                colRef = docRef.collection(colDocCol[i]);
            }
            else{
                //document
                docRef = colRef.document(colDocCol[i]);
            }
        }
        docRef.update(entry)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        deleteResult.fail(e.getMessage());
                    }
                });
    }

    public static void updateName(@NonNull final String name, final AddUpdateResult addUpdateResult){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        getUser().updateProfile(profileUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(addUpdateResult != null)
                            addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(addUpdateResult != null)
                            addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    public static void updateEmail(@NonNull final String email, @NonNull String password, @NonNull final AddUpdateResult addUpdateResult){
        reLoginOrAuthenticate(password, new LoginResult() {
            @Override
            public void onSuccess(FirebaseUser user) {
                user.updateEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                addUpdateResult.success();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                addUpdateResult.fail(e.getMessage());
                            }
                        });
            }
            @Override
            public void onFail(String error) {
                addUpdateResult.fail(error);
            }
        });
    }

    public static void updatePassword(@NonNull final String password, @NonNull final AddUpdateResult addUpdateResult){
        reLoginOrAuthenticate(password, new LoginResult() {
            @Override
            public void onSuccess(FirebaseUser user) {
                user.updatePassword(password)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                addUpdateResult.success();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                addUpdateResult.fail(e.getMessage());
                            }
                        });
            }
            @Override
            public void onFail(String error) {
                addUpdateResult.fail(error);
            }
        });
    }

    private static void reLoginOrAuthenticate(String password, final LoginResult loginResult){
        if(getUser() == null) {
            loginResult.onFail("user is null");
            return;
        }
        if(getUser().getEmail() == null) {
            loginResult.onFail("user email is null");
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(getUser().getEmail(), password);
        getUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loginResult.onSuccess(getUser());
                } else{
                    loginResult.onFail("relogin failed");
                }
            }
        });
    }

    public static void resetPassword(@NonNull final String email, final AddUpdateResult addUpdateResult) {
        getAuth().sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(addUpdateResult != null)
                            addUpdateResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(addUpdateResult != null)
                            addUpdateResult.fail(e.getMessage());
                    }
                });
    }

    public static void deleteUser(FirebaseUser user, final DeleteResult deleteResult){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(null)
                .build();
        user.updateProfile(profileUpdates);
        user.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(deleteResult != null)
                            deleteResult.success();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(deleteResult != null)
                            deleteResult.fail(e.getMessage());
                    }
                });
    }
}

package com.example.eyalb.myapplication;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileManager {

    private final File path;
    private File currFolder;
    private String curr_date;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private String anonymousUid;

    private Context context;

    public fileManager(Context mContext) {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        path = new File(Environment.getExternalStorageDirectory(), "CarSensorsApp");

        context = mContext;
    }

    public void upload(final File file) {
        final Uri uri = Uri.fromFile(file);
        StorageReference sRef = mStorageRef.child(context.getString(R.string.version) + "/" + anonymousUid + "/" + curr_date + "/" + uri.getLastPathSegment());

        sRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                file.delete();
            }
        });
    }

    public void upload(final File file, String folderName) {
        final Uri uri = Uri.fromFile(file);
        StorageReference sRef = mStorageRef.child(context.getString(R.string.version) + "/" + anonymousUid + "/" + folderName + "/" + uri.getLastPathSegment());

        sRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                file.delete();
            }
        });
    }

    public void checkForSignedUser(Activity activity) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            anonymousUid = currentUser.getUid();
        else {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                anonymousUid = user.getUid();
                            }
                        }
                    })
                    .addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            anonymousUid = "Failed";
                        }
                    });
        }
    }

    public File getCurrFolder() {
        return currFolder;
    }

    public File getPath() {
        return path;
    }

    public boolean IsFolderEmpty(File folder) {
        if (folder.isDirectory()) {
            String[] files = folder.list();
            if (files.length == 0)
                return true;
        }
        return false;
    }

    public void deleteFolder(File folder) {
        folder.delete();
    }

    public void setCurrFolder() throws IOException {
        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        Date date = new Date();
        curr_date = df.format(date);
        currFolder = new File(path, curr_date);
        currFolder.mkdir();
    }
}

package com.example.eyalb.myapplication;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class fileManager {

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private String anonymousUid;

    public fileManager() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void upload(Uri file, String date) {

        StorageReference sRef = mStorageRef.child(anonymousUid + "/" + date + "/" + file.getLastPathSegment());

        sRef.putFile(file);
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
                            } else {
                                anonymousUid = "failed";
                            }
                        }
                    });
        }
    }
}

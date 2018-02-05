package app.SensoCar.eyalb.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileManager {

    private final File path;
    private File currFolder;
    private Date curr_date;

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
        MainActivity activity = (MainActivity) context;

        if(anonymousUid == null)
        {
            Toast.makeText(activity, "לא ניתן להעלות את קבצים לשרת מבלי למלא את הסקר הראשוני", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(activity, "לא ניתן להעלות את קבצים לשרת ללא חיבור לאינטרנט", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final Uri uri = Uri.fromFile(file);
        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        String date = df.format(curr_date);
        StorageReference sRef = mStorageRef.child(context.getString(R.string.version) + "/" + anonymousUid + "/" + date + "/" + uri.getLastPathSegment());

        sRef.putFile(uri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.setMessage("This might take a while...");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                file.delete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failed uploading file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void upload(final File file, String folderName) {
        MainActivity activity = (MainActivity) context;

        if(anonymousUid == null)
        {
            Toast.makeText(activity, "לא ניתן להעלות את קבצים לשרת מבלי למלא את הסקר הראשוני", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(activity, "לא ניתן להעלות את קבצים לשרת ללא חיבור לאינטרנט", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final Uri uri = Uri.fromFile(file);
        StorageReference sRef = mStorageRef.child(context.getString(R.string.version) + "/" + anonymousUid + "/" + folderName + "/" + uri.getLastPathSegment());

        sRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                file.delete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.setMessage("This might take a while...");
            }
        });
    }

    public boolean checkForSignedUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            anonymousUid = currentUser.getUid();


            if (path.exists())
                if (!IsFolderEmpty(path)) {
                    File[] folders = path.listFiles();
                    for (File folder : folders) {
                        String folderName = folder.getName();
                        if (folderName.equals("settings.txt")) {
                            upload(folder, "0");
                            continue;
                        }
                        File[] contents = folder.listFiles();
                        for (File content : contents) upload(content, folderName);
                        if (IsFolderEmpty(folder))
                            deleteFolder(folder);
                    }
                }


            if (isNetworkAvailable())
                settingsExists();

            return true;
        } else {
            return false;
        }
    }

    public boolean signUser(Activity activity, final Intent data) {
        if (data != null)
            mAuth.signInAnonymously()
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                anonymousUid = user.getUid();
                                uploadUserSettings(data);
                            }
                        }

                        private void uploadUserSettings(Intent data) {

                            String age = data.getStringExtra("age");
                            String years = data.getStringExtra("years");

                            try {

                                if (!path.exists())
                                    path.mkdir();

                                File doc = new File(path, "settings.txt");
                                doc.createNewFile();
                                FileOutputStream docStream = new FileOutputStream(doc);
                                OutputStreamWriter streamWriter = new OutputStreamWriter(docStream);

                                streamWriter.write("age: " + age + "\nyears: " + years);

                                streamWriter.close();

                                docStream.flush();

                                docStream.close();

                                upload(doc, "0");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            anonymousUid = "Failed";
                        }
                    });
        else
            return false;
        return true;
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
            if (files == null)
                return true;
            else if (files.length == 0)
                return true;
        }
        return false;
    }

    public void deleteFolder(File folder) {
        folder.delete();
    }

    public void setCurrFolder() throws IOException {
        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        curr_date = new Date();
        String date = df.format(curr_date);
        currFolder = new File(path, date);
        currFolder.mkdirs();
    }

    public Date getCurrDate() {
        return curr_date;
    }

    public void settingsExists() {
        mStorageRef.child(context.getString(R.string.version) + "/" + anonymousUid + "/0/settings.txt").getDownloadUrl()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        MainActivity activity = (MainActivity) context;
                        activity.setRedo(View.VISIBLE);
                    }
                });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

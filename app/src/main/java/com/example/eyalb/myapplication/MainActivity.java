package com.example.eyalb.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;

    private NotificationCompat.Builder builder;
    private static final int notification_id = 451234;

    private sensorManager sensorManager;
    private Property property;
    private fileManager fileManager;

    private location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);

        fileManager = new fileManager(this);
        sensorManager = new sensorManager(this, fileManager);
        property = new Property("media", fileManager);

        mLocation = new location(fileManager, (TextView) findViewById(R.id.textView2), this);

        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        stopButton.setEnabled(false);

        // Check if user is signed in (non-null) and update UI accordingly.
        fileManager.checkForSignedUser(this);

        if (fileManager.getPath().exists())
            if (!fileManager.IsFolderEmpty(fileManager.getPath())) {
                File[] folders = fileManager.getPath().listFiles();
                for (File folder : folders) {
                    String folderName = folder.getName();
                    File[] contents = folder.listFiles();
                    for (File content : contents) fileManager.upload(content, folderName);
                    if (fileManager.IsFolderEmpty(folder))
                        fileManager.deleteFolder(folder);
                }
            }
    }


    public boolean fileInit() {
        try {
            // Make sure the path directory exists.
            if (!fileManager.getPath().exists()) {
                // Make it, if it doesn't exit
                fileManager.getPath().mkdirs();
            }

            fileManager.setCurrFolder();

            if (!sensorManager.createFiles(fileManager.getCurrFolder()))
                return false;

            mLocation.createFiles(fileManager.getCurrFolder());

            return true;
        } catch (Exception e) {
            Log.d("Error", e.toString());
            return false;
        }
    }

    public boolean fileFin() {

        if (!sensorManager.closeFiles())
            return false;
        mLocation.closeFiles();

        return true;
    }


    public void onStartClick(View view) {

        if (!mLocation.getManager().isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        fileInit();

        sensorManager.register();
        property.startExamining(fileManager.getCurrFolder());
        mLocation.startExamining(this);

        stopButton.setEnabled(true);
        startButton.setEnabled(false);

    }

    public void onStopClick(View view) {

        property.stopExamining();
        mLocation.stopExamining();
        sensorManager.unregister();

        fileFin();

        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        Toast.makeText(getApplicationContext(), "Uploading started...", Toast.LENGTH_SHORT).show();

        sensorManager.upload();

        property.upload();

        mLocation.upload();

        if (fileManager.IsFolderEmpty(fileManager.getCurrFolder())) {
            fileManager.deleteFolder(fileManager.getCurrFolder());
        }

        Toast.makeText(getApplicationContext(), "Uploading complete.", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (startButton.isEnabled())
            return;

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("SensoCar is still recording");
        builder.setContentText("click here to resume to the application menu");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notification_id, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (startButton.isEnabled())
            return;

        property.stopExamining();
        mLocation.stopExamining();
        sensorManager.unregister();
        fileFin();

        Toast.makeText(getApplicationContext(), "Recording Stopped, Uploading data", Toast.LENGTH_SHORT).show();

        sensorManager.upload();

        property.upload();

        mLocation.upload();

        if (fileManager.IsFolderEmpty(fileManager.getCurrFolder())) {
            Toast.makeText(getApplicationContext(), "Uploading complete.", Toast.LENGTH_SHORT).show();
            fileManager.deleteFolder(fileManager.getCurrFolder());
        } else
            Toast.makeText(getApplicationContext(), "There is an error, Please try again later", Toast.LENGTH_LONG).show();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notification_id);

    }
}

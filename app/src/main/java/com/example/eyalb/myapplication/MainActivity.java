package com.example.eyalb.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;

    private NotificationCompat.Builder builder;
    private static final int notification_id = 451234;

    private sensorManager sensorManager;
    private Property property;
    private fileManager fileManager;

    private location mLocation;

    private File path = new File(Environment.getExternalStorageDirectory(), "CarSensorsApp");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);

        fileManager = new fileManager();
        sensorManager = new sensorManager(this, fileManager);
        property = new Property("media", fileManager);

        mLocation = new location(fileManager, this);

        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        stopButton.setEnabled(false);
    }


    public boolean fileInit() {
        try {
            // Make sure the path directory exists.
            if (!path.exists()) {
                // Make it, if it doesn't exit
                path.mkdirs();
            }

            if (!sensorManager.createFiles(path))
                return false;

            mLocation.createFiles();

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

        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        fileInit();

        sensorManager.register();
        property.startExamining(path);
        mLocation.startExamining(this);

    }

    public void onStopClick(View view) {

        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        Date date = new Date();
        String curr_date = df.format(date);

        property.stopExamining();
        mLocation.stopExamining();
        sensorManager.unregister();
        fileFin();


        Toast.makeText(getApplicationContext(), "Uploading started...", Toast.LENGTH_SHORT).show();

        sensorManager.upload(curr_date);

        property.upload(curr_date);

        mLocation.upload(curr_date);

        Toast.makeText(getApplicationContext(), "Uploading complete.", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        fileManager.checkForSignedUser(this);
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

        DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        Date date = new Date();
        String curr_date = df.format(date);

        property.stopExamining();
        mLocation.stopExamining();
        sensorManager.unregister();
        fileFin();

        Toast.makeText(getApplicationContext(), "Recording Stopped, Uploading data", Toast.LENGTH_SHORT).show();

        sensorManager.upload(curr_date);

        property.upload(curr_date);

        mLocation.upload(curr_date);

        Toast.makeText(getApplicationContext(), "Uploading complete", Toast.LENGTH_SHORT).show();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notification_id);

    }
}

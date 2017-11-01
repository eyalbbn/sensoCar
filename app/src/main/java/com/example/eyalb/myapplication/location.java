package com.example.eyalb.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class location implements LocationListener {

    private FileOutputStream outputStream;
    private OutputStreamWriter streamWriter;
    private File file;
    private fileManager fileManager;
    private LocationManager lMan;
    private Context mContext;
    private TextView speedometer;

    private Handler timeouthandler;

    public location(fileManager fMan, TextView view, Context c) {
        fileManager = fMan;
        mContext = c;
        speedometer = view;
        lMan = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        timeouthandler = new Handler();
    }

    public void createFiles(File path) {

        file = new File(path, "location.csv");

        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            streamWriter = new OutputStreamWriter(outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFiles() {
        try {
            streamWriter.close();

            outputStream.flush();

            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void upload() {
        fileManager.upload(file);
    }

    public void startExamining(Activity activity) {

        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.INTERNET, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO}, 11);

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Permission not granted", Toast.LENGTH_LONG).show();
        }

        lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 25, this);

    }

    public void stopExamining() {
        lMan.removeUpdates(this);
    }

    public LocationManager getManager() {
        return lMan;
    }


    @Override
    public void onLocationChanged(final Location location) {
        timeouthandler.removeCallbacksAndMessages(null);
        getSample(location);
        timeouthandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getSample(location);
            }
        }, 15000);
    }

    private void getSample(Location location) {
        try {
            float currentSpeed = location.getSpeed();
            streamWriter.write(location.getLatitude() + "," + location.getLongitude() + "," + currentSpeed + ",");
            speedometer.setText(currentSpeed * 3.6 + "km/h");
            DateFormat df = new SimpleDateFormat("dd/MM/yy");
            Date date = new Date();
            streamWriter.write(df.format(date));
            streamWriter.write(",");
            df = new SimpleDateFormat("HH:mm:ss");
            streamWriter.write(df.format(date));
            streamWriter.write("\n");

        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
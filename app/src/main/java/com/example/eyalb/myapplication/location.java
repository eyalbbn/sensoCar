package com.example.eyalb.myapplication;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class location implements LocationListener {

    private Uri uri;
    private FileOutputStream outputStream;
    private OutputStreamWriter streamWriter;
    private File file;
    private fileManager fileManager;
    private LocationManager lMan;
    private Context mContext;

    public location(fileManager fMan, Context c) {
        fileManager = fMan;
        mContext = c;
        lMan = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
    }

    public void createFiles(File path) {

        file = new File(path, "location.csv");

        uri = Uri.fromFile(file);
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
        if (!lMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
            return;
        }
        //enable internet toast
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


    @Override
    public void onLocationChanged(Location location) {
        try {
            streamWriter.write(location.getLatitude() + "," + location.getLongitude() + ",");
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
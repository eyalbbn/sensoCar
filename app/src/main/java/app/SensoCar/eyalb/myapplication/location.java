package app.SensoCar.eyalb.myapplication;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class location implements LocationListener {

    private FileOutputStream outputStream;
    private OutputStreamWriter streamWriter;
    private File file;
    private fileManager fileManager;
    private LocationManager lMan;
    private Context mContext;
    private Activity mActivity;
    private TextView speedometer;

    private Handler timeoutHandler;

    public location(fileManager fMan, TextView view, Context c) {
        fileManager = fMan;
        mContext = c;
        speedometer = view;
        lMan = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
        timeoutHandler = new Handler();
    }

    public void createFiles(File path) {

        file = new File(path, "location.csv");

        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            streamWriter = new OutputStreamWriter(outputStream);
            streamWriter = new OutputStreamWriter(outputStream);
            streamWriter.write("latitude,longitude,speed,elapsed");
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

        mActivity = activity;
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
        timeoutHandler.removeCallbacksAndMessages(null);
        getSample(location);
    }

    private void getSample(final Location location) {
        try {
            streamWriter.write("\n");
            float currentSpeed = location.getSpeed();
            streamWriter.write(location.getLatitude() + "," + location.getLongitude() + "," + currentSpeed * 3.6 + ",");
            speedometer.setText(Float.parseFloat(String.valueOf(currentSpeed * 3.6)) + " km/h");
            String time = calculateElapsed();
            streamWriter.write(time);
            timeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "Permission not granted", Toast.LENGTH_LONG).show();
                    }

                    lMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location mlocation) {

                            getSample(mlocation);
                        }

                        @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
                        @Override public void onProviderEnabled(String provider) { }
                        @Override public void onProviderDisabled(String provider) { }
                    }, null);
                }
            }, 8000);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    private String calculateElapsed() {
        Date date = new Date();
        long different = date.getTime() - fileManager.getCurrDate().getTime();

        long secondsInMilli = 1000;

        long elapsedSeconds = different / secondsInMilli;
        different = different % secondsInMilli;

        if(String.valueOf(different).length() == 1)
            return elapsedSeconds + ".00" + different;
        else if(String.valueOf(different).length() == 2)
            return elapsedSeconds + ".0" + different;
        return elapsedSeconds + "." + different;
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
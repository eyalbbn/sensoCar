package com.example.eyalb.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sensor {
    private String name;
    private int type;
    private Sensor s;
    private Uri uri;
    private FileOutputStream outputStream;
    private OutputStreamWriter streamWriter;
    private File file;

    sensor(String str, int t) {
        name = str;
        type = t;
    }

    void setSensor(SensorManager sMan) {
        s = sMan.getDefaultSensor(type);
    }

    boolean createFiles(File path) {
        try {
            file = new File(path, name + "Output.csv");

            uri = Uri.fromFile(file);
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            streamWriter = new OutputStreamWriter(outputStream);
            return true;
        } catch (Exception e) {
            Log.d("Error", e.toString());
            return false;
        }
    }

    void writeValues(SensorEvent event) {
        try {
            for (int i = 0; i < event.values.length; i++) {
                streamWriter.write(Float.toString(event.values[i]));
                streamWriter.write(",");
            }
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

    boolean closeFiles() {
        try {
            streamWriter.close();

            outputStream.flush();

            outputStream.close();

            return true;
        } catch (Exception r) {
            return false;
        }
    }


    File getFile() {
        return file;
    }

    Sensor getSensor() {
        return s;
    }

    int getType() {
        return type;
    }

}

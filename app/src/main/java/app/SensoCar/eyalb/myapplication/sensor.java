package app.SensoCar.eyalb.myapplication;

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
    private fileManager fileManager;

    sensor(String str, fileManager fMan, int t) {
        name = str;
        fileManager = fMan;
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
            streamWriter.write(name + "_x," + name + "_y," + name + "_z,");
            if (name == "rott")
                streamWriter.write(name + "_cos," + name + "_accuracy,");
            streamWriter.write("elapsed");
            return true;
        } catch (Exception e) {
            Log.d("Error", e.toString());
            return false;
        }
    }

    void writeValues(SensorEvent event) {
        try {
            streamWriter.write("\n");
            for (int i = 0; i < event.values.length; i++) {
                streamWriter.write(Float.toString(event.values[i]));
                streamWriter.write(",");
            }
            String time = calculateElapsed();
            streamWriter.write(time);
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

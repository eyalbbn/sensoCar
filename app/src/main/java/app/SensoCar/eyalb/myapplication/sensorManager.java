package app.SensoCar.eyalb.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.File;

public class sensorManager implements SensorEventListener {

    private static final int NUM_OF_SENSORS = 6;

    private Context mContext;

    private SensorManager sMan;
    private sensor[] sensors;

    private fileManager fileManager;

    public sensorManager(Context c, fileManager fMan) {
        mContext = c;
        fileManager = fMan;
        sensors = new sensor[NUM_OF_SENSORS];
        sensors[0] = new sensor("acce", fileManager, Sensor.TYPE_ACCELEROMETER);
        sensors[1] = new sensor("gyro", fileManager, Sensor.TYPE_GYROSCOPE);
        sensors[2] = new sensor("liac", fileManager, Sensor.TYPE_LINEAR_ACCELERATION);
        sensors[3] = new sensor("grav", fileManager, Sensor.TYPE_GRAVITY);
        sensors[4] = new sensor("rott", fileManager, Sensor.TYPE_ROTATION_VECTOR);
        sensors[5] = new sensor("magn", fileManager, Sensor.TYPE_MAGNETIC_FIELD);


        sMan = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            sensors[i].setSensor(sMan);
        }
    }

    public boolean createFiles(File path) {
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            if (!sensors[i].createFiles(path))
                return false;
        }
        return true;
    }

    public boolean closeFiles() {
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            if (!sensors[i].closeFiles())
                return false;
        }
        return true;
    }

    public void register() {
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            sMan.registerListener(this, sensors[i].getSensor(), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void unregister() {
        sMan.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for (int i = 0; i < NUM_OF_SENSORS; i++)
            if (event.sensor.getType() == sensors[i].getType())
                sensors[i].writeValues(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void upload() {
        for (int i = 0; i < NUM_OF_SENSORS; i++) {
            fileManager.upload(sensors[i].getFile());

        }
    }
}

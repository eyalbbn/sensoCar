package app.SensoCar.eyalb.myapplication;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.INTERNET, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_NETWORK_STATE}, 10);

        fileManager = new fileManager(this);
        sensorManager = new sensorManager(this, fileManager);
        property = new Property("media", fileManager);

        // Check if user is signed in (non-null) and update UI accordingly.
        if (!fileManager.checkForSignedUser()) {
            changeScreens(null);
        }

        setContentView(R.layout.activity_main);

        builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);

        mLocation = new location(fileManager, (TextView) findViewById(R.id.textView2), this);

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);

    }

    public void changeScreens(View view) {
        Intent intent = new Intent(this, FormActivity.class);
        final int result = 1;
        try {
            this.startActivityForResult(intent, result);
        } catch (Exception e) {
            e.printStackTrace();
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
            property.init(fileManager.getCurrFolder());

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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            fileInit();
        else
            return;

        fileInit();

        sensorManager.register();
        property.startExamining();
        mLocation.startExamining(this);

        stopButton.setEnabled(true);
        startButton.setEnabled(false);

    }

    public void onStopClick(View view) {

        property.stopExamining();
        mLocation.stopExamining();
        sensorManager.unregister();
        TextView tv = findViewById(R.id.textView2);
        tv.setText("0.0 km/h");

        fileFin();

        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        sensorManager.upload();

        property.upload();

        mLocation.upload();

        if (fileManager.IsFolderEmpty(fileManager.getCurrFolder())) {
            fileManager.deleteFolder(fileManager.getCurrFolder());
        }
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
        notificationManager.notify("SensoCar", notification_id, builder.build());
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

        Toast.makeText(getApplicationContext(), "ההקלטה נעצרה הקבצים יועלו בפעם הבאה שתתחברו", Toast.LENGTH_LONG).show();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel("SensoCar", notification_id);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (fileManager.signUser(this, data)) {
            setRedo(View.INVISIBLE);
            Toast.makeText(this, "הפרטים נשמרו בהצלחה", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setRedo(int visibility) {
        findViewById(R.id.redoForm).setVisibility(visibility);
    }
}

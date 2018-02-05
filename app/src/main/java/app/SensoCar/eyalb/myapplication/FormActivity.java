package app.SensoCar.eyalb.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class FormActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.form_screen);

    }

    public void sendFormData(View view) {
        if(!isNetworkAvailable()) {
            Toast.makeText(this,"עליך להיות מחובר לאינטרנט לפני שתמשיך",Toast.LENGTH_LONG).show();
            return;
        }

        EditText age = findViewById(R.id.age);
        String ageVal = age.getText().toString();
        EditText years = findViewById(R.id.years);
        String yearsVal = years.getText().toString();

        if (ageVal.isEmpty() || yearsVal.isEmpty()) {
            Toast.makeText(this, "אנא מלא את השדות באופן חוקי", Toast.LENGTH_LONG).show();
            return;
        }

        Intent goingBack = new Intent();
        goingBack.putExtra("age", ageVal);
        goingBack.putExtra("years", yearsVal);

        setResult(RESULT_OK, goingBack);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}


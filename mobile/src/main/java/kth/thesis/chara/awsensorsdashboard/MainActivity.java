package kth.thesis.chara.awsensorsdashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

public class MainActivity extends AppCompatActivity {

    private boolean connected = false;
    private long lastUpdate = System.currentTimeMillis();
    float[] ad = new float[3];
    float[] gd = new float[3];
    private MFPPush push;
    private MFPPushNotificationListener notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the SDK for Android
        BMSClient.getInstance().initialize(this, BMSClient.REGION_US_SOUTH);

        //Initialize client Push SDK for Java
        push = MFPPush.getInstance();
        push.initialize(getApplicationContext(), "<to be changed>", "<to be changed>");

        //Register Android devices
        push.registerDevice(new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                //handle success here
                push.listen(notificationListener);
            }
            @Override
            public void onFailure(MFPPushException ex) {
                //handle failure here
                push = null;
            }
        });

        //Handles the notification when it arrives
        notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive(final MFPSimplePushNotification message) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("AW Sensors Dashboard")
                                .setMessage(message.getAlert())
                                .show();
                    }
                });
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        startService(new Intent(this, SensorWearListener.class));

        MqttHandler.getInstance(this).connect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                connected = true;
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(push != null) {
            push.listen(notificationListener);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver(){
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getStringExtra("sensorType") == "android.sensor.accelerometer"){
                            ad = intent.getFloatArrayExtra("sensorValues");
                            ((TextView) MainActivity.this.findViewById(R.id.text_title1)).setText("Accelerometer [m/s2]");
                            ((TextView) MainActivity.this.findViewById(R.id.text_values1)).setText(
                                    Float.toString(ad[0]) + "\n" +
                                    Float.toString(ad[1]) + "\n" +
                                    Float.toString(ad[2]) + "\n");
                        }
                        else if (intent.getStringExtra("sensorType") == "android.sensor.gyroscope"){
                            gd = intent.getFloatArrayExtra("sensorValues");
                            ((TextView) MainActivity.this.findViewById(R.id.text_title2)).setText("Gyroscope [rad/s]");
                            ((TextView) MainActivity.this.findViewById(R.id.text_values2)).setText(
                                    Float.toString(gd[0]) + "\n" +
                                    Float.toString(gd[1]) + "\n" +
                                    Float.toString(gd[2]) + "\n");
                        }

                        long curTime = System.currentTimeMillis();
                        //Send value every 1 sec to the cloud
                        if (connected && (curTime - lastUpdate) > 1000){
                            lastUpdate = curTime;
                            MqttHandler.getInstance(MainActivity.this).publish(ad, gd);
                        }

                    }
                }, new IntentFilter("Sense")
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (push != null) {
            push.hold();
        }
    }

    @Override
    protected void onDestroy(){
        MqttHandler.getInstance(this).disconnect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                connected = false;
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });

        stopService(new Intent(this, SensorWearListener.class));
        super.onDestroy();
    }
}
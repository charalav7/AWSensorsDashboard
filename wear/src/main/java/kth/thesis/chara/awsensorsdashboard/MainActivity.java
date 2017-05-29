package kth.thesis.chara.awsensorsdashboard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MainActivity extends Activity {

    private TextView accelVal;
    private TextView gyroVal;
    private Button buttonStart;
    private Button buttonStop;
    private boolean connected = false;
    private long lastUpdate = System.currentTimeMillis();
    private boolean wearNotConnected = false;
    private float[] ad = new float[3];
    private float[] gd = new float[3];
    private int mqttCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                accelVal = (TextView) stub.findViewById(R.id.accelValues);
                gyroVal = (TextView) stub.findViewById(R.id.gyroValues);
                buttonStart = (Button) stub.findViewById(R.id.start);
                buttonStop = (Button) stub.findViewById(R.id.stop);
                buttonStart.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startService(new Intent(v.getContext(), SensorService.class));
                    }
                });
                buttonStop.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        stopService(new Intent(v.getContext(), SensorService.class));
                    }
                });

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver(){
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getStringExtra("sensorType") == "android.sensor.accelerometer"){
                            //float[] ad = intent.getFloatArrayExtra("sensorValues");
                            ad = intent.getFloatArrayExtra("sensorValues");
                            accelVal.setText(
                                    Float.toString(ad[0]) + "\n" +
                                    Float.toString(ad[1]) + "\n" +
                                    Float.toString(ad[2])
                            );
                        }
                        else if (intent.getStringExtra("sensorType") == "android.sensor.gyroscope"){
                            //float[] gd = intent.getFloatArrayExtra("sensorValues");
                            gd = intent.getFloatArrayExtra("sensorValues");
                            gyroVal.setText(
                                    Float.toString(gd[0]) + "\n" +
                                    Float.toString(gd[1]) + "\n" +
                                    Float.toString(gd[2])
                            );
                        }

                        /*//In case the watch is not connected to the phone and has wifi internet access, then send the data to cloud itself
                        if (isInternetConnected(getApplicationContext())){
                            isWearConnected();
                            if (wearNotConnected){
                                mqttCounter++;
                                if (mqttCounter == 1){
                                    mqttConnect();
                                }
                                long curTime = System.currentTimeMillis();
                                //Send value every 1 sec to the cloud
                                if (connected && (curTime - lastUpdate) > 1000){
                                    lastUpdate = curTime;
                                    MqttHandler.getInstance(MainActivity.this).publish(ad, gd);
                                }
                            }
                            else {
                                if (mqttCounter != 0){
                                    mqttCounter = 0;
                                    mqttDisconnect();
                                }
                            }
                        }
                        else {
                            if (mqttCounter != 0){
                                mqttCounter = 0;
                                mqttDisconnect();
                            }
                        }*/
                    }
                }, new IntentFilter("SensorBroadcast")
        );
    }

    /*public static boolean isInternetConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected())
        {
            return true;
        }

        return false;
    }

    public void isWearConnected(){
        try {
            getPackageManager().getPackageInfo("com.google.android.wearable.app", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            //android wear app is not installed
            wearNotConnected = true;
        }
    }

    public void mqttConnect(){
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

    public void mqttDisconnect(){
        MqttHandler.getInstance(this).disconnect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                connected = false;
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });
    }*/
}

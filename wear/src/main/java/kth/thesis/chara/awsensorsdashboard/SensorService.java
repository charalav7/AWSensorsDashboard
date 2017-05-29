package kth.thesis.chara.awsensorsdashboard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SensorService extends Service implements SensorEventListener {
    SensorManager sensorManager;
    private GoogleApiClient googleApiClient;
    private ExecutorService executorService;
    private Sensor accelSensor;
    private Sensor gyroSensor;
    private float gx=0;
    private float gy=0;
    private float gz=0;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            /*SensorEvent sensorEventA = event;
            // Isolate the force of gravity with the low-pass filter.
            gx = (float) 0.9 * gx + (float) 0.1* sensorEventA.values[0];
            gy = (float) 0.9 * gy + (float) 0.1* sensorEventA.values[1];
            gz = (float) 0.9 * gz + (float) 0.1* sensorEventA.values[2];

            // Remove the gravity contribution with the high-pass filter.
            sensorEventA.values[0] = sensorEventA.values[0] - gx;
            sensorEventA.values[1] = sensorEventA.values[1] - gy;
            sensorEventA.values[2] = sensorEventA.values[2] - gz;

            sendToWear(sensorEventA);
            sendToMobile(sensorEventA);*/

            sendToWear(event);
            sendToMobile(event);
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            sendToWear(event);
            sendToMobile(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    private boolean isConnected() {
        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext()).addApi(Wearable.API).build();

        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(15000, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    private void sendToMobile(final SensorEvent event) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                PutDataMapRequest dataMap = PutDataMapRequest.create("/sensor/" + event.sensor.getStringType());
                dataMap.getDataMap().putLong("timestamp", event.timestamp);
                dataMap.getDataMap().putFloatArray("value", event.values);
                PutDataRequest putDataRequest = dataMap.asPutDataRequest();
                //putDataRequest.setUrgent();
                if (isConnected()) {
                    Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.v(this.getClass().getName(), "Sending data: " + dataItemResult.getStatus().isSuccess());
                        }
                    });
                }
            }
        });
    }

    private void sendToWear(SensorEvent event){
        if (event != null) {
            Intent intent = new Intent("SensorBroadcast");
            intent.putExtra("sensorType", event.sensor.getStringType());
            intent.putExtra("sensorValues", event.values);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}

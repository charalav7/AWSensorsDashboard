package kth.thesis.chara.awsensorsdashboard;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class SensorWearListener extends WearableListenerService {

    private float[] valueA = new float[3];
    private float[] valueG = new float[3];

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(this.getClass().getName(), "onDataChanged()");
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();
                Intent intentS = new Intent("Sense");

                if (path.matches("/sensor/android.sensor.accelerometer")) {
                    DataMap map = DataMapItem.fromDataItem(dataItem).getDataMap();
                    //tA = map.getLong("timestamp");
                    valueA = map.getFloatArray("value");
                    intentS.putExtra("sensorValues", valueA);
                    intentS.putExtra("sensorType", "android.sensor.accelerometer");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentS);

                }
                else if (path.matches("/sensor/android.sensor.gyroscope")) {
                    DataMap map = DataMapItem.fromDataItem(dataItem).getDataMap();
                    //tG = map.getLong("timestamp");
                    valueG = map.getFloatArray("value");
                    intentS.putExtra("sensorValues", valueG);
                    intentS.putExtra("sensorType", "android.sensor.gyroscope");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentS);

                }
            }
        }
    }

}
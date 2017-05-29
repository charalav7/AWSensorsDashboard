package kth.thesis.chara.awsensorsdashboard;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHandler implements MqttCallback {

    private static MqttHandler instance;
    private MqttAndroidClient mqttClient;
    Context context;

    
    private static String ORG = "quickstart"; //to be changed from the corresponding user
    private static String DEVICE_TYPE = "<give Type>"; //also to be changed 
    private static String DEVICE_ID = "<give ID>"; //also to be changed 
    private static String TOKEN = "<give token>"; //to be changed
    private static String TOPIC = "iot-2/evt/hr/fmt/json";

    private MqttHandler(Context context) {
        this.context = context;
    }


    public static MqttHandler getInstance(Context context) {
        if (instance == null) {
            instance = new MqttHandler(context);
        }
        return instance;
    }


    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    public void connect(IMqttActionListener listener) {
        if (!isConnected()) {
            String iotPort = "1883";
            String iotHost = ORG+".messaging.internetofthings.ibmcloud.com";
            String iotClientId = "d:"+ORG+":"+DEVICE_TYPE+":"+DEVICE_ID;

            String connectionUri = "tcp://" + iotHost + ":" + iotPort;

            if (mqttClient != null) {
                mqttClient.unregisterResources();
                mqttClient = null;
            }

            mqttClient = new MqttAndroidClient(context, connectionUri, iotClientId);
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("use-token-auth");
            options.setPassword(TOKEN.toCharArray());

            try {
                mqttClient.connect(options, context, listener);
            } catch (MqttException e) {

            }
        }
    }

    public void disconnect(IMqttActionListener listener) {
        if (isConnected()) {
            try {
                mqttClient.disconnect(context, listener);
                mqttClient = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void publish(float[] accel, float[] gyro) {
        if (isConnected()) {
            String msg =
                    "{ \"d\": {" +
                            "\"AccelX\":" + accel[0] + ", " +
                            "\"AccelY\":" + accel[1] + ", " +
                            "\"AccelZ\":" + accel[2] + ", " +
                            "\"GyroX\":" + gyro[0] + ", " +
                            "\"GyroY\":" + gyro[1] + ", " +
                            "\"GyroZ\":" + gyro[2] + " " +
                            "} }";

            MqttMessage mqttMsg = new MqttMessage(msg.getBytes());
            mqttMsg.setRetained(false);
            mqttMsg.setQos(0);
            try {
                mqttClient.publish(TOPIC, mqttMsg);
            } catch (Exception e) {

            }
        }
    }

    private boolean isConnected() {
        if (mqttClient != null) {
            return mqttClient.isConnected();
        }
        return false;
    }
}

package com.example.wireless_noise_detection;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import org.eclipse.paho.android.service.MqttAndroidClient;
import info.mqtt.android.service.MqttAndroidClient;
import info.mqtt.android.service.Ack;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    // Adafruit.io feed credentials
    private static final String AIO_USERNAME = "";
    private static final String AIO_KEY = "";
    private static final String FEED_KEY = "";
    private static final String TOPIC = AIO_USERNAME + "/feeds/" + FEED_KEY;
    private static final String TOPIC_ALL = AIO_USERNAME + "/feeds/+";
    private static final String SERVER_URI = "ssl://io.adafruit.com:8883";
    
    private TextView messageText;
    private TextView warningText;
    private ProgressBar progressBar;
    private MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // finds the items that will be changed
        progressBar = findViewById(R.id.progressBar);
        messageText = findViewById(R.id.messageText);
        warningText = findViewById(R.id.warningText);

        progressBar.setMax(100);

        String clientId = "android-" + System.currentTimeMillis();
        client = new MqttAndroidClient(getApplicationContext(), SERVER_URI, clientId, Ack.AUTO_ACK);

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // this helps subscribe to the Adafruit.io feed
                subscribeTo(TOPIC);
                subscribeTo(TOPIC_ALL);
                requestLastValue();
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                final String text = (message != null ? message.toString() : "");

                runOnUiThread(() -> {
                    float dbValue;
                    try {
                        dbValue = Float.parseFloat(text);
                    } catch (Exception e) {
                        return;
                    }
                    // this puts together the text that will display the decibel values to the user
                    messageText.setText(text + " dB");

                    float minDb = 30f;
                    float maxDb = 80f;

                    float normalized = (dbValue - minDb) / (maxDb - minDb);
                    int progress = Math.round(normalized * 100f);
                    // this section helps create the logic for the progress bar
                    if (progress < 0) progress = 0;
                    if (progress > 100) progress = 100;

                    progressBar.setProgress(progress);

                    if (dbValue >= maxDb) {
                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        warningText.setText("Noise is too loud!");
                    } else {
                        progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        warningText.setText("");
                    }
                });
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                progressBar.setProgress(0);
            }
        });
        connectAndSubscribe();
    }
    // this helps connect and subscribe to the Adafruit.io feed
    // subscribing to the feed helps the app receive the values sent to the feed
    private void connectAndSubscribe() {
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setKeepAliveInterval(60);
        opts.setUserName(AIO_USERNAME);
        opts.setPassword(AIO_KEY.toCharArray());
        opts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        try {
            client.connect(opts, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (Exception e) {
        }
    }
    // this helps subscribe to the Adafruit.io feed
    private void subscribeTo(String topic) {
        try {
            client.subscribe(topic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken token) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (Exception e) {

        }
    }
    // this helps get the most recent value sent to the feed
    private void requestLastValue() {
        try {
            MqttMessage msg = new MqttMessage(new byte[0]);
            msg.setQos(1);
            client.publish(TOPIC + "/get", msg);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { client.unregisterResources(); } catch (Exception ignored) {}
        try { client.close(); } catch (Exception ignored) {}
    }
}
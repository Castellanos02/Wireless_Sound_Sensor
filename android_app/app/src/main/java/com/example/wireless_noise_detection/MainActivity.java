package com.example.wireless_noise_detection;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

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

        progressBar = findViewById(R.id.progressBar);
        messageText = findViewById(R.id.messageText);
        warningText = findViewById(R.id.warningText);

        progressBar.setMax(100);

        String clientId = "android-" + System.currentTimeMillis();
        client = new MqttAndroidClient(getApplicationContext(), SERVER_URI, clientId);

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
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

                    messageText.setText(text + " dB");

                    float minDb = 30f;
                    float maxDb = 80f;

                    float normalized = (dbValue - minDb) / (maxDb - minDb);  // 0.0 -> 1.0
                    int progress = Math.round(normalized * 100f);

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
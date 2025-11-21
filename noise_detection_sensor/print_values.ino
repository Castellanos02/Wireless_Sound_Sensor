#if !defined(ARDUINO_ARCH_ESP32)
  #error "This sketch targets ESP32/ESP32-S2. Select an ESP32-S2 board."
#endif

#include <Arduino.h>
#include <WiFi.h>
#include "Adafruit_MQTT.h"
#include "Adafruit_MQTT_Client.h"
#include <Adafruit_NeoPixel.h>

//Wi-Fi Credentials
const char* WIFI_SSID = "";
const char* WIFI_PASS = "";

//Adafruit.io credentials
#define AIO_SERVER      "io.adafruit.com"
#define AIO_SERVERPORT  1883
#define AIO_USERNAME    ""
#define AIO_KEY         ""
// "/feeds/{Add feed name}"
#define AIO_FEED_KEY    AIO_USERNAME "/feeds/"

//Pin where sounds sensor is connected on the ESP32-S2 Feather
const uint8_t PIN_SEN0232 = A0;

//Neopixel ring setup
#define LED_PIN       5
#define LED_COUNT     24
#define LED_BRIGHT    10
Adafruit_NeoPixel strip(LED_COUNT, LED_PIN, NEO_GRBW + NEO_KHZ800);

static const float ADC_COUNTS = 4095.0f;
static const float VREF_VOLTS = 3.3f;
const float EMA_ALPHA = 0.8f;
float db_ema = 0.0f;

// Setup for Neopixel ring
// what the lowest value is when the ring has a few lights on
float DB_MIN = 50.0f;
// what the hightest value is when all the lights come on
float DB_MAX = 80.0f;

// Helps set up the feature to show the peak and it reset as the values changes
uint16_t peak_idx = 0;
uint32_t last_peak_decay = 0;
const uint16_t PEAK_HOLD_MS = 300;
const uint16_t PEAK_DECAY_MS = 5;

// since we are using the free version of an adafruit.io feed
// we have to set a delay so the feed receives a value every 3 seconds and if the change in db is greater than 0.5
const uint32_t PUBLISH_INTERVAL_MS = 3000;
const float    MIN_DELTA_DB = 0.5f;

// Simple debug macro: prints only if Serial is active
#define DBGln(x) do { if (Serial) { Serial.println(x); } } while(0)
#define DBG(x)   do { if (Serial) { Serial.print(x); } } while(0)

// Wi-Fi and MQTT objects to help connect to feed
WiFiClient netClient;  // non-SSL
Adafruit_MQTT_Client mqtt(&netClient, AIO_SERVER, AIO_SERVERPORT, AIO_USERNAME, AIO_KEY);
Adafruit_MQTT_Publish feed_db_raw(&mqtt, AIO_FEED_KEY);

//helps accumalte readings so a time average value is sent rather than a single noisy one
uint32_t lastPub = 0;
float    accum_db = 0.0f;
uint16_t accum_n  = 0;
// helps compre the last average to the new average value
float    last_published = NAN;
// In case the value is rejected by the feed then we need to wait this specfic time
uint32_t throttle_release_at = 0;

// checks to see if the device is already connected to Wi-Fi, if so then it returns
// if not then it starts the proccess to connect to the Wi-Fi
void connectWiFi(uint32_t timeout_ms = 15000) {
  if (WiFi.status() == WL_CONNECTED) return;
  WiFi.mode(WIFI_STA);
  WiFi.setHostname("esp32s2-soundmeter");
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  DBG(F("Wi-Fi: connecting to ")); DBGln(WIFI_SSID);
  uint32_t start = millis();
  while (WiFi.status() != WL_CONNECTED && (millis() - start) < timeout_ms) {
    delay(250);
    DBG('.');
  }
  DBGln("");

  if (WiFi.status() == WL_CONNECTED) {
    DBG(F("Wi-Fi OK. IP: ")); DBGln(WiFi.localIP());
  } else {
    DBGln(F("Wi-Fi timeout; will retry in loop."));
  }
  WiFi.setAutoReconnect(true);
}

// checks to see if the device is already connected to the adafrui.io feed, if so then it returns
// if not then it starts the process to connect to the adafruit.io feed
void MQTT_connect() {
  if (mqtt.connected()) return;
  DBG(F("MQTT: connecting to ")); DBG(AIO_SERVER); DBG(F(" ... "));
  int8_t ret;
  uint8_t retries = 5;
  while ((ret = mqtt.connect()) != 0) {
    DBGln(mqtt.connectErrorString(ret));
    DBGln(F("Retrying MQTT in 2s..."));
    mqtt.disconnect();
    delay(2000);
    if (!--retries) return;
  }
  DBGln(F("connected."));
}

// helps with showing the correct color depending on the noise level value
static uint32_t colorFromFraction(float f) {
  f = constrain(f, 0.0f, 1.0f);
  uint8_t r, g, b;
  if (f < 0.5f) {
    // green -> yellow
    float t = f * 2.0f;
    r = (uint8_t)(t * 255);
    g = 255;
    b = 0;
  } else {
    // yellow -> red
    float t = (f - 0.5f) * 2.0f;
    r = 255;
    g = (uint8_t)((1.0f - t) * 255);
    b = 0;
  }
  return strip.Color(r, g, b, 0);
}

// helps light up the Neopixel ring right with the correct colors
void drawGauge(float db_val) {
  float f = (db_val - DB_MIN) / (DB_MAX - DB_MIN);
  f = constrain(f, 0.0f, 1.0f);

  int lit = (int)roundf(f * LED_COUNT);
  peak_idx = constrain((int)roundf(f * (LED_COUNT - 1)), 0, LED_COUNT - 1);
  strip.clear();

  for (int i = 0; i < LED_COUNT; i++) {
    if (i < lit) {
      float seg_f = (float)i / (LED_COUNT - 1);
      strip.setPixelColor(i, colorFromFraction(seg_f));
    }
  }

  strip.setPixelColor(peak_idx, strip.Color(0, 0, 0, 140));
  strip.show();
}


void setup() {
  Serial.begin(115200);
  uint32_t t0 = millis();
  while (!Serial && millis() - t0 < 1000) { delay(10); }

  analogReadResolution(12);
  pinMode(PIN_SEN0232, INPUT);

  for (int i = 0; i < 10; i++) { analogRead(PIN_SEN0232); delay(5); }

  strip.begin();
  strip.setBrightness(LED_BRIGHT);
  strip.show();

  connectWiFi();

  DBGln(F("SEN0232 Sound Level -> Serial + Adafruit IO + NeoPixel gauge"));
  DBGln(F("Columns: adc, volts, dB_raw, dB_EMA, wifi, mqtt"));
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi(5000);
  } else {
    MQTT_connect();
  }

  const int N = 8;
  long acc = 0;
  for (int i = 0; i < N; i++) {
    acc += analogRead(PIN_SEN0232);
    delayMicroseconds(250);
  }
  float adc   = acc / float(N);
  float volts = (adc / ADC_COUNTS) * VREF_VOLTS;
  float db_raw = 50.0f * volts; 
  db_ema = (EMA_ALPHA * db_raw) + ((1.0f - EMA_ALPHA) * db_ema);

  drawGauge(db_ema);

  accum_db += db_raw;
  accum_n++;

  if (Serial) {
    Serial.print(adc, 1);   Serial.print(',');
    Serial.print(volts, 3); Serial.print(',');
    Serial.print(db_raw,1); Serial.print(',');
    Serial.print(db_ema,1); Serial.print(',');
    Serial.print(WiFi.status() == WL_CONNECTED ? F("wifi_ok") : F("wifi_down")); Serial.print(',');
    Serial.println(mqtt.connected() ? F("mqtt_ok") : F("mqtt_down"));
  }

  if (millis() < throttle_release_at) {
    delay(25);
    mqtt.processPackets(5);
    mqtt.ping();
    return;
  }

  if (millis() - lastPub >= PUBLISH_INTERVAL_MS) {
    lastPub = millis();

    float avg_db = (accum_n > 0) ? (accum_db / accum_n) : db_raw;
    accum_db = 0.0f; accum_n = 0;

    avg_db = roundf(avg_db * 2.0f) / 2.0f;

    bool changed_enough = isnan(last_published) || fabsf(avg_db - last_published) >= MIN_DELTA_DB;

    if (WiFi.status() == WL_CONNECTED && mqtt.connected() && changed_enough) {
      if (!feed_db_raw.publish(avg_db)) {
        DBGln(F("MQTT publish failed (maybe throttled). Backing off 15s."));
        throttle_release_at = millis() + 15000;  // gentle backoff
      } else {
        last_published = avg_db;
        DBG(F("MQTT publish OK: ")); DBGln(avg_db);
      }
    }
  }

  mqtt.processPackets(10);
  mqtt.ping();

  delay(50);
}

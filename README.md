# Wireless Sound Sensor

This project aims to design and implement a wireless real-time noise detection system that monitors 
environmental sound levels and visualizes them on a live web dashboard and Android App.
It includes both a **Flask HTTP Server** for noise monitoring and an **Android App** for mobile visualization.

##  1. HTTP Flask Server (Noise Monitoring)

**Folder:** `http_flask_server/`

This component uses a **sound level sensor** connected to an **ESP32-S2** and publishes readings to **Adafruit IO**.  
A **Flask web app** polls the Adafruit REST API to visualize live noise levels using **Chart.js**.

###  Features
- Real-time noise level monitoring via HTTP polling  
- Visual alert when noise exceeds 85 dB  
- Adafruit IO integration for live data  
- Flask + Chart.js dashboard interface  

##  Setup Instructions

### 1. Clone this repository
```bash
git clone https://github.com/Castellanos02/Wireless_Sound_Sensor.git
cd Wireless_Sound_Sensor/http_flask_server
```

### 2. Install dependencies
```bash
pip install -r requirements.txt
```

### 3. Create a .env file (use your Adafruit credentials)
```bash
AIO_USERNAME=your_adafruit_username
AIO_KEY=your_adafruit_key
AIO_FEED=wireless
FLASK_ENV=development
```

### 4. Run the Flask server
```bash
python app.py
```

### 5. Open the web dashboard
After the server starts, open your browser and go to:  
 [http://127.0.0.1:5000](http://127.0.0.1:5000)

##  2. Android App (Mobile Visualization)

**Folder:** `android_app/`

This Android App was created to provide a real-time visulization of the sound levels that are sent to the Adafruit IO feed. It connects to the feed by using MQTT and displays the most recent value the feed has received.

###  Features
- Receive real-time noise levels from Adafruit IO feed
- Displays the most recent sound level
- Displays a progress bar to show how close the noise level is to an unsafe noise level
- Changes progress bar color depening on the sound level
- Warning message is displayed after the noise level exceeds 80.0 dB

##  Setup Instructions

### 1. Clone this repository
```bash
git clone https://github.com/Castellanos02/Wireless_Sound_Sensor.git
cd Wireless_Sound_Sensor/http_flask_server
```

### 2. Open folder in Android Studio
`android_app/`

### 3. Enter Adafruit IO credentials in this file
`android_app/app/src/main/java/com/example/wireless_noise_detection/MainActivity.java`

### 4. Create an Android Emulator
- Go to Device Manager, located top right
- Click "Create Device"
- Select Pixel 5 phone 
- Select API 34 for system image, may need to dowload it
- Click "next" then "finish" to create the emulator
- Start the emulator by clicking the "Run" at the top bar and then "/Run 'app'" to start the app

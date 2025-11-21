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

This Android App was created to provide a real-time visualization of the sound levels that are sent to the Adafruit IO feed. It connects to the feed by using MQTT and displays the most recent value the feed has received.

###  Features
- Receive real-time noise levels from Adafruit IO feed
- Displays the most recent sound level
- Displays a progress bar to show how close the noise level is to an unsafe noise level
- Changes progress bar color depending on the sound level
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
- Select Pixel 6 phone 
- Select Tiramisu for system image, may need to download it
- Click "next" then "finish" to create the emulator
- Start the emulator by clicking the "Run" at the top bar and then "/Run 'app'" to start the app

##  3. Noise Detection Sensor

**Folder:** `noise_detection_sensor/`

## Parts List

| Component                                   | Cost    |
|---------------------------------------------|---------|
| Gravity: Analog Sound Level Meter           | $39.50  |
| NeoPixel Ring – 24 × 5050 RGBW LEDs         | $19.95  |
| Breadboarding Wire Bundle                   | $4.95   |
| Breadboard Power Kit                        | $5.99   |
| Dorcy Mastercell 9V Battery                 | $2.99   |
| Adafruit ESP32-S2 Feather                   | $17.99  |
| Pink and Purple Woven USB A to USB C Cable  | $2.95  |

**Total Cost: $94.32**

###  Features
- Connect to Wi-Fi
- Connect to Adafruit IO feed
- Receive sound level values
- Send sound level values to feed
- Display how close the noise level value is to an unsafe noise level

##  Setup Instructions

### 1. Place breadboard power kit onto breadboard

### 2. Place Adafruit ESP32-S2 Feather onto breadboard
- Place one breadboard wire from the GND pin on the ESP32-S2 to a negative side hole on the breadboard
- Place one breadboard wire from the USB pin on the ESP32-S2 to a positive side hole on the breadboard

### 3. Connect analog sound level meter to breadboard
- Connect one breadboard wire from the meter to the A0 pin on the ESP32-S2 
- Connect one breadboard wire from the meter to the negative side holes on the breadboard
- Connect one breadboard wire from the meter to the positive side holes on the breadboard

### 4. Connect NeoPixel ring to breadboard
- Connect one breadboard wire from the ring to the 5 pin on the ESP32-S2 
- Connect one breadboard wire from the ring to the negative side holes on the breadboard
- Connect one breadboard wire from the ring to the positive side holes on the breadboard

### 5. Ensure proper board and libraries are used
- Open up Arduino IDE
- Open "print_values.ino" which is the file located in the "noise_detection_sensor/" folder   
- Install "Adafruit Feather ESP32-S2" by clicking "Tools -> Board Manager"
- Install the following libraries by clicking "Sketch -> Include Library -> Manage Libraries":
  - Adafruit MQTT Library
  - Adafruit NeoPixel

### 6. Insert Code into ESP32-S2
- Add Wi-Fi and Adafruit IO credentials in file
- Connect USB to ESP32-S2 to computer
- Use Arduino IDE to send code to ESP32-S2
- Click "Verify" on the top left to ensure there is no errors in the code
- Click "Upload" on the top left to send code into the ESP32-S2

### 7. Connect power source to breadboard power kit

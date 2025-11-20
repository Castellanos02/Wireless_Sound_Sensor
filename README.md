# Wireless Sound Sensor

This project aims to design and implement a wireless real-time noise detection system that monitors 
environmental sound levels and visualizes them on a live web dashboard and Android App.
It includes both a **Flask HTTP Server** for noise monitoring and an **Android App** for mobile visualization.

---

##  1. HTTP Flask Server (Noise Monitoring)

**Folder:** `http_flask_server/`

This component uses a **sound level sensor** connected to an **ESP32** and publishes readings to **Adafruit IO**.  
A **Flask web app** polls the Adafruit REST API to visualize live noise levels using **Chart.js**.

###  Features
- Real-time noise level monitoring via HTTP polling  
- Visual alert when noise exceeds 85 dB  
- Adafruit IO integration for live data  
- Flask + Chart.js dashboard interface  

---

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

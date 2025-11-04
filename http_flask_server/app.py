from flask import Flask, jsonify, render_template
import requests
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

app = Flask(__name__)

# Read secrets from environment
ADAFRUIT_IO_USERNAME = os.getenv("AIO_USERNAME")
ADAFRUIT_IO_KEY = os.getenv("AIO_KEY")
FEED_KEY = os.getenv("AIO_FEED")

# --- HTTP polling from Adafruit IO ---
# Every time /data is requested by the browser,
# Flask calls fetch_latest_data(), which calls
# the Adafruit REST API via requests.get().

def fetch_latest_data():
    # limit=1 : the latest single value
    url = f"https://io.adafruit.com/api/v2/{ADAFRUIT_IO_USERNAME}/feeds/{FEED_KEY}/data?limit=1"
    headers = {"X-AIO-Key": ADAFRUIT_IO_KEY}
    r = requests.get(url, headers=headers)
    if r.status_code == 200:
        data = r.json()
        if data:
            # returns value as JSON ({ "value": "69" })
            return data[0]['value']
    return None


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/data")
def data():
    val = fetch_latest_data()
    return jsonify({"value": val})


if __name__ == "__main__":
    app.run(debug=True)

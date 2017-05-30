# AWSensorsDashboard
This app is intended for collection of sensor data from an android smartwatch, in order to send them through an android mobile to IBM Bluemix with MQTT protocol.

It collects only Accelerometer and Gyroscope data from the watch, but it can be easily expanded to other types of sensors.
It runs in the background, both for wear and mobile.

The app also receives push notifications, both to wear and mobile, after successfully registering a Firebase account. 

This app was developed as part of a master thesis. It mainly focuses on its functionality as input to a big project of processing sensor data with Cloud Computing (IBM Bluemix), and not to the UI, which is rather simple, but easy to use. 

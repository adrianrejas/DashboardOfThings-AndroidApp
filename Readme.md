# Dashboard of Things

## Project Overview
Nowadays we have available a lot of cloud solutions oriented to IoT networks, such as Microsoft Azure IoT Hub, Google IoT core, IBM Watson IoT platform or Samsung Artik platform. These cloud platforms and many more provide us a bridge between IoT sensors and actuators (sensors like thermometers, movement detectors or heart rate trackers and actuators like smart plugs, thermostats or lights) and any device connected to the Internet. Most of these platforms relies on standard protocols, such as HTTP and MQTT.    

The problem is the lack of a general frontend for accessing from there to different IoT backends. There are many IoT projects and installations which require just a simple frontend which can be configured for adapting to backend, but there are not many applications, no matter the platform, for that.    

With Dashboard of Things, you’ll be able to convert your phone or tablet in a dashboard where you’ll be able to control your IoT devices remotely, receiving real-time and historical information from IoT sensors and being able to activate and configure your IoT actuators, being able to have IoT devices from different sources being monitored and controlled from a single device.  

More information about the project in https://github.com/adrianrejas/DashboardOfThings-AndroidApp/blob/master/Capstone_Stage1.pdf


## Screenshots

| Sensors dashboard | Actuators dashboard |  History dashboard |  Maps screen |
|:-:|:-:|:-:|
| ![First](https://github.com/adrianrejas/DashboardOfThings-AndroidApp/blob/master/captures/main_dash_sensors.png?raw=true) | ![Sec](https://github.com/adrianrejas/DashboardOfThings-AndroidApp/blob/master/captures/main_dash_actuators.png?raw=true) | ![Sec](https://github.com/adrianrejas/DashboardOfThings-AndroidApp/blob/master/captures/maindash_history.png?raw=true) | ![Sec](https://github.com/adrianrejas/DashboardOfThings-AndroidApp/blob/master/captures/maps.png?raw=true) |


## Main libraries used 

* Androidx framework (Room, Lifecycle, legacy ...)
* Dagger2 for dependency injection
* RxJava2 library for reactive communication between elements
* GSON JSON parser library
* OkHttp library for HTTP connections
* Eclipse PAHO library for MQTT connections
* Glide external library for image loading
* MPAndroidChart library for line graphs
* Google maps library for map management
* Sucho Place picker library
* Google Admob library for the management of ads in the application
* Firebase Job dispatcher for scheduling HTTP request for sensors
* Support libraries


## Testing

For testing HTTP connections, http://dummy.restapiexample.com/ can be used.

For testing MQTT connections, Mosquitto provides a test server available online: https://test.mosquitto.org/

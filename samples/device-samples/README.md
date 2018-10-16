Device Samples
============================================

Following stand-alone samples(present in this project) demonstrate the device connectivity to IBM Watson IoT Platform.

* [Quickstart sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-samples/src/main/java/com/ibm/iotf/sample/client/device/QuickstartDeviceEventPublish.java) that connects to Watson IoT Quickstart service and publishes an event.
* [MQTT Device sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-samples/src/main/java/com/ibm/iotf/sample/client/device/DeviceEventPublishWithCounter.java) that publishes an event, every second to IBM Watson IoT Platform using MQTT.
* [HTTP Device sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-samples/src/main/java/com/ibm/iotf/sample/client/device/HttpDeviceEventPublish.java) that publishes an event, every second to IBM Watson IoT Platform using HTTP.
* [Command subscription sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-samples/src/main/java/com/ibm/iotf/sample/client/device/RegisteredDeviceCommandSubscribe.java) that subscribes to commands while publishing device events to IBM Watson IoT Platform.

The samples are written using the [Java Client Library](https://github.com/ibm-messaging/iot-java) for IBM Watson IoT Platform that simplifies the interactions with the IBM Watson IoT Platform.

----

### Prerequisites
To build and run the sample, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

### Register Device in IBM Watson IoT Platform

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-devices-in-ibm-iot-foundation/) to register your device in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Device-Type = [Your Device Type]
* Device-ID = [Your Device ID]
* Authentication-Method = token
* Authentication-Token = [Your Device Token]

We need these details to connect the device to IBM Watson IoT Platform.

----


### Build & Run the sample using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the device-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-device-samples.git`
    
* Import the **device-samples** project into eclipse using the File->Import option in eclipse.

* Modify the **device.properties** file with the device registration details that you noted in the above step.

* Run the each of the sample by right clicking on the project and selecting "Run as" option.

* Observe that the device connects to Watson IoT Platform and publishes events / subscribes to commands.

----

### Building the sample - Required if you want to run the samples outside of Eclipse

* Clone the device-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/iot-device-samples.git`
    
* Navigate to the device-samples project, 

    `cd iot-device-samples\java\device-samples`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform, other required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-device-samples-0.0.1.jar.

----

### Running the Quickstart sample outside Eclipse

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.device.QuickstartDeviceEventPublish"`

* Observe that the sample connects to Watson IoT Platform Quickstart service and publishes an event. You can view the same, [by visiting this link] (https://quickstart.internetofthings.ibmcloud.com/#/device/00aabbccde03). You may need to change the Device-id, if you have modified the sample with different deviceId.

----

### Running the Registered device sample outside Eclipse

* Navigate to **target/classes** directory and modify **device.properties** file with the registration details that you noted in the previous step.

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.device.DeviceEventPublishWithCounter"`
    
* Observe that the sample connects to Watson IoT Platform Registered service and publishes an event every second. You can view the same by going to the platform dashboard.

----

**Note**: One can run other samples by following the above steps.

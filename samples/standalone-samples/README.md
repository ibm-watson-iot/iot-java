Application Samples
============================================

Following stand-alone samples(present in this project) demonstrate the device connectivity to IBM Watson IoT Platform.

* [Receive Device Events sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/standalone-samples/src/main/java/com/ibm/iotf/sample/client/application/RegisteredApplicationSubscribeSample.java) - Application that connects to Watson IoT service and subscribes to various device events.
* [Command Publish sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/standalone-samples/src/main/java/com/ibm/iotf/sample/client/application/RegisteredApplicationCommandPublish.java) - Application sample that publishes various commands to the devices connected to the IBM Watson IoT Platform.
* [MQTT Application Device event publish sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/standalone-samples/src/main/java/com/ibm/iotf/sample/client/application/MQTTApplicationDeviceEventPublish.java) - An application sample that publishes an event on behalf of a device, every second to IBM Watson IoT Platform using MQTT.
* [HTTP Application Device event publish sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/standalone-samples/src/main/java/com/ibm/iotf/sample/client/application/HttpApplicationDeviceEventPublish.java) - An application sample that publishes an event on behalf of a device, every second to IBM Watson IoT Platform using HTTP.
* [Quickstart - HTTP Application Device event publish sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/standalone-samples/src/main/java/com/ibm/iotf/sample/client/application/QuickstartMQTTApplicationDeviceEventPublish.java) - An application sample that publishes an event on behalf of a device, to Watson IoT Quickstart service.
* [Shared subscription sample] (https://github.com/ibm-watson-iot/iot-java/blob/master/samples/standalone-samples/src/main/java/com/ibm/iotf/sample/client/application/SharedSubscriptionSample.java) - Application that showcases how to build a scalable application/load balancing in Watson IoT Platform.

The samples are written using the [Java Client Library](https://github.com/ibm-messaging/iot-java) for IBM Watson IoT Platform that simplifies the interactions with the IBM Watson IoT Platform.

----

### Prerequisites
To build and run the sample, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

### Build & Run the sample using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the device-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-application-samples`
    
* Import the **standalone-samples** project into eclipse using the File->Import option in eclipse.

* Modify the **application.properties** file with the Organization Id, API-Key, Auth-Token and other necessary details.

* Build & run the each of the sample by right clicking on the project and selecting "Run as" option.

* Observe that the application connects to Watson IoT Platform and publishes events on behalf of devices/ publishes commands to device / subscribes to device events.

----

### Building the sample - Required if you want to run the samples outside of Eclipse

* Clone the iot-application-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/iot-application-samples`
    
* Navigate to the standalone-samples project, 

    `cd iot-application-samples\java\standalone-samples`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform and all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-standalone-samples-0.0.1.jar.

----


### Running the RegisteredApplicationSubscribeSample sample outside Eclipse

* Navigate to **target/classes** directory and modify **application.properties** file with the Organization Id, API-Key, Auth-Token and other necessary details. Also, specify the details of the device for which the application will subscribe to and receive events.

* Go back to the root project directory where the POM.xml file is present and start the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.RegisteredApplicationSubscribeSample"`

* Observe that the application connects to Watson IoT Platform and subscribes to device events.

----

**Note**: One can run other samples by following the above steps.

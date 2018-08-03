IBM IOT Cloud SIGAR Adapter
===========================

This sample utilizes Hyperic's System Information Gatherer (SIGAR) library, a cross-platform API for collecting 
software inventory data.  The sample supports [IBM Internet of Things QuickStart](http://quickstart.internetofthings.ibmcloud.com) as well as 
the registered flow.

SIGAR includes support for Linux, FreeBSD, Windows, Solaris, AIX, HP-UX and Mac OSX across a variety of 
versions and architectures. 

Hyperic SIGAR is licensed under the terms of the Apache 2.0 license.


### Prerequisites
To build and run the sample, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

### Build & Run the sample using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the device-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-application-samples.git`
    
* Import the **sigar-sample** project into eclipse using the File->Import option in eclipse.

* Add the native library path as <ibmiot-sigar-sample>/src/main/resources - This is where the native sigar library is present. Look at the [stack overflow answer](http://stackoverflow.com/questions/15271100/how-can-i-set-the-java-library-path-used-by-eclipse-from-a-pom) for how to add the path in eclipse.

**Run device sample in Quick start mode**

* Build and run the device sample **SigarIoTDevice** by right clicking on the project and selecting "Run as" option.

* Observe that the device connects to quickstart service and publishes an event every second,
```
Connected successfully - Your device ID is 507b9d89ebe9
http://quickstart.internetofthings.ibmcloud.com/?deviceId=507b9d89ebe9
Visit the QuickStart portal to see this device's data visualized in real time and learn more about the IBM Internet of Things Cloud
   
Start publishing event every second...
IBM497-PC0A0Y13:/22.99/61.62/7.69
```
As mentioned in the output, you can view the device events, by visiting the mentioned URL.

**Run device sample in Registered mode**

* Modify the **device.properties** file with the device registration details. Refer to the bottom of the page if you want to know how to register a device in IBM Watson IoT Platform.

* Build and run the device sample **SigarIoTDevice** by specifying the location of the device.properties file in the program arguments section, **-c [device.properties file path]**

* Observe that the device connects to Watson IoT Platform and publishes events. You can view the events in Watson IoT Platform dashboard. Also, we can start the application and receive the events.

**Run Application sample to receive events**

* Modify the **application.properties** file with the Organization name, API-Key, Token and the device details.

* Build and run the application sample **SigarIoTApp**.
 
* Observe that the application receives all the events that are published by the device, along with the device connectivity status.

----

### Building the sample - Required if you want to run the samples outside of Eclipse

* Clone the device-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/iot-application-samples.git`
    
* Navigate to the device-samples project, 

    `cd iot-application-samples\java\sigar-sample`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform (Currently its shipped as part of this sample, but soon it will be made available in maven central repository), download all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-sigar-sample-0.0.1.jar.

----

### Running the Device sample (in Quickstart mode) outside Eclipse

* Set MAVEN_OPTS with the native library path as follows, 

    On Windows,
    set MAVEN_OPTS="-Djava.library.path=<path-to-project-directory>\src\main\resources"
    
    On Linux systems
    export MAVEN_OPTS="-Djava.library.path=<path-to-project-directory>\src\main\resources"

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.samples.sigar.SigarIoTDevice"`

**Note:** If there is an Error, try extracting the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location. 

* Observe that the device connects to quickstart service and publishes an event every second,

```
Connected successfully - Your device ID is 507b9d89ebe9
http://quickstart.internetofthings.ibmcloud.com/?deviceId=507b9d89ebe9
Visit the QuickStart portal to see this device's data visualized in real time and learn more about the IBM Internet of Things Cloud
   
Start publishing event every second...
IBM497-PC0A0Y13:/22.99/61.62/7.69
```

As mentioned in the output, you can view the device events, by visiting the mentioned URL.

----

### Running the Device sample (in Registered mode) outside Eclipse

* Navigate to **target/classes** directory and modify **device.properties** file with the device registration details. Refer to the bottom of the page if you want to know how to register a device in IBM Watson IoT Platform.

* Set MAVEN_OPTS with the native library path as follows, 

    On Windows,
    set MAVEN_OPTS="-Djava.library.path=<path-to-project-directory>\src\main\resources"
    
    On Linux systems
    export MAVEN_OPTS="-Djava.library.path=<path-to-project-directory>\src\main\resources"

* Extract ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location.

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.samples.sigar.SigarIoTDevice" -Dexec.args="-c <path to the device.propertiesfile>"`

* Observe that the device connects to Watson IoT Platform and publishes events. You can view the events in Watson IoT Platform dashboard. Also, we can start the application and receive the events.

----

### Running the Application sample outside Eclipse

* Navigate to **target/classes** directory and modify the **application.properties** file with the Organization name, API-Key, Token and the device details.

* Extract ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location.

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass=mvn exec:java -Dexec.mainClass=com.ibm.iotf.samples.sigar.SigarIoTApp"`

* Observe that the application receives all the events that are published by the device, along with the device connectivity status.

----

### Register Device in IBM Watson IoT Platform - Not required for Quickstart flow

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-devices-in-ibm-iot-foundation/) to register your device in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Device-Type = [Your Device Type]
* Device-ID = [Your Device ID]
* Authentication-Method = token
* Authentication-Token = [Your Device Token]

We need these details to connect the device to IBM Watson IoT Platform.

----

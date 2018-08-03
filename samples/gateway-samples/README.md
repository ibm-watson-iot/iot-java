Gateway samples
============================================

This Java project contains 3 samples, which will help you to connect your Gateway and devices behind the Gateway to IBM Watson Internet of Things Platform. All the samples use the Java Client Library for IBM Watson IoT Platform, that simplifies the Gateway interactions with the Platform.

Following are examples present in this project,

* [SimpleGatewayExample](https://github.com/ibm-messaging/gateway-samples/blob/master/java/gateway-samples/src/main/java/com/ibm/iotf/sample/client/gateway/SimpleGatewayExample.java)
* [SampleRasPiGateway](https://github.com/ibm-messaging/gateway-samples/blob/master/java/gateway-samples/src/main/java/com/ibm/iotf/sample/client/gateway/SampleRasPiGateway.java)
* [ManagedRasPiGateway](https://github.com/ibm-messaging/gateway-samples/blob/master/java/gateway-samples/src/main/java/com/ibm/iotf/sample/client/gateway/devicemgmt/ManagedRasPiGateway.java)

----

###Tutorial explaining the samples

Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-gateway-to-watson-iot-platform/), that explains how to connect your Gateway and devices behind the Gateway to IBM Watson Internet of Things Platform with the sample present in this github project. 

Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-1/), that explains how to connect your Gateway as managed device in IBM Watson Internet of Things Platform and perform one or more device management operations. 

----

### Prerequisites
To build and run the samples, you must have the following installed:

* [git](https://git-scm.com/)
* [maven](https://maven.apache.org/download.cgi)
* Java 7+

----

### Register Gateway in IBM Watson IoT Platform

Follow the steps in [this recipe](https://developer.ibm.com/recipes/tutorials/how-to-register-gateways-in-ibm-watson-iot-platform/) to register your gateway in Watson IoT Platform if not registered already. And copy the registration details, like the following,

* Organization-ID = [Your Organization ID]
* Device-Type = [Your Gateway Device Type]
* Device-ID = [Your Gateway Device ID]
* Authentication-Method = token
* Authentication-Token = [Your Gateway Token]

We need these details to connect the gateway to IBM Watson IoT Platform.

----

### Build & Run the samples using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the gateway-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/gateway-samples.git`

* Import the advanced-gateway-sample project into eclipse using the File->Import option in eclipse.
* Modify **gateway.properties** and **DMGatewaySample.properties** files with the gateway registration details.
* Also, generate the Organization's API-Key and Token and update the same in both the properties files if the registration mode is manual (as of now, only the manual registration is supported).
* Run the appropriate sample by by right clicking on the project and selecting "Run as" option.

**Note**: Each sample section below has a link to recipe that explains the sample in more details and also how to run them.

----

### Building the samples - required if you want to run outside Eclipse

* Clone the gateway-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/gateway-samples.git`
    
* Navigate to the gateway-samples project, 

    `cd gateway-samples\java\gateway-samples`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform and all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-gateway-samples-0.0.1.jar.

----

### Running SimpleGatewayExample outside Eclipse

A stand-alone sample that connects a gateway and a device behind the gateway to IBM Watson IoT Platform. 

* Navigate to **target/classes** directory and modify **gateway.properties** file with the registration details that you noted in the previous step.
* Also, generate the Organization's API-Key and Token and update the same if the registration mode is manual (as of now, only the manual registration is supported).

* Go back to the root directory where the POM file is present and run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.gateway.SimpleGatewayExample"`

----

### Running SampleRasPiGateway Sample outside Eclipse

The Gateway support is demonstrated in this sample by connecting the Arduino Uno to Raspberry Pi, where the Raspberry Pi act as a Gateway and publishes events/receives commands on behalf of Arduino Uno to IBM Watson IoT Platform. This sample has a simulator object and can be used in the places where Raspberry Pi and Arduino Uno is not there. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-gateway-to-watson-iot-platform/) for more information about the sample and how to run the sample in detail?

* Navigate to **target/classes** directory and modify **gateway.properties** file with the registration details that you noted in the previous step.
* Also, generate the Organization's API-Key and Token and update the same if the registration mode is manual (as of now, only the manual registration is supported).
* Go back to the root directory and run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.gateway.SampleRasPiGateway"`

* In order to push the command to blink the LED, one need to start the sample application present in the sample.

* Modify the application.properties file with the Organization details, then,

* Run the following command to start the application sample,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.SampleApplication"`

----

### Running ManagedRasPiGateway Sample outside Eclipse

**Gateway Device Management(DM)** capabilities are demonstrated in this sample by managing the Arduino Uno device through the Raspberry Pi Gateway. If you do not have Raspberry Pi and Arduino UNO, don’t worry, you can still follow the sample to connect your device as a gateway and manage one or more attached devices. In this case, you can use your Windows or Linux server as the gateway instead of Raspberry Pi. Also, the sample has a simulator in place of Arduino UNO to respond to gateway requests. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-1/) for more information about the sample and how to run the sample in detail?

* Navigate to **target/classes** directory and modify **DMGatewaySample.properties** file with the registration details that you noted in the previous step.
* Also, generate the Organization's API-Key and Token and update the same if the registration mode is manual (as of now, only the manual registration is supported)
* Extract the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location
* Go back to the root directory and run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.gateway.devicemgmt.ManagedRasPiGateway"`

* In order to push the command to blink the LED, one need to start the sample application present in the sample.

* Modify the application.properties file with the Organization details, then,

* Run the following command to start the application sample,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.SampleApplication"`

----

### License
-----------------------

The library is shipped with Eclipse Public License and refer to the [License file] (https://github.com/ibm-messaging/iot-gateway-samples/blob/master/LICENSE) for more information about the licensing.

----

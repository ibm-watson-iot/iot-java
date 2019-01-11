Device Management Samples
============================================

Following stand-alone samples(present in this project) demonstrates the device management capabilities in IBM Watson IoT Platform.

* [SampleRasPiDMAgent](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiDMAgent.java)- A sample agent code that shows how to perform various device management operations on Raspberry Pi.
* [SampleRasPiManagedDevice](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiManagedDevice.java) - A sample code that shows how one can perform both device operations and device management operations.
* [SampleRasPiDMAgentWithCustomMqttAsyncClient](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiDMAgentWithCustomMqttAsyncClient.java) - A sample agent code with custom MqttAsyncClient.
* [SampleRasPiDMAgentWithCustomMqttClient](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/devicemgmt/device/SampleRasPiDMAgentWithCustomMqttClient.java) - A sample agent code with custom MqttClient.
* [ManagedDeviceWithLifetimeSample](https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/src/com/ibm/iotf/sample/devicemgmt/device/ManagedDeviceWithLifetimeSample.java) - A sample that shows how to send regular manage request with lifetime specified.
* [DeviceAttributesUpdateListenerSample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/devicemgmt/device/DeviceAttributesUpdateListenerSample.java) - A sample listener code that shows how to listen for a various device attribute changes.
* [SampleDMEDevice](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/dme/device/SampleDMEDevice.java) - A sample DME device code that shows how to change the event publish interval using a custom action.
* [SampleDMEGateway](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/device-management-sample/src/main/java/com/ibm/iotf/sample/dme/gateway/SampleDMEGateway.java) - A sample DME gateway code that shows how to change the event publish interval for the gateway & attached devices using the custom device management action.

The samples are written using the [Java Client Library](https://github.com/ibm-messaging/iot-java) for IBM Watson IoT Platform that simplifies the interactions with the IBM Watson IoT Platform.

----

### Tutorial explaining the Sample

Refer to [the recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-managed-device-to-ibm-iot-foundation/) that shows how to connect the Raspberry Pi device as managed device to IBM Watson Internet of Things Platform to perform various device management operations in step by step using this client library.

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

* Clone the iot-device-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-device-samples.git`
    
* Import the **device-management-sample** project into eclipse using the File->Import option in eclipse.

* Modify the **DMDeviceSample.properties** file with the device registration details that you noted in the above step.

* Build & Run each of the sample by right clicking on the project and selecting "Run as" option.

* Observe that the device connects to IBM Watson IoT Platform and lists down various device management operations that the sample agent can perform. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-managed-device-to-ibm-iot-foundation/) for more information about how to run the RasPiDMAgent Sample.

----

### Building the sample - Required if you want to run the samples outside of Eclipse

* Clone the device-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/iot-device-samples.git`
    
* Navigate to the device-samples project, 

    `cd iot-device-samples\java\device-management-sample`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform, other required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-device-management-sample-0.0.1.jar.

----

### Running the SampleRasPiDMAgent sample outside Eclipse

* Navigate to **target/classes** directory and modify the **DMDeviceSample.properties** file with the device registration details that you noted in the above step.

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.devicemgmt.device.SampleRasPiDMAgent"`

* Observe that the device connects to IBM Watson IoT Platform and lists down various device management operations that the sample agent can perform. Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-managed-device-to-ibm-iot-foundation/) for more information about how to run the RasPiDMAgent Sample.

----

**Note**: Follow the above steps to run any other samples present in this project.

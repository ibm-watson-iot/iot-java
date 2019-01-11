Shared subscription sample
============================================

There are two stand-alone samples present in this project to demonstrate the shared subscription support:

* [Device sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/shared-subscription-sample/src/main/java/com/ibm/iotf/sample/client/device/DeviceEventPublishWithCounter.java) that publishes an event, every second to IBM Watson IoT Platform.
* [Application sample](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/shared-subscription-sample/src/main/java/com/ibm/iotf/sample/client/application/SharedSubscriptionSample.java) that subscribes to all the device events in the given IBM Watson IoT Platform organization and outputs the events in real-time.

Both the samples are written using the [Java Client Library](https://github.com/ibm-messaging/iot-java) for IBM Watson IoT Platform that simplifies the interactions with the IBM Watson IoT Platform.

The Java Client Library for IBM Watson IoT Platform enables the shared subscription support based on the value set on the property **Shared-Subscription** by the user. When **Shared-Subscription** property is set to **true**, it connects the application in shared subscription mode, otherwise connects in stand-alone mode(default behavior). In order to enable the shared subscription support or scalable application support, the library internally forms the client id as **A:org_id:app_id** and connects to IoT Platform.

----

### Tutorial explaining the sample

Refer to [this recipe](https://developer.ibm.com/recipes/tutorials/shared-subscription-in-ibm-iot-foundation/), that explains the sample present in this github project and shared subscription support in detail.

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

### Build & Run the samples using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the shared-subscription-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-scalable-application-samples.git`
    
* Import the *ibmiot-shared-subscription-sample* project into eclipse using the File->Import option in eclipse.

**Start device sample**

* Modify the **device.properties** file with the Device registration details (Refer below to know how to register the Device in Watson IoT Platform).

* Run the device sample, **DeviceEventPublishWithCounter** by right clicking on the project and selecting "Run as" option.

* Observe that the device publishes events every 1 second – each event contains a counter, name, cpu and memory usage of the process as shown below,

     `{"event-count":1,"name":"Windows_7:6.1:x86","cpu":0.0,"mem":4896}`

**Start Application sample**

* Modify the **application.properties** file by entering your Organization ID, API Key and Authentication Token of the application.

* Make sure that the following property is set to true

      `Shared-Subscription = true`

* Run the first instance of application sample, **SharedSubscriptionSample** by right clicking on the project and selecting "Run as" option.

* Observe that all the events that are published by the device is consumed by the application, (Refer to the event-count to identify the event).

*  While the first instance of the application is running, start the another instance of application sample, **SharedSubscriptionSample** by right clicking on the project and selecting "Run as" option.
  
* Now, observe that the events are shared between the applications. that is the load is now shared between the application instance 1 and 2. For example, if application instance 1 receives the events with “event-count” values 2119 and 2121, the instance 2 will receive the events with “event-count” values 2118 and 2120.

----

### Building the sample - Outside Eclipse

* Clone the shared-subscription-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/iot-scalable-application-samples.git`
    
* Navigate to the ibmiot-shared-subscription-sample project, 

    `cd scalable-application-samples\java\ibmiot-shared-subscription-sample`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform, other required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-shared-subscription-sample-0.0.1.jar.

----

### Running the Device Sample - Outside Eclipse

* Navigate to **target/classes** directory and modify **device.properties** file with the registration details that you noted in the previous step.

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.device.DeviceEventPublishWithCounter"`

* Observe that the device publishes events every 1 second – each event contains a counter, name, cpu and memory usage of the process as shown below,

     `{"event-count":1,"name":"Windows_7:6.1:x86","cpu":0.0,"mem":4896}`


### Running the Application Sample - Outside Eclipse

* Modify the **application.properties** file by entering your Organization ID, API Key and Authentication Token of the application.

* Make sure that the following property is set to true

      `Shared-Subscription = true`

* Run the first instance of application sample, **SharedSubscriptionSample** by specifying the following command,

     `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.SharedSubscriptionSample"`

* Observe that all the events that are published by the device is consumed by the application, (Refer to the event-count to identify the event).

*  While the first instance of the application is running, start the another instance of application sample, **SharedSubscriptionSample** by specifying the same command as follows,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.SharedSubscriptionSample"`

* Now, observe that the events are shared between the applications. that is the load is now shared between the application instance 1 and 2. For example, if application instance 1 receives the events with “event-count” values 2119 and 2121, the instance 2 will receive the events with “event-count” values 2118 and 2120.

----

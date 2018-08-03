Advanced gateway sample
============================================

In this sample, we demonstrate a [sample home gateway](https://github.com/ibm-messaging/gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/gateway/HomeGatewaySample.java) that manages few attached home devices like, Lights, Switches, Elevator, Oven and OutdoorTemperature. And the following configuration is assumed,
 
 * Few devices are not manageable
 * Few devices are manageable but accept only firmware
 * Few devices are manageable but accept only Device actions
 * Few devices are manageable and accept both firmware/device actions 
 * All devices publish events and few devices accept commands.

Also, the sample has an [application](https://github.com/ibm-messaging/gateway-samples/blob/master/java/advanced-gateway-sample/src/main/java/com/ibm/iotf/sample/application/HomeApplication.java) that can be used to control one or more attached devices. For example, turn on/off a particular switch, turn on Oven or control the brightness of the light and etc..

Also, one can use the IBM Watson IoT Platform dashboard to update the firmware, reboot and reset the gateway or devices connected through the gateway.

----

### Prerequisites
To build and run the sample, you must have the following installed:

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

### Build & Run the sample using Eclipse

You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the gateway-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/gateway-samples.git`
    
* Import the advanced-gateway-sample project into eclipse using the File->Import option in eclipse.

* Build the project

#### Start Gateway Sample

* Modify the **DMGatewaySample.properties** file with the gateway registration details that you noted in the previous step.

* Also, generate the Organization's API-Key and Token and update the same in **DMGatewaySample.properties** file if the registration mode is manual (as of now, only the manual registration is supported).

* Run the **HomeGatewaySample** by right clicking on the project and selecting "Run as" option.

* Observe that the gateway publishes events for itself and on behalf of the devices connected through it.

#### Start Application Sample

In order to control one or more devices, you need to start the **HomeApplication** present in the project. 

* Modify the **application.properties** file with the organization details, like name, Organization's API-Key and Token.

* Run the **HomeApplication** by right clicking on the project and selecting "Run as" option. The application provides list of options to control the devices attached.

----

### Building the sample - Required if you want run outside Eclpise

* Clone the gateway-samples project using git clone as follows,
   
    `git clone https://github.com/ibm-messaging/gateway-samples.git`
    
* Navigate to the advanced-gateway-sample project, 

    `cd gateway-samples\java\advanced-gateway-sample`
    
* Run the maven build as follows,

    `mvn clean package`
    
This will download the Java Client library for Watson IoT Platform and all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-advanced-gateway-sample-0.0.1.jar.

----

### Running the HomeGateway Sample outside Eclipse

* Navigate to **target/classes** directory and modify **MGatewaySample.properties** file with the registration details that you noted in the previous step.
* Also, generate the Organization's API-Key and Token and update the same if the registration mode is manual (as of now, only the manual registration is supported)
* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.gateway.HomeGatewaySample"`

* In order to control one or more devices, you need to start the **HomeApplication** present in the project. 

* Modify the **application.properties** file with the organization details, like name, Organization's API-Key and Token.

* Run the following command to start the application sample,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.application.HomeApplication"`

Observe that the Application provides list of options to control one or more devices,

Also, In order to push a **firmware to a Gateway/device or reboot Gateway/device**, follow the [part-2 and part-3 of this recipe](https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-2/). This shows how to push a firmware using the Watson IoT Platform dashboard.

----

### License
-----------------------

The library is shipped with Eclipse Public License and refer to the [License file] (https://github.com/ibm-messaging/iot-gateway-samples/blob/master/LICENSE) for more information about the licensing.

----

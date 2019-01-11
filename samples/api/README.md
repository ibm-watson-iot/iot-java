IBM Watson IoT Platform API samples
============================================

Following stand-alone samples(present in this project) interacts with IBM Watson IoT Platform using the platform API Version 2 to demonstrate various capabilities of the API.

1.  [SampleBulkAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleBulkAPIOperations.java) - Sample that showcases how to get, add or remove devices in bulk from Watson IoT Platform.
2.  [SampleDeviceTypeAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleDeviceTypeAPIOperations.java) - Sample that showcases various Device Type API operations like list all, create, delete, view and update device types in Watson IoT Platform.
3.  [SampleDeviceAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleDeviceAPIOperations.java) - A sample that showcases various Device operations like list, add, remove, view, update, view location and view management information of a device in Watson IoT Platform.
4.  [SampleDeviceDiagnosticsAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleDeviceDiagnosticsAPIOperations.java) - A sample that showcases various Device Diagnostic operations like clear logs, retrieve logs, add log information, delete logs, get specific log, clear error codes, get device error codes and add an error code to Watson IoT Platform.
5.  [SampleDeviceManagementAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleDeviceManagementAPIOperations.java) - A sample that showcases various device management request operations that can be performed on Watson IoT Platform.
6.  [SampleUsageManagementAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleUsageManagementAPIOperations.java) - A sample that showcases various Usage management operations that can be performed on Watson IoT Platform.
7.  [SampleDataManagementAPIOperations](https://github.com/ibm-watson-iot/iot-java/blob/master/samples/api/src/main/java/com/ibm/iotf/sample/client/application/api/SampleDataManagementAPIOperations.java) - A sample that showcases various Data management operations that can be performed on Watson IoT Platform..

The samples are written using the [Java Client Library](https://github.com/ibm-messaging/iot-java) for IBM Watson IoT Platform that simplifies the interactions with the IBM Watson IoT Platform.

----

Setup your development environment
==================================
This section explains how to set up your development environment to use the Watson IoT Platform Client Library for Java.

Pre-requisites
--------------
To build and run the sample, you must have the following installed:  

* [Java 8](https://java.com/en/download/)

* [Maven 3](https://maven.apache.org/download.cgi)

* [Watson IoT Client Library for Java](https://github.com/ibm-watson-iot/iot-java)

* Optionally [Eclipse IDE](https://www.eclipse.org)

* [Git](https://git-scm.com/downloads)
----

Build & Run the samples inside Eclipse
=====================================
You must have installed the [Eclipse Maven plugin](http://www.eclipse.org/m2e/), to import & run the samples in eclipse. Go to the next step, if you want to run manually.

* Clone the iot-device-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-platform-apiv2-samples.git`

* Import the **api-samples-v2** project into eclipse using the File->Import option in eclipse.

* Modify the **application.properties** file with your Watson IoT Platform Organization details like, Organization name, API-Key and Token.

* Build & Run each of the sample by right clicking on the project and selecting "Run as" option.

* Observe that each sample interacts with IBM Watson IoT Platform using the ReST API and creates/modifies/retrieves/deletes the resources. One can observe the changes using the IoT Platform dashboard, for example, device creation can be viewed in the dashboard to see that the device is actually registered in the Platform.

----

Build & Run the samples outside Eclipse
====================================
### Building the sample

* Clone the device-samples project using git clone as follows,

    `git clone https://github.com/ibm-messaging/iot-platform-apiv2-samples.git`

* Navigate to the **api-samples-v2** project,

    `cd iot-platform-apiv2-samples\java\api-samples-v2`

* Run the maven build as follows,

    `mvn clean package`

This will download the Java Client library for Watson IoT Platform (Currently its shipped as part of this sample, but soon it will be made available in maven central repository), download all required dependencies and starts the building process. Once built, the sample can be located in the target directory, for example, target\ibmiot-apiv2-samples-0.0.1.jar.

----

### Running the `SampleDeviceAPIOperations` sample outside Eclipse

* Navigate to **target/classes** directory and modify the **application.properties** file with your Watson IoT Platform Organization details like, Organization name, API-Key and Token.

* Run the sample using the following command,

    `mvn exec:java -Dexec.mainClass="com.ibm.iotf.sample.client.application.api.SampleDeviceAPIOperations"`

**Note:** If there is an Error, try extracting the ibmwiotp.jar present in target/classes directory to the same location and run again. Remember the jar must be extracted in the same location.

* Observe that the sample creates/modifies/retrieves/deletes one or more devices in Watson IoT Platform using the API. You can observe the changes using the IoT Platform dashboard, for example, device creation can be viewed in the dashboard to see that the device is actually registered in the Platform.

----

**Note**: Follow the above steps to run any other samples present in this project.

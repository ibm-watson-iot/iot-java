Java Client Library - Introduction
============================================

This Java Client Library can be used to simplify interactions with the [IBM Internet of Things Foundation] (https://internetofthings.ibmcloud.com). The documentation is divided into following sections:  

- The [Device section] (https://docs.internetofthings.ibmcloud.com/devices/libraries/java.html) contains information on how devices publish events and handle commands using the Java ibmiotf Client Library. 
- The [Managed Device section] (https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/java_cli_for_manageddevice.rst) contains information on how devices can connect to the Internet of Things Foundation Device Management service using Java ibmiotf Client Library and perform device management operations like firmware update, location update, and diagnostics update.
- The [Gateway section] (https://github.com/ibm-messaging/iotf-rtd/blob/master/docs/gateways/java.rst) contains information on how gateways publish events and handle commands for itself and for the attached devices using the Java ibmiotf Client Library. 
- The [Gateway Management section] (https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdevicemanagement/java_cli_for_managedgateway.rst) contains information on how to connect the gateway as Managed Gateway to IBM Watson IoT Platform and manage the attached devices.
- The [Application section] (https://docs.internetofthings.ibmcloud.com/applications/libraries/java.html) details how applications can use the Java ibmiotf Client Library to interact with devices.
- The [API section] (https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/java_cli_for_historian.rst)  contains information on how applications can use the Java ibmiotf Client Library to interact with the organization in the Internet of Things Foundation through ReST APIs

This Java Client Library requires following version of Java,

*  [Java 7] (http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
*  [Java 8] (https://java.com/en/download/)

Download
-------------------------------------------------------------------------------
The latest version of the client library is available [here] (https://github.com/ibm-messaging/iot-java/releases/latest) to download.

The zip module contains the following files,

* com.ibm.iotf.client-<version>.jar - Client library that enables one to talk to Internet of Things Foundation Connect.
* com.ibm.iotf.samples-<version>.jar - Contains samples.
* DMDeviceSample.properties - Properties file used to configure the list of options required to connect to Internet of Things Foundation Connect and perform device management operations.
* device.prop - Properties file to specify connectivity information when DeviceClient is used
* lib - Contains all dependent libraries
* javadoc - Contains the client library documentation

Dependencies
-------------------------------------------------------------------------------

-  [Paho MQTT Java Client] (http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.java.git/) - provides a client class which enable applications to connect to an MQTT broker
-  [google-gson] (https://code.google.com/p/google-gson/) - library for interacting with JSON objects
-  [Apache Commons Logging] (http://commons.apache.org/proper/commons-logging/download_logging.cgi) - library for logging various informations
-  [Apache Commons Codec] (https://commons.apache.org/proper/commons-codec/download_codec.cgi) - provides common encoder and decoder functionalities such as Base64
-  [Apache Commons Lang] (https://commons.apache.org/proper/commons-lang/download_lang.cgi) - provides methods for manipulating core Java classes
-  [Apache Ant] (http://ant.apache.org/) - build tool for automated builds
-  [Apache HttpClient] (https://hc.apache.org/downloads.cgi) - A HTTP Client library
-  [Apache HttpCore] (https://hc.apache.org/downloads.cgi)  - A HTTP Core library
-  [Joda-Time] (http://www.joda.org/joda-time/download.html) - The date and time library for Java 

----

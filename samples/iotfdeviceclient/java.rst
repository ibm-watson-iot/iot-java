IBM Internet of Things Foundation for Java
============================================


Use the Java client library for interacting with the `IBM Internet of Things Foundation <https://internetofthings.ibmcloud.com>`__ and to automate commands using Java 7 or Java 8. The client library can be used to simplify interactions with the IBM Internet of Things Foundation. The following libraries contain instructions and guidance on using the java ibmiotf client library to interact with devices and applications within your organizations.

-  `Java 7 <http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html>`__
-  `Java 8 <https://java.com/en/download/>`__

This client library is divided into three sections, all included within the library.  

-  The Devices section contains information on how devices publish events and handle commands using the Java ibmiotf Client Library. 
-  The Applications section contains information on how applications can use the Java ibmiotf Client Library to interact with devices. 
-  The Historian section contains information on how applications can use the Java ibmiotf Client Library to retrieve the historical information.



Dependencies
-------------------------------------------------------------------------------

-  `Paho MQTT Java Client <http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.java.git/>`__   - provides a client class which enable applications to connect to an MQTT broker
-  `google-gson <https://code.google.com/p/google-gson/>`__   - library for interacting with JSON objects
-  `Apache Commons Logging <http://commons.apache.org/proper/commons-logging/download_logging.cgi>`__   - library for logging various informations
-  `Apache Commons Codec <http://commons.apache.org/proper/commons-logging/download_logging.cgi>`__  - provides common encoder and decoder functionalities such as Base64
-  `Apache Ant <http://ant.apache.org/>`__   - build tool for automated builds
-  `Apache HttpClient <https://hc.apache.org/downloads.cgi>`__   - A HTTP Client library
-  `Apache HttpCore <https://hc.apache.org/downloads.cgi>`__   - A HTTP Core library
-  `Joda-Time <http://www.joda.org/joda-time/download.html>`__ - The date and time library for Java 

----



Documentation
-------------
* `Device Client <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/java_cli_for_devices.rst>`__
* `Application Client <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/java_cli_for_applications.rst>`__
* `Historian Client <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/java_cli_for_historian.rst>`__
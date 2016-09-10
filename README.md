Java Client Library - Introduction
============================================

This Java Client Library can be used to simplify interactions with the [IBM Watson IoT Platform] (https://internetofthings.ibmcloud.com). The documentation is divided into following sections:  

- The [Device section] (docs/java_cli_for_devices.rst) contains information on how devices publish events and handle commands using the Java ibmiotf Client Library. 
- The [Managed Device section] (docs/java_cli_for_manageddevice.rst) contains information on how devices can connect to the Watson IoT Platform Device Management service using Java ibmiotf Client Library and perform device management operations like firmware update, location update, and diagnostics update.
- The [Gateway section] (docs/java_cli_for_gateway.rst) contains information on how gateways publish events and handle commands for itself and for the attached devices using the Java ibmiotf Client Library. 
- The [Gateway Management section] (docs/java_cli_for_managedgateway.rst) contains information on how to connect the gateway as Managed Gateway to IBM Watson IoT Platform and manage the attached devices.
- The [Application section] (docs/java_cli_for_applications.rst) details how applications can use the Java ibmiotf Client Library to interact with devices.
- The [API section] (docs/java_cli_for_api.rst)  contains information on how applications can use the Java ibmiotf Client Library to interact with the organization in the Watson IoT Platform through ReST APIs

This Java Client Library requires following version of Java,

*  [Java 7] (http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
*  [Java 8] (https://java.com/en/download/)

----

Maven support
--------------------------------------------------------------------

The library artifact is pushed to the maven. Use the following maven dependency to include this library in your java application.

    <dependency>
        <groupId>com.ibm.messaging</groupId>
        <artifactId>watson-iot</artifactId>
        <version>0.1.5</version>
    </dependency>
    

However if you want to build the library by yourself, use the following maven command,

    mvn clean package -Dmaven.test.skip=true
    
The above command quickly builds the library by skipping the tests and the `target` directory contains the output jar files. However, if you want to run the tests as well, then modify the property files present under directory `src/test/resources` and run the following maven command,
    
    mvn clean package

----

Download
-------------------------------------------------------------------------------

Refer to the maven secion for how to use this library in your project. Also, you can download the library manually from the [maven repository](https://repo1.maven.org/maven2/com/ibm/messaging/watson-iot/0.1.5/watson-iot-0.1.5.jar).

When you use maven, the dependencies are downloaded automatically.

----

Samples
-------------------------------------------------------------------------------
In order to search & find the samples easily, we moved the samples to corresponding repositories and 

* [Device samples] (https://github.com/ibm-messaging/iot-device-samples) - Repository contains all device (also device management) related samples in different programming languages. Information and instructions regarding the use of these samples can be found in their respective directories.
* [Gateway Samples] (https://github.com/ibm-messaging/iot-gateway-samples) - Repository contains all Gateway (also gateway management) related samples in different programming languages.
* [Application samples] (https://github.com/ibm-messaging/iot-application-samples) - Repository contains samples for developing the application(s) in IBM Watson Internet of Things Platform in different languages.
* [Watson IoT Platform API V002 samples] (https://github.com/ibm-messaging/iot-platform-apiv2-samples) - Repository contains samples that interacts with IBM Watson IoT Platform using the platform API Version 2.
* [Scalable Application samples] (https://github.com/ibm-messaging/iot-scalable-application-samples) - Repository contains sample(s) for building scalable applications, using shared subscription support in IBM Watson IoT Platform.
* [Backup-restore sample] (https://github.com/ibm-messaging/iot-backup-restore-sample) - The sample in the repository shows how to backup the device configuration in Cloudant NoSQL DB and restore the same later.


Dependencies
-------------------------------------------------------------------------------

-  [Paho MQTT Java Client] (https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.client.mqttv3/1.1.0/org.eclipse.paho.client.mqttv3-1.1.0.jar) - provides a client class which enable applications to connect to an MQTT broker
-  [google-gson] (https://code.google.com/p/google-gson/) - library for interacting with JSON objects
-  [Apache Commons Logging] (http://commons.apache.org/proper/commons-logging/download_logging.cgi) - library for logging various informations
-  [Apache Commons Codec] (https://commons.apache.org/proper/commons-codec/download_codec.cgi) - provides common encoder and decoder functionalities such as Base64
-  [Apache Commons Lang] (https://commons.apache.org/proper/commons-lang/download_lang.cgi) - provides methods for manipulating core Java classes
-  [Apache Commons Net] (https://commons.apache.org/proper/commons-net/download_net.cgi) - provides method for client side Internet protocols.
-  [Apache Ant] (http://ant.apache.org/) - build tool for automated builds
-  [Apache HttpClient] (https://hc.apache.org/downloads.cgi) - A HTTP Client library
-  [Apache HttpCore] (https://hc.apache.org/downloads.cgi)  - A HTTP Core library
-  [Joda-Time] (http://www.joda.org/joda-time/download.html) - The date and time library for Java 

----

License
-----------------------

The library is shipped with Eclipse Public License and refer to the [License file] (LICENSE) for more information about the licensing.

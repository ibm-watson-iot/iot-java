iot-java
========

Contains samples for working with the IBM Internet of Things cloud from a Java runtime environment.  

All samples are based on common client code.  This client uses google-gson to convert Java objects to a
JSON, which is supports the sending 

Apache Ant is required if you wish to build the project locally.
 
Dependencies
------------
* [Apache Ant](http://ant.apache.org/)
* [google-gson](https://code.google.com/p/google-gson/)
* [Paho MQTT Java Client](http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.java.git/)


Client Usage
------------
```java
import com.ibm.iotcloud.client.Device;

String myDeviceId = "1234567789012";

HashMap<String, String> dataMap = new HashMap<String, String>();
dataMap.put("foo", "bar");
dataMap.put("red", "hot");

MyClass dataObj = new MyData();
dataObj.setFoo("bar");
dataObj.setRed("hot");

// Connect, send 2 (identical) messages and then disconnect
Device d = new Device(myDeviceId);
d.send("sampleDataset", dataMap);
d.send("sampleDataset", dataObj);
d.disconnect();
```
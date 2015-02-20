iot-java (This is a pre-release version and its still a work in progress)
========
The Java Client Library can be used to connect to the [IBM Internet of Things (IoT) Foundation](https://internetofthings.ibmcloud.com/). Use the Java Client Library for the following activities:  

* Subscribe to device events, device status, application status
* Register Devices, de-register devices and retrieve information about devices 
* Retrieve Historian Information

All samples are based on common client code.  This client uses google-gson to convert Java objects to a
JSON object. 

Apache Ant is required if you wish to build the project locally.
 
Dependencies
------------
* [Apache Ant](http://ant.apache.org/)
* [google-gson](https://code.google.com/p/google-gson/)
* [Paho MQTT Java Client](http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.java.git/)


Client Usage
------------
## Device
```DeviceClient``` is used for making device calls. 
```DeviceClient``` constructor accepts an `options(java.util.Properties)` as follows

* org - Your organization ID
* type - The type of your device
* id - The ID of your device
* auth-method - Method of authentication (the only value currently supported is “token”)
* auth-token - API key token (required if auth-method is “token”)

```java
      
        
		Properties options = new Properties();
		DeviceClient client = null;
		options.put("org", "organization");
		options.put("type", "deviceType");
		options.put("id", "deviceId");
		options.put("auth-method", "token"); // the only value currently supported is “token”
		options.put("auth-token", "authToken");

		try {
			client = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
```

```DeviceClient``` constructor also accepts configuration file in the following format

    org=$orgId
    type=$myDeviceType
    id=$myDeviceId
    auth-method=token
    auth-token=$token

```java
        
      DeviceClient client;
      Properties options = DeviceClient.parsePropertiesFile(configFilePath);
        try {
			client = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
```

### Publishing events
```DeviceClient``` can be used to publish events. Events can be published at any of the three [quality of service](http://iotf.readthedocs.org/en/latest/messaging/mqtt.html#qoslevels "MQTT Quality of Service") levels. 

#### Publish events using default quality of service
```java
        
		client.connect();
		HashMap<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("temp", "30");
		dataMap.put("pressure", "289");

      client.publishEvent("status",dataMap); //QoS = 0
```

#### Publish events using user-defined quality of service
```java
        
        client.connect();
        HashMap<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("temp", "30");
        dataMap.put("pressure", "289");

        int qos = 2;
        client.publishEvent("status",dataMap, qos); //QoS = 2
```

As seen in the above example, the device event can be sent both as a ```HashMap``` and as a Java bean.

### Device Commands

When the device client connects it automatically subscribes to any command for this device. To process specific commands you need to register a command callback method. The messages are returned as an instance of the Command class which has the following properties:

* command - command name
* format - format of the command payload
* data - The actual data for the command
* payload - String 
* timestamp - Timestamp of the command

```java
			
	class MyCommandCallback implements EventCallback {	
		@Override
		public void processCommand(Command cmd) {	
			System.out.println("Received Command :: " + cmd.getCommand());
			if("print".equals(cmd.getCommand()) ) {
				String data = cmd.getPayload().toString();
			   if(data == null) {
			       System.out.println("ERROR - command is missing required information");
			   } else {
			       System.out.println(data);
			   }
          } else {
              System.out.println("Command : "+cmd.getCommand()+" , is currently not supported");
          }
		}
	}
	
	....
	....
	 client.connect();
	 client.setEventCallback(new MyCommandCallback());
```

## Application

```ApplicationClient``` constructor accepts an `options(java.util.Properties)` as follows

* org - Your organization ID
* id - The unique ID of your application within your organization
* auth-method - Method of authentication (the only value currently supported is “apikey”)
* auth-key - API key (required if auth-method is “apikey”)
* auth-token -  API key token (required if auth-method is “apikey”)

```java
        
		Properties options = new Properties();
    	ApplicationClient appClient = null;

		options.put("org", "organization");
		options.put("id", "uniqueAppId");
		options.put("auth-method", "apikey"); // the only value currently supported is “apikey”
		options.put("auth-key", "authKey");
		options.put("auth-token", "authToken");

		try {
			appClient = new ApplicationClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
```

```ApplicationClient``` constructor also accepts configuration file in the following format
    org=$orgId
    id=$myApplication
    auth-method=apikey
    auth-key=$key
    auth-token=$token

```java
        
		Properties options = new Properties();
    	ApplicationClient appClient;

      options = ApplicationClient.parsePropertiesFile(configFilePath);
      try {
			appClient = new ApplicationClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
```

### Subscribing to device events
```subscribeToDeviceEvents``` will subscribe to all events from all connected devices. Use the type, id and event parameters to control the scope of the subscription. A single client can support multiple subscriptions.

##### Subscribe to all events from all devices

```java

    client.connect();
    client.subscribeToDeviceEvents();
```

##### Subscribe to all events from all devices of a specific type

```java

    client.connect();
    client.subscribeToDeviceEvents("myDeviceType");
```

##### Subscribe to all events from a specfic device

```java

    client.connect();
    client.subscribeToDeviceEvents("myDeviceType", "myDeviceId");
```

##### Subscribe to specfic events from a specfic device

```java

    client.connect();
    client.subscribeToDeviceEvents("myDeviceType", "myDeviceId", "status");
```

##### Subscribe to a specific event from two different devices

```java

    client.connect();
    client.subscribeToDeviceEvents("myDeviceType", "myDeviceId1", "status");
    client.subscribeToDeviceEvents("myOtherDeviceType", "myDeviceId2", "status");
```


### Handling events from devices

To process the events received by your subscriptions you need to register an event callback method. The messages are returned as an instance of the Event class:

* event.getDeviceId() - String (uniquely identifies the device across all types of devices in the organization
* event.getDeviceType() - String
* event.getEvent() - String
* event.getFormat() - String
* event.getPayload() - String
* event.getTimestamp() - DateTime
 
```java

    class MyEventCallback implements EventCallback {
		@Override
		public void processEvent(Event e) {
			System.out.println("Event " + e.getPayload());
		}

		@Override
		public void processCommand(Command cmd) {
			System.out.println("Command " + cmd.getPayload());			
		}
	}
```



### Handling status updates from devices

To process the status updates received by your subscriptions you need to register an event callback method. The messages are returned as an instance of the Status class:

The following properties are set for both “Connect” and “Disconnect” status events:

* status.clientAddr - string
* status.protocol - string
* status.clientId - string
* status.user - string
* status.time - datetime
* status.action - string
* status.connectTime - datetime
* status.port - int

The following properties are only set when the action is “Disconnect”:

* status.writeMsg - int
* status.readMsg - int
* status.reason - string
* status.readBytes - int
* status.writeBytes - int

 
```java

    private class MyStatusCallback implements StatusCallback {

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println("Application Status = " + status.getPayload());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			System.out.println("Device Status = " + status.getPayload());
		}
	}
```

#### Subscribe to status updates for two different devices


```java


	private class MyStatusCallback implements StatusCallback {

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println("Application Status = " + status.getPayload());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			System.out.println("Device Status = " + status.getPayload());
		}
	}
	
	....
	....
	client.connect();
	client.setStatusCallback(new MyStatusCallback());	
	client.subscribeToDeviceStatus("myDeviceType", "myDeviceId1", "status");
	client.subscribeToDeviceStatus("myDeviceType", "myDeviceId2", "status");
	
```

### Publishing commands to devices

Applications can publish commands to connected devices

```java

        client.connect();
        HashMap<String, String> commandData = new HashMap<String, String>();
        commandData.put("reboot", "3");
        client.publishCommand("myDeviceType", "myDeviceId", "reboot", commandData)
```

## Device Registration, Information Retrieval and Device Deletion

The IoT Foundation client library can also be used for device registration, deletion and device information  retrieval.

```DeviceFactory``` is used to register devices, retrieve information about existing device(s) and delete an existing device. 

### Device Registration

```DeviceFactory``` constructor accepts an `options(java.util.Properties)` as follows
* authKey - The API Key for external device development
* authToken - The Auth Token for the API Key

```registerDevice``` method accepts deviceType, deviceId and metadata as follows
* deviceType - String which contains a deviceType
* deviceId - String which contains a deviceId
* metadata - String which contains metadata

```java

	Properties options = new Properties();
        
	options.put("authKey", "auth key for the app");
	options.put("authToken", "auth token");
	options.put("id", "deviceId");

	DeviceFactory factory = new DeviceFactory(options);

	String deviceType = new String("device type");
	String deviceId = new String("device id");
	String metadata = new String("metadata information");
	Device device = factory.registerDevice(deviceType, deviceId, metadata);

	if(device != null)
		System.out.println("Device retrieved and has " + device);
	else 
		System.out.println("Device not retrieved");

```

### Device Deletion

```DeviceFactory``` constructor accepts an `options(java.util.Properties)` as follows
* authKey - The API Key for external device development
* authToken - The Auth Token for the API Key.

```deleteDevice``` method accepts deviceType and deviceId as follows
* deviceType - String which contains a deviceType
* deviceId - String which contains a deviceId

```java

	Properties options = new Properties();
        
	options.put("authKey", "auth key for the app");
	options.put("authToken", "auth token");
	options.put("id", "deviceId");

	DeviceFactory factory = new DeviceFactory(options);

	String deviceType = new String("device type");
	String deviceId = new String("device id");

	boolean deviceDeleted = factory.deleteDevice(deviceType, deviceId);
	System.out.println("Operation was successful? " + deviceDeleted);
```

### Device Information Retrieval

```DeviceFactory``` constructor accepts an `options(java.util.Properties)` as follows
* authKey - The API Key for external device development
* authToken - The Auth Token for the API Key.

```getDevices``` method doesn't take any parameters and returns a list of devices registered with the given organization.

```java

	Properties options = new Properties();
        
	options.put("authKey", "auth key for the app");
	options.put("authToken", "auth token");
	options.put("id", "deviceId");

	DeviceFactory factory = new DeviceFactory(options);

	Device [] listDevices = factory.getDevices();
	System.out.println("Devices obtained = " + listDevices.length);	
```


### Historian Information Retrieval

```HistoricalEvent``` constructor is a default constructor.

```getHistoricalEvents``` method of the class DeviceFactory returns an array of Historian events (at most last 100). It is overloaded and accepts varying number of arguments.
#### 2 arguments
* deviceType which is of String type
* deviceId which is of String type

#### single argument
* deviceType which is of String type

#### no arguments

```java
    
	Properties options = new Properties();
        
	options.put("authKey", "auth key for the app");
	options.put("authToken", "auth token");
	options.put("id", "deviceId");

	DeviceFactory factory = new DeviceFactory(options);

	HistoricalEvent [] listHistory = factory.getHistoricalEvents("deviceType", "device Id");
	System.out.println("Events obtained = " + listHistory.length);

	listHistory = factory.getHistoricalEvents("deviceType");
	System.out.println("Events obtained = " + listHistory.length);

	listHistory = factory.getHistoricalEvents();
	System.out.println("Events obtained = " + listHistory.length);

```

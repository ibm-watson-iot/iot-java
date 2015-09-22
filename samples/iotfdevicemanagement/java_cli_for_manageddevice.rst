===============================================================================
Java Client Library - Managed Device (Update In Progress)
===============================================================================

Introduction
-------------------------------------------------------------------------------

This client library describes how to use devices with the Java ibmiotf client library. For help with getting started with this module, see `Java Client Library - Introduction <https://docs.internetofthings.ibmcloud.com/libraries/java.html#/>`__. 

This client library is divided into four sections, all included within the library. This section contains information on how devices can participate in various device management activities like, firmware update, location update, diagnostics update, device actions and etc..

The Device section contains information on how devices can publish events and handle commands using the Java ibmiotf Client Library. 

The Applications section contains information on how applications can use the Java ibmiotf Client Library to interact with devices. 

The Historian section contains information on how applications can use the Java ibmiotf Client Library to retrieve the historical information.

Device Management
-------------------------------------------------------------------------------
The device management feature enhances the Internet of Things Foundation service with new capabilities for managing devices. It creates a distinction between managed and unmanaged devices,

* **Managed Devices** are defined as devices which have a management agent installed. The management agent sends and receives device metadata and responds to device management commands from the Internet of Things Foundation. The device management agent and the Internet of Things Foundation device management service must share an understanding of data formats and communication patterns so they can interpret data correctly.
* **Unmanaged Devices** are any devices which do not have a device management agent. All devices begin their lifecycle as unmanaged devices, and can transition to managed devices by sending a message from a device management agent to the Internet of Things Foundation. Devices without a device management agent installed can never become managed devices.

Construct DeviceData
------------------------------------------------------------------------
The device model is the combination of device metadata and management characteristics of a device. Devices can contain a lot of metadata, including device identifiers, device type and associated identifiers, and device model extensions. For a list of identifiers, see the device model reference documentation

To create Device Data one needs to create the following objects:

* DeviceInfo (mandatory)
* DeviceLocation (optional)
* DeviceDiagnostic (optional)
* DeviceFirmware (optional)

The following code snippet shows how to create the DeviceInfo Object with sample data:

.. code:: java

     DeviceInfo deviceInfo = new DeviceInfo.Builder().
				serialNumber("10087").
				manufacturer("IBM").
				model("7865").
				deviceClass("A").
				description("My RasPi Device").
				fwVersion("1.0.0").
				hwVersion("1.0").
				descriptiveLocation("EGL C").
				build();

The following code snippet shows how to create the DeviceData Object with the above created DeviceInfo object:

.. code:: java

	DeviceData deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 metadata(new JsonObject()).
				 build();
Construct ManagedDevice
-------------------------------------------------------------------------------
The constructor builds a ManagedDevice instance using which the device can perform the device management operations. Also the ManagedDevice instance can be used to do normal device operations like publishing device events and listening for commands from application.

The device management section provides 3 different constructors to support different user patterns, 

**Constructor#1**

Constructs a managedDevice instance by accepting the DeviceData and the following properties,

* Organization-ID - Your organization ID.
* Device-Type - The type of your device.
* Device-ID - The ID of your device.
* Authentication-Method - Method of authentication (The only value currently supported is "token"). 
* Authentication-Token - API key token

The Properties object creates definitions which are used to interact with the Internet of Things Foundation module. 

The following code shows how to create a ManagedDevice instance:


.. code:: java

	Properties options = new Properties();
	options.setProperty("Organization-ID", "organization");
	options.setProperty("Device-Type", "deviceType");
	options.setProperty("Device-ID", "deviceId");
	options.setProperty("Authentication-Method", "authMethod");
	options.setProperty("Authentication-Token", "authToken");
	ManagedDevice dmClient = new ManagedDevice(options, deviceData);
 
Note that the name of the properties are slightly changed to miror the names in Internet of Things Foundation Dashboard, but the existing users who wants to migrate from the DeviceClient to ManagedDevice can still use the old format:

.. code:: java

	Properties options = new Properties();
	options.setProperty("org", "organization");
	options.setProperty("type", "deviceType");
	options.setProperty("id", "deviceId");
	options.setProperty("auth-method", "authMethod");
	options.setProperty("auth-token", "authToken");
	ManagedDevice dmClient = new ManagedDevice(options, deviceData);


Using a configuration file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Instead of including a Properties object directly, you can use a configuration file containing the name-value pairs for Properties. If you are using a configuration file containing a Properties object, use the following code format.

.. code:: java


	package com.ibm.iotf.sample.client.device;

	import java.io.File;
	import java.util.Properties;

	import com.google.gson.JsonObject;
	import com.ibm.iotf.client.device.DeviceClient;

	public class RegisteredDeviceEventPublishPropertiesFile {

		public static void main(String[] args) {
			//Provide the device specific data, as well as Auth-key and token using Properties class	
			Properties options = DeviceClient.parsePropertiesFile(new File("C:\\temp\\device.prop"));

			DeviceClient myClient = null;
			try {
				//Instantiate the class by passing the properties file			
				myClient = new DeviceClient(options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Connect to the IBM IoT Foundation	
			myClient.connect();
			
			//Generate a JSON object of the event to be published
			JsonObject event = new JsonObject();
			event.addProperty("name", "foo");
			event.addProperty("cpu",  90);
			event.addProperty("mem",  70);
			
			//Registered flow allows 0, 1 and 2 QoS
			myClient.publishEvent("status", event, 1);
			System.out.println("SUCCESSFULLY POSTED......");
			
      ...

The content of the configuration file must be in the following format:

::

    [device]
    org=$orgId
    typ=$myDeviceType
    id=$myDeviceId
    auth-method=token
    auth-token=$token


----


Publishing events
-------------------------------------------------------------------------------
Events are the mechanism by which devices publish data to the Internet of Things Foundation. The device controls the content of the event and assigns a name for each event it sends.

When an event is received by the IBM IoT Foundation the credentials of the connection on which the event was received are used to determine from which device the event was sent. With this architecture it is impossible for a device to impersonate another device.

Events can be published at any of the three `quality of service levels <https://docs.internetofthings.ibmcloud.com/messaging/mqtt.html#/>` defined by the MQTT protocol.  By default events will be published as qos level 0.

Publish event using default quality of service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
.. code:: java

			myClient.connect();
			
			JsonObject event = new JsonObject();
			event.addProperty("name", "foo");
			event.addProperty("cpu",  90);
			event.addProperty("mem",  70);
		    
			myClient.publishEvent("status", event);


----


Publish event using user-defined quality of service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Events can be published at higher MQTT quality of servive levels, but these events may take slower than QoS level 0, because of the extra confirmation of receipt. Also Quickstart flow allows only Qos of 0

.. code:: java

			myClient.connect();
			
			JsonObject event = new JsonObject();
			event.addProperty("name", "foo");
			event.addProperty("cpu",  90);
			event.addProperty("mem",  70);
		    
			//Registered flow allows 0, 1 and 2 QoS
			myClient.publishEvent("status", event, 2);


----


Handling commands
-------------------------------------------------------------------------------
When the device client connects it automatically subscribes to any command for this device. To process specific commands you need to register a command callback method. 
The messages are returned as an instance of the Command class which has the following properties:

* payload - java.lang.String
* format - java.lang.String
* command - java.lang.String
* timestamp - org.joda.time.DateTime

.. code:: java

	package com.ibm.iotf.sample.client.device;

	import java.util.Properties;


	import com.ibm.iotf.client.device.Command;
	import com.ibm.iotf.client.device.CommandCallback;
	import com.ibm.iotf.client.device.DeviceClient;


	//Implement the CommandCallback class to provide the way in which you want the command to be handled
	class MyNewCommandCallback implements CommandCallback{
		
		public MyNewCommandCallback() {
		}

		//In this sample, we are just displaying the command the moment the device recieves it
		@Override
		public void processCommand(Command command) {
			System.out.println("COMMAND RECEIVED = '" + command.getCommand() + "'\twith Payload = '" + command.getPayload() + "'");			
		}
	}

	public class RegisteredDeviceCommandSubscribe {

		
		public static void main(String[] args) {
			
			//Provide the device specific data, as well as Auth-key and token using Properties class		
			Properties options = new Properties();
			
			options.setProperty("org", "uguhsp");
			options.setProperty("type", "iotsample-arduino");
			options.setProperty("id", "00aabbccde03");
			options.setProperty("auth-method", "token");
			options.setProperty("auth-token", "AUTH TOKEN FOR DEVICE");
			
			DeviceClient myClient = null;
			try {
				//Instantiate the class by passing the properties file			
				myClient = new DeviceClient(options);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Pass the above implemented CommandCallback as an argument to this device client
			myClient.setCommandCallback(new MyNewCommandCallback());

			//Connect to the IBM IoT Foundation	
			myClient.connect();
		}
	}



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
Devices can contain a lot of metadata, including device identifiers, device type and associated identifiers, and device model extensions. For a list of identifiers, see the device model reference documentation

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
ManagedDevice - A device class that connects the device as managed device to IBM IoT Foundation and enables the device to perform one or more Device Management operations. Also the ManagedDevice instance can be used to do normal device operations like publishing device events and listening for commands from application.

ManagedDevice exposes 3 different constructors to support different user patterns, 

**Constructor#1**

Constructs a ManagedDevice instance by accepting the DeviceData and the following properties,

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
 
Note that the name of the properties are slightly changed to miror the names in Internet of Things Foundation Dashboard, but the existing users who wants to migrate from the DeviceClient to ManagedDevice can still use the old format and construct the ManagedDevice Instance:

.. code:: java

	Properties options = new Properties();
	options.setProperty("org", "organization");
	options.setProperty("type", "deviceType");
	options.setProperty("id", "deviceId");
	options.setProperty("auth-method", "authMethod");
	options.setProperty("auth-token", "authToken");
	ManagedDevice dmClient = new ManagedDevice(options, deviceData);

**Constructor#2**

Construct a ManagedDevice instance by accepting the DeviceData and the MqttClient instance. Also, this constructor requires the DeviceData to be created with the Device Type and Device Id as follows:

.. code:: java
	
	// Code that constructs the MqttClient
	.....
	
	// Code that constructs the DeviceData
	DeviceData deviceData = new DeviceData.Builder().
				 typeId("Device-Type").
				 deviceId("Device-ID").
				 deviceInfo(deviceInfo).
				 metadata(new JsonObject()).
				 build();
	
	....
	ManagedDevice dmClient = new ManagedDevice(mqttClient, deviceData);
	
Note this constructor helps the custom device users to create ManagedDevice instance with the already created and connected MqttClient instance to take advantage of device management operations. But we recommend the users to use the library for all the device functionalities.

**Constructor#3**

Constructs a managedDevice instance by accepting the DeviceData and the AsyncMqttClient instance:

.. code:: java
	
	// code that constructs the AsyncMqttClient
	....
	ManagedDevice dmClient = new ManagedDevice(asyncMqttClient, deviceData);

Manage	
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
A device can send this request to become a managed device. It should be the first device management request sent by the device after connecting to the Internet of Things Foundation. 

The connect() method on ManagedDevice does 2 things,

* Connects the device to IBM IoT Foundation and
* Sends the manage request such that the device becomes an Managed Device.

The overloaded connect(long) method takes time in seconds that specifies the length of time within which the device must send another **Manage device** request in order to avoid being reverted to an unmanaged device and marked as dormant.

.. code:: java

	dmClient.connect(3600);

The manage(long) method can be used to send the manage request to IBM IoT Foundation at any point:

.. code:: java

	dmClient.manage(4800);
	
Unmanage
-----------------------------------------------------

A device can send this request when it no longer needs to be managed. The Internet of Things Foundation will no longer send new device management requests to this device and all device management requests from this device will be rejected other than a **Manage device** request.

.. code:: java

	dmClient.unmanage();

Location Update
-----------------------------------------------------

Devices that can determine their location can choose to notify the Internet of Things Foundation device management server about location changes. In order to update the location, the device needs to create DeviceData instance with the DeviceLocation object:

.. code:: java
    // Construct the location object with latitude, longitude and elevation
    DeviceLocation location = new DeviceLocation.Builder(30.28565, -97.73921).
								elevation(10).
								build();
    DeviceData deviceData = new DeviceData.Builder().
				 deviceInfo(deviceInfo).
				 deviceLocation(location).
				 metadata(new JsonObject()).
				 build();
	
    
Once the device is connected to IBM IoT Foundation, the location can be updated by invoking the following method:

.. code:: java

	location.sendLocation();

Later, any new location can be easily updated by changing the properties of the DeviceLocation object:

.. code:: java

	location.update(40.28, -98.33, 11);

Listening for Location change
-----------------------------

As the location of the device can be updated using the the Internet of Things Foundation REST API, the library code updates the DeviceLocation object whenever it receives the update from the Internet of Things Foundation. The device can listen for such a location change by adding itself as a property change listener in DeviceLocation object and query the properties whenever the location is changed:

.. code:: java

	// Add a listener for location change
	location.addPropertyChangeListener(this);
	
	...
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println("Received a new location - "+evt.getNewValue());
	}

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



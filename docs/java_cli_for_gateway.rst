Java for Gateway Developers 
============================

- See `iot-java <https://github.com/ibm-messaging/iot-java>`_ in GitHub

Constructor
-------------------------------------------------------------------------------

The constructor builds the Gateway client instance, and accepts a Properties object containing the following definitions:

* org - Your organization ID.
* domain - (Optional) The messaging endpoint URL. By default the value is "internetofthings.ibmcloud.com"(Watson IoT Production server)
* type - The type of your Gateway device.
* id - The ID of your Gateway.
* auth-method - Method of authentication (The only value currently supported is "token"). 
* auth-token - API key token.

The Properties object creates definitions which are used to interact with the Watson Internet of Things Platform module. 

The following code snippet shows how to construct the GatewayClient instance,

.. code:: java
    
  Properties options = new Properties();
  options.setProperty("org", "<Your Organization ID>");
  options.setProperty("type", "<The Gateway Device Type>");
  options.setProperty("id", "The Gateway Device ID");
  options.setProperty("auth-method", "token");
  options.setProperty("auth-token", "API token");
  
  GatewayClient gwClient = new GatewayClient(options); 
    
Using a configuration file
~~~~~~~~~~~~~~~~~~~~~~~~~~

Instead of including a Properties object directly, you can use a configuration file containing the name-value pairs for Properties. If you are using a configuration file containing a Properties object, use the following code format.

.. code:: java

    Properties props = GatewayClient.parsePropertiesFile(new File("C:\\temp\\device.prop"));
    GatewayClient gwClient = new GatewayClient(props);
    ...

The Gateway device configuration file must be in the following format:

::

    [Gateway]
    org=$orgId
    domain=$domain
    typ=$myGatewayDeviceType
    id=$myGatewayDeviceId
    auth-method=token
    auth-token=$token

----


Connecting to the Watson Internet of Things Platform
----------------------------------------------------

Connect to the Watson Internet of Things Platform by calling the **connect** function. The connect function takes an optional boolean parameter autoRetry (by default autoRetry is true) that controls allows the library to retry the connection when there is an MqttException. Note that the library won't retry when there is a MqttSecurityException due to incorrect device registration details passed even if the autoRetry is set to true.

Also, one can use the **setKeepAliveInterval(int)** method before calling connect() to set the MQTT "keep alive" interval. This value, measured in seconds, defines the maximum time interval between messages sent or received. It enables the client to detect if the server is no longer available, without having to wait for the TCP/IP timeout. The client will ensure that at least one message travels across the network within each keep alive period. In the absence of a data-related message during the time period, the client sends a very small "ping" message, which the server will acknowledge. A value of 0 disables keepalive processing in the client. The default value is 60 seconds.

.. code:: java

    Properties props = GatewayClient.parsePropertiesFile(new File("C:\\temp\\device.prop"));
    GatewayClient gwClient = new GatewayClient(props);
    gwClient.setKeepAliveInterval(80);
    gwClient.connect();
    
Also, use the overloaded connect(int numberOfTimesToRetry) function to control the number of retries when there is a connection failure.

.. code:: java

    DeviceClient myClient = new DeviceClient(options);
    gwClient.setKeepAliveInterval(80);
    myClient.connect(10);
    
After the successful connection to the IBM Watson IoT Platform, the Gateway client can perform the following operations,

* Publish events for itself and on behalf of devices connected behind the Gateway.
* Subscribe to commands for itself and on behalf of devices behind the Gateway.

----

Register devices using the Watson IoT Platform API
-------------------------------------------------------------------------
There are different ways to register the devices behind the Gateway to IBM Watson IoT Platform,

* **Auto registration**: The device gets added automatically in IBM Watson IoT Platform when Gateway publishes any event/subscribes to any commands for the devices connected to it.
* **API**: The Watson IoT Platform API can be used to register the devices to the Watson IoT Platform. 

The Watson IoT Platform API can be used to register the devices (that are connected to the Gateway) to the Watson IoT Platform. The APIClient simplifies the interactions with Watson IoT Platform API. Get the APIClient instance by invoking the api() method as follows,

.. code:: java
     
     import com.ibm.iotf.client.api.APIClient;
     
     ....
     
     GatewayClient gwClient = new GatewayClient(props);
     gwClient.connect();
     
     APIClient api = gwClient.api();

Once you get the handle of APIClient, you can add the devices. Following code snippet shows how to add a device to a Gateway in Watson IoT Platform,

.. code:: java
 
    GatewayClient gwClient = new GatewayClient(props);
    gwClient.connect();
     
    String deviceToBeAdded = "{\"deviceId\": \"" + DEVICE_ID +
						"\",\"authToken\": \"qwer123\"}";

    JsonParser parser = new JsonParser();
    JsonElement input = parser.parse(deviceToBeAdded);
    JsonObject response = this.gwClient.api().registerDeviceUnderGateway(DEVICE_TYPE, gwDeviceId, gwDeviceType, input);

The gwDeviceId and gwDeviceType are the Gateway properties to which this device will be attached to when its registered.

----


Publishing events
-------------------------------------------------------------------------------
Events are the mechanism by which Gateways/devices publish data to the Watson IoT Platform. The Gateway/device controls the content of the event and assigns a name for each event it sends.

**The Gateway can publish events from itself and on behalf of any device connected via the Gateway**.

When an event is received by the IBM Watson IoT Platform the credentials of the connection on which the event was received are used to determine from which Gateway the event was sent. With this architecture it is impossible for a Gateway to impersonate another device.

Events can be published at any of the three `quality of service levels <../messaging/mqtt.html#/>`__ defined by the MQTT protocol.  By default events will be published as qos level 0.

Publish Gateway event using default quality of service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
.. code:: java
    
    gwClient.connect();
    JsonObject event = new JsonObject();
    event.addProperty("name", "foo");
    event.addProperty("cpu",  90);
    event.addProperty("mem",  70);
    
    gwClient.publishGatewayEvent("status", event);


Publish Gateway event using user-defined quality of service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Events can be published at higher MQTT quality of service levels, but these events may take slower than QoS level 0, because of the extra confirmation of receipt. 

.. code:: java

    gwClient.connect();
    JsonObject event = new JsonObject();
    event.addProperty("name", "foo");
    event.addProperty("cpu",  90);
    event.addProperty("mem",  70);
    
    gwClient.publishGatewayEvent("status", event, 2);

    
Publishing events from devices
-------------------------------------------------------------------------------

The Gateway can publish events on behalf of any device connected via the Gateway by passing the appropriate typeId and deviceId based on the origin of the event:

.. code:: java

    gwClient.connect()
    
    //Generate the event to be published
    JsonObject event = new JsonObject();
    event.addProperty("name", "foo");
    event.addProperty("cpu",  60);
    event.addProperty("mem",  40);
    
    // publish the event on behalf of device
     gwClient.publishDeviceEvent(deviceType, deviceId, eventName, event);

One can use the overloaded publishDeviceEvent() method to publish the device event in the desired quality of service. Refer to `MQTT Connectivity for Gateways <https://docs.internetofthings.ibmcloud.com/gateways/mqtt.html>`__ documentation to know more about the topic structure used.

----


Handling commands
-------------------------------------------------------------------------------
The Gateway can subscribe to commands directed at the gateway itself and to any device connected via the gateway. When the Gateway client connects, it automatically subscribes to any commands for this Gateway. But to subscribe to any commands for the devices connected via the Gateway, use one of the overloaded subscribeToDeviceCommands() method, for example,

.. code:: java

    gwClient.connect()
    
    // subscribe to commands on behalf of device
    gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);

To process specific commands you need to register a command callback method. The messages are returned as an instance of the Command class which has the following properties:

* deviceType - The device type for which the command is received.
* deviceId - The device id for which the command is received, Could be the Gateway or any device connected via the Gateway.
* payload - The command payload.
* format - The format of the command payload, currently only JSON format is supported in the Java Client Library.
* command - The name of the command.
* timestamp - The org.joda.time.DateTime when the command is sent.

A sample implementation of the Command callback is shown below,

.. code:: java

    import com.ibm.iotf.client.gateway.Command;
    import com.ibm.iotf.client.gateway.GatewayCallback;
    
    public class GatewayCommandCallback implements GatewayCallback, Runnable {
    	// A queue to hold & process the commands
    	private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
    	
    	public void processCommand(Command cmd) {
    	    queue.put(cmd);
    	}
    	
    	public void run() {
    	    while(true) {
    	        Command cmd = queue.take();
    	        System.out.println("Command " + cmd.getPayload());
    	        
    	        // code to process the command
    	    }
    	}
    	
    	/**
    	 * If a gateway subscribes to a topic of a device or sends data on behalf of a device 
	 * where the gateway does not have permission for, the message or the subscription is being ignored. 
	 * This behavior is different compared to applications where the connection will be terminated. 
	 * The Gateway will be notified on the notification topic:
	 */
    	@Override
	public void processNotification(Notification notification) {
		
	}
    } 
  
Once the Command callback is added to the GatewayClient, the processCommand() method is invoked whenever any command is published on the subscribed criteria, The following snippet shows how to add the command call back into GatewayClient instance,

.. code:: java

    gwClient.connect()
    GatewayCommandCallback callback = new GatewayCommandCallback();
    gwClient.setGatewayCallback(callback);
    //Subscribe to device connected to the Gateway
    gwClient.subscribeToDeviceCommands(DEVICE_TYPE, DEVICE_ID);


Overloaded methods are available to control the command subscription. 

----

List Devices Connected through the Gateway
------------------------------------------

Invoke the method getDevicesConnectedThroughGateway() to retrieve all devices that are connected through the specified gateway(typeId, deviceId) to Watson IoT Platform:

.. code:: java

    gwClient.connect()
    gwClient.api().getDevicesConnectedThroughGateway(gatewayType, gatewayId);


Examples
-------------
* `SampleRasPiGateway <https://github.com/ibm-messaging/iot-gateway-samples/tree/master/java/gateway-samples>`__ - This Java project contains 3 samples, which will help one to connect their own Gateway and devices behind the Gateway to IBM Watson Internet of Things Platform. All the samples use the Java Client Library for IBM Watson IoT Platform, that simplifies the Gateway interactions with the Platform. 

Recipes
------------------

Refer to `the recipe <https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-gateway-to-watson-iot-platform/>`__ that explains how to connect your Gateway and devices behind the Gateway to IBM Watson Internet of Things Platform with the sample present in this github project. 

Refer to `the recipe <https://developer.ibm.com/recipes/tutorials/raspberry-pi-as-managed-gateway-in-watson-iot-platform-part-1/>`__ that explains how to connect your Gateway as managed device in IBM Watson Internet of Things Platform and perform one or more device management operations. 

----

===============================================================================
Java Client Library - Applications
===============================================================================

Introduction
-------------------------------------------------------------------------------

This client library describes how to use applications with the Java ibmiotf client library. For help with getting started with this module, see `Java Client Library - Introduction <../java/javaintro.html/>`__. 

This client library is divided into three sections, all included within the library. This section contains information on how applications can use the Java ibmiotf Client Library to interact with devices.

The Device section contains information on how devices can publish events and handle commands using the Java ibmiotf Client Library. 

The Managed Device section contains information on how devices can connect to the Internet of Things Foundation Device Management service using Java ibmiotf Client Library and perform device management operations like firmware update, location update, and diagnostics update.

Constructor
-------------------------------------------------------------------------------

The constructor builds the client instance, and accepts a Properties object containing the following definitions:

* org - Your organization ID. (This is a required field. In case of quickstart flow, provide org as quickstart.)
* id - The unique ID of your application within your organization.
* auth-method - Method of authentication (the only value currently supported is “apikey”).
* auth-key - API key (required if auth-method is “apikey”).
* auth-token - API key token (required if auth-method is “apikey”).

The Properties object creates definitions which are used to interact with the Internet of Things Foundation module. If no options are provided or organization is provided as quickstart, the client will connect to the Internet of Things Foundation Quickstart, and default to an unregistered device.

The following code snippet shows how to construct the ApplicationClient instance in Quickstart mode,

.. code:: java

    import com.ibm.iotf.client.app.ApplicationClient;
    
    Properties options = new Properties();
    options.put("org", "quickstart");
    
    ApplicationClient myClient = new ApplicationClient(options);
    ...

The following code snippet shows how to construct the ApplicationClient instance in registered flow,

.. code:: java
    
    Properties options = new Properties();
    options.put("org", "uguhsp");
    options.put("id", "app" + (Math.random() * 10000));
    options.put("Authentication-Method","apikey");
    options.put("API-Key", "<API-Key>");
    options.put("Authentication-Token", "<Authentication-Token>");
    
    ApplicationClient myClient = new ApplicationClient(options);
    ...


Using a configuration file
~~~~~~~~~~~~~~~~~~~~~~~~~~

Instead of including a Properties object directly, you can use a configuration file containing the name-value pairs for Properties. If you are using a configuration file containing a Properties object, use the following code format.

.. code:: java

    Properties props = ApplicationClient.parsePropertiesFile(new File("C:\\temp\\application.prop"));
    ApplicationClient myClient = new ApplicationClient(props);
    ...

The application configuration file must be in the following format:

::

    [application]
    org=$orgId
    id=$myApplication
    auth-method=apikey
    auth-key=$key
    auth-token=$token

----

Connecting to the Internet of Things Foundation
----------------------------------------------------

Connect to the Internet of Things Foundation by calling the *connect* function.

.. code:: java

    Properties props = ApplicationClient.parsePropertiesFile(new File("C:\\temp\\application.prop"));
    ApplicationClient myClient = new ApplicationClient(props);
    
    myClient.connect();
    

After the successful connection to the IoTF service, the application client can perform the following operations, like subscribing to device events, subscribing to device status, publishing device events and commands.

----

Subscribing to device events
-------------------------------------------------------------------------------
Events are the mechanism by which devices publish data to the Internet of Things Foundation. The device controls the content of the event and assigns a name for each event it sends.

When an event is received by the IoT Foundation the credentials of the connection on which the event was received are used to determine from which device the event was sent. With this architecture it is impossible for a device to impersonate another device.

By default, applications will subscribe to all events from all connected devices. Use the type, id, event and msgFormat parameters to control the scope of the subscription. A single client can support multiple subscriptions. The code samples below give examples of how to subscribe to devices dependent on device type, id, event and msgFormat parameters.

To subscribe to all events from all devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceEvents();

To subscribe to all events from all devices of a specific type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceEvents("iotsample-ardunio");

To subscribe to all events from a specific device
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceEvents("iotsample-ardunio", "00aabbccddee");

To subscribe to a specific event from two or more different devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceEvents("iotsample-ardunio", "00aabbccddee", "myEvent");
    myClient.subscribeToDeviceEvents("iotsample-ardunio", "10aabbccddee", "myEvent");

To subscribe to events published by a device in json format
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    client.connect()
    myClient.subscribeToDeviceEvents("iotsample-ardunio", "00aabbccddee", "myEvent", "json", 0);
    
----

Handling events from devices
-------------------------------------------------------------------------------
To process the events received by your subscriptions you need to register an event callback method. The messages are returned as an instance of the Event class which has the following properties:

* event.device - string (uniquely identifies the device across all types of devices in the organization)
* event.deviceType - string
* event.deviceId - string
* event.event - string
* event.format - string
* event.data - dict
* event.timestamp - datetime

A sample implementation of the Event callback,

.. code:: java

  import com.ibm.iotf.client.app.Event;
  import com.ibm.iotf.client.app.EventCallback;
  import com.ibm.iotf.client.app.Command;
  
  public class MyEventCallback implements EventCallback {
      public void processEvent(Event e) {
          System.out.println("Event:: " + e.getDeviceId() + ":" + e.getEvent() + ":" + e.getPayload());
      }
      
      public void processCommand(Command cmd) {
          System.out.println("Command " + cmd.getPayload());
      }
  }

Once the event callback is added to the ApplicationClient, the processEvent() method is invoked whenever any event is published on the subscribed criteria, The following snippet shows how to add the Event call back into ApplicationClient instance,

.. code:: java

    myClient.connect()
    myClient.setEventCallback(new MyEventCallback());
    myClient.subscribeToDeviceEvents();

Similar to subscribing to device events, the application can subscribe to commands that are sent to the devices. Following code snippet shows how to subscribe to all commands to all the devices in the organization:

.. code:: java

    myClient.connect()
    myClient.setEventCallback(new MyEventCallback());
    myClient.subscribeToDeviceCommands();

Overloaded methods are available to control the command subscription. The processCommand() method is called when a command is sent to the device that matches the command subscription. 

----

Subscribing to device status
-------------------------------------------------------------------------------
Similar to subscribing to device events, applications can subscribe to device status, like device connect and disconnect to Internet of Things Foundation. By default, this will subscribe to status updates for all connected devices. Use the Device Type and Device Id parameters to control the scope of the subscription. A single ApplicationClient can support multiple subscriptions.

Subscribe to status updates for all devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceStatus();


Subscribe to status updates for all devices of a specific type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceStatus("iotsample-ardunio");


Subscribe to status updates for two different devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code:: java

    myClient.connect();
    myClient.subscribeToDeviceStatus("iotsample-ardunio", "00aabbccddee");
    myClient.subscribeToDeviceStatus("iotsample-ardunio", "10aabbccddee");

----


Handling status updates from devices
-------------------------------------------------------------------------------
To process the status updates received by your subscriptions you need to register an status event callback method. The messages are returned as an instance of the Status class which contains the below mentioned properties:

The following properties are set for both "Connect" and "Disconnect" status events:
  
* status.clientAddr - string
* status.protocol - string
* status.clientId - string
* status.user - string
* status.time - java.util.Date
* status.action - string
* status.connectTime - java.util.Date
* status.port - integer

The following properties are only set when the action is "Disconnect":

* status.writeMsg - integer
* status.readMsg - integer
* status.reason - string
* status.readBytes - integer
* status.writeBytes - integer

A sample implementation of the Status callback,

.. code:: java

  private static class MyStatusCallback implements StatusCallback {
      
      public void processApplicationStatus(ApplicationStatus status) {
          System.out.println("Application Status = " + status.getPayload());
      }
      
      public void processDeviceStatus(DeviceStatus status) {
          if(status.getAction() == "Disconnect") {
              System.out.println("device: "+status.getDeviceId()
                                  + "  time: "+ status.getTime()
                                  + "  action: " + status.getAction()
                                  + "  reason: " + status.getReason());
          } else {
              System.out.println("device: "+status.getDeviceId()
                                  + "  time: "+ status.getTime()
                                  + "  action: " + status.getAction());
          }
      }
  }
	
Once the status callback is added to the ApplicationClient, the processDeviceStatus() method is invoked whenever any device is connected or disconnected from Internet of Things Foundation that matches the criteria, The following snippet shows how to add the status call back instance into ApplicationClient,

.. code:: java

    myClient.connect()
    myClient.setStatusCallback(new MyStatusCallback());
    myClient.subscribeToDeviceStatus();

----

As similar to device status, the application can subscribe to any other application connect or disconnect status as well. Following code snippet shows how to subscribe to the application status in the organization:

.. code:: java

    myClient.connect()
    myClient.setEventCallback(new MyEventCallback());
    myClient.subscribeToApplicationStatus();

Overloaded method is available to control the status subscription to a particular application. The processApplicationStatus() method is called whenever any application is connected or disconnected from Internet of Things Foundation that matches the criteria.

Publishing events from devices
-------------------------------------------------------------------------------
Applications can publish events as if they originated from a Device.

.. code:: java

    myClient.connect()
    
    //Generate the event to be published
    JsonObject event = new JsonObject();
    event.addProperty("name", "foo");
    event.addProperty("cpu",  60);
    event.addProperty("mem",  40);
    
    // publish the event on behalf of device
    myClient.publishEvent(deviceType, deviceId, "blink", event);

----

Publishing commands to devices
-------------------------------------------------------------------------------
Applications can publish commands to connected devices.

.. code:: java

    myClient.connect()
    
    //Generate the event to be published
    JsonObject data = new JsonObject();
    data.addProperty("name", "stop-rotation");
    data.addProperty("delay",  0);
    
    //Registered flow allows 0, 1 and 2 QoS
    myAppClient.publishCommand(deviceType, deviceId, "stop", data);

----

Examples
-------------
* `MQTTApplicationDeviceEventPublish <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/MQTTApplicationDeviceEventPublish.java>`__ - A sample application that shows how to publish device events.
* `RegisteredApplicationCommandPublish <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/RegisteredApplicationCommandPublish.java>`__ - A sample application that shows how to publish a command to a device.
* `RegisteredApplicationSubscribeSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/RegisteredApplicationSubscribeSample.java>`__ - A sample application that shows how to subscribe for various events like, device events, device commands, device status and application status.

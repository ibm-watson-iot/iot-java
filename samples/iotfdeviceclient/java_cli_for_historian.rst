===============================================================================
Java Client Library - ReST API Support (**Update In Progress**)
===============================================================================

Introduction
-------------------------------------------------------------------------------

This client library describes how to use Internet of Things Foundation API with the Java ibmiotf client library. For help with getting started with this module, see `Java Client Library - Introduction <../java/javaintro.html/>`__. 

This client library is divided into Four sections, all included within the library. This section contains information on how applications can use the Java ibmiotf Client Library to interact with your organization in the Internet of Things Foundation through ReST APIs.

The Device section contains information on how devices can publish events and handle commands using the Java ibmiotf Client Library. 

The Managed Device section contains information on how devices can connect to the Internet of Things Foundation Device Management service using Java ibmiotf Client Library and perform device management operations like firmware update, location update, and diagnostics update.

Application section contains information on how applications can use the Java ibmiotf Client Library to interact with devices.

Constructor
-------------------------------------------------------------------------------

The constructor builds the client instance, and accepts a Properties object containing the following definitions:

* org - Your organization ID
* auth-key - API key
* auth-token - API key token

The Properties object creates definitions which are used to interact with the Internet of Things Foundation module. 

The following code snippet shows how to construct the APIClient instance using the properties,

.. code:: java
    
    import com.ibm.iotf.client.api.ApiClient;
    ...
    Properties options = new Properties();
    options.put("org", "uguhsp");
    options.put("API-Key", "<API-Key>");
    options.put("Authentication-Token", "<Authentication-Token>");
    
    APIClient apiClient = new APIClient(props);
    ...

----

Response and Exception
----------------------

Each method in the APIClient responds with either a valid response (JSON or boolean) in the case of sucess or com.ibm.iotf.client.IoTFCReSTException in the case of failure. The IoTFCReSTException contains the following properties that application can parse to get more information about the failure.

* httpcode - HTTP Status Code
* message - Exception message containing the reason for the failure
* response - JsonElement containing the partial response if any otherwise null

So in the case of failure, application needs to parse the response to see if the action is partially successful or not.

Organization details
----------------------------------------------------

Applications can view the Organization details by using the following code snippet:

.. code:: java

    JsonObject orgDetail = apiClient.getOrganizationDetails();

Refer to the Organization Configuration section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the possible response and errorcodes.

----

Bulk device operations
----------------------------------------------------

Applications can use bulk opertions to get, add or remove devices in bulk from Internet of Things Foundation.

Get Devices in bulk
~~~~~~~~~~~~~~~~~~~

Use the following method to retrieve the devices in bulk that are already registered to Internet of Things Foundation.

.. code:: java

    JsonObject response = apiClient.getDevices();
    

The reponse will contain more paramters and application needs to retrieve the JSON element *results* from the response to get the array of devices returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used.

.. code:: java

    import org.apache.http.message.BasicNameValuePair;
    
    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
		parameters.add(new BasicNameValuePair("_sort","deviceId"));
		JsonObject response = apiClient.getDevices(parameters);
		
The above snippet sorts the response based on device id and uses the bookmark to page through the results.

Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the list of qery parameters to control the output and also the response mode.

Register Devices in bulk
~~~~~~~~~~~~~~~~~~~~~~~~

Use the following method to register multiple new devices to Internet of Things Foundation, each request can contain a maximum of 512KB.

.. code:: java

    // A sample JSON respresentation of a device to be added
    
    private final static String deviceToBeAdded = "{\"typeId\": \"SampleDT\",\"deviceId\": "
			+ "\"RasPi100\",\"authToken\": \"password\",\"deviceInfo\": {\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi01 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"    },    "
			+ "\"location\": {\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"    "
			+ "},    \"metadata\": {}}";
		
		....
		
    JsonElement input = new JsonParser().parse(deviceToBeAdded);
		JsonArray arryOfDevicesToBeAdded = new JsonArray();
		arryOfDevicesToBeAdded.add(input);
		
		JsonArray response = apiClient.bulkDevicesAdd(arryOfDevicesToBeAdded);
    
The response will contain the generated authentication tokens for all devices. Application must make sure to record these tokens when processing the response. The Internet of Things Foundation will not able to retrieve lost authentication tokens. 

Delete Devices in bulk
~~~~~~~~~~~~~~~~~~~~~~~~

Use the following method to delete multiple devices from Internet of Things Foundation, each request can contain a maximum of 512KB.

.. code:: java

    // A sample JSON respresentation of a device to be deleted
    private final static String deviceToBeDeleted1 = "{\"typeId\": \"SampleDT\", \"deviceId\": \"RasPi100\"}";
    private final static String deviceToBeDeleted2 = "{\"typeId\": \"SampleDT\", \"deviceId\": \"RasPi101\"}";
    
    ....
    
    JsonElement device1 = new JsonParser().parse(deviceToBeDeleted1);
		JsonElement device2 = new JsonParser().parse(deviceToBeDeleted2);
		JsonArray arryOfDevicesToBeDeleted = new JsonArray();
		arryOfDevicesToBeDeleted.add(device1);
		arryOfDevicesToBeDeleted.add(device2);
		
		JsonArray devices = apiClient.bulkDevicesRemove(arryOfDevicesToBeDeleted);
	
Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the list of qery parameters to control the output and also the response mode.

----

Examples
-------------
* `RegisteredApplicationSubscribeSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/RegisteredApplicationSubscribeSample.java>`__ - A sample application that shows how to subscribe for various events like, device events, device commands, device status and application status.

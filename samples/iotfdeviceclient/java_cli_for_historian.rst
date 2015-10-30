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

Method getDevices() can be used to retrieve all the registered devices in an organization from Internet of Things Foundation, each request can contain a maximum of 512KB. For example,

.. code:: java

    JsonObject response = apiClient.getDevices();
    

The reponse will contain more paramters and application needs to retrieve the JSON element *results* from the response to get the array of devices returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    import org.apache.http.message.BasicNameValuePair;
    
    ...
    
    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    parameters.add(new BasicNameValuePair("_sort","deviceId"));
    
    JsonObject response = apiClient.getDevices(parameters);
		
The above snippet sorts the response based on device id and uses the bookmark to page through the results.

Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the list of query parameters to control the output and also the response mode.

Register Devices in bulk
~~~~~~~~~~~~~~~~~~~~~~~~

Method bulkDevicesAdd() can be used to register one or more devices to Internet of Things Foundation, each request can contain a maximum of 512KB. For example,

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

Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the response codes and model.

Delete Devices in bulk
~~~~~~~~~~~~~~~~~~~~~~~~

Method bulkDevicesRemove() can be used delete multiple devices from Internet of Things Foundation, each request can contain a maximum of 512KB. For example,

.. code:: java

    // A sample JSON respresentation of a device to be deleted
    private final static String deviceToBeDeleted1 = "{\"typeId\": \"SampleDT\", \"deviceId\": \"RasPi100\"}";
    private final static String deviceToBeDeleted2 = "{\"typeId\": \"SampleDT\", \"deviceId\": \"RasPi101\"}";
    
    ....
    
    // Create a JSON array by adding both devices that needs to be removed
    JsonElement device1 = new JsonParser().parse(deviceToBeDeleted1);
    JsonElement device2 = new JsonParser().parse(deviceToBeDeleted2);
    JsonArray arryOfDevicesToBeDeleted = new JsonArray();
    arryOfDevicesToBeDeleted.add(device1);
    arryOfDevicesToBeDeleted.add(device2);
    
    JsonArray devices = apiClient.bulkDevicesRemove(arryOfDevicesToBeDeleted);
	
Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the response codes and model.

----

Device Type operations
----------------------------------------------------

Applications can use device type opertions to list all, create, delete, view and update device types in Internet of Things Foundation.

Get all Device Types
~~~~~~~~~~~~~~~~~~~~

Method getAllDeviceTypes() can be used to retrieve all the registered device types in an organization from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getAllDeviceTypes(parameters);
    

The reponse will contain more paramters and application needs to retrieve the JSON element *results* from the response to get the array of device types returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    import org.apache.http.message.BasicNameValuePair;
    
    ...
    
    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    parameters.add(new BasicNameValuePair("_sort","id"));
    
    JsonObject response = apiClient.getAllDeviceTypes(parameters);
		
The above snippet sorts the response based on device type id and uses the bookmark to page through the results.

Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the list of query parameters to control the output and also the response mode.

Add a Device Type
~~~~~~~~~~~~~~~~~

Method addDeviceType() can be used to register a device type to Internet of Things Foundation. For example,

.. code:: java

    // A sample JSON respresentation of a device type to be added
    
    private final static String deviceTypeToBeAdded = "{\"id\": \"SampleDT\",\"description\": "
			+ "\"SampleDT\",\"deviceInfo\": {\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"},\"metadata\": {}}";
    
    ....
		
    JsonElement type = new JsonParser().parse(deviceTypeToBeAdded);
    JsonObject response = apiClient.addDeviceType(type);
    
Application can use a overloaded method that accepts more parameters to add a device type. For example,

.. code:: java

    // JSON representation of DeviceInfo and Metadata
    private final static String deviceInfoToBeAdded = "{\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"}";
    private final static String metaDataToBeAdded = "{\"hello\": \"I'm metadata\"}";

    ....
    
    JsonParser parser = new JsonParser();
    JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
    JsonElement metadata = parser.parse(metaDataToBeAdded);
    JsonObject response = apiClient.addDeviceType("SampleDT", "sample description", deviceInfo, metadata);
    
Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the response code and model.

Delete a Device Type
~~~~~~~~~~~~~~~~~~~~

Method deleteDeviceType() can be used to delete a device type from Internet of Things Foundation. For example,

.. code:: java

    boolean status = this.apiClient.deleteDeviceType("SampleDT");
    
Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the response code.


Get a Device Type
~~~~~~~~~~~~~~~~~~~~

Method getDeviceType() can be used to retrieve a device type from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = this.apiClient.getDeviceType("SampleDT");
    
Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the response code and response model.

Update a Device Type
~~~~~~~~~~~~~~~~~~~~

Method updateDeviceType() can be used to modify one or more properties of a device type. The properties that needs to be modified should be passed in JSON format, For example,

.. code:: java
    
    JsonObject json = new JsonObject();
    json.addProperty("description", "Hello, I'm updated description");
    JsonObject response = this.apiClient.updateDeviceType("SampleDT", json);

Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for more information about the Json format to be passed and the response.

----
Examples
-------------
* `RegisteredApplicationSubscribeSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/RegisteredApplicationSubscribeSample.java>`__ - A sample application that shows how to subscribe for various events like, device events, device commands, device status and application status.

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

Each method in the APIClient responds with either a valid response (JSON or boolean) in the case of success or com.ibm.iotf.client.IoTFCReSTException in the case of failure. The IoTFCReSTException contains the following properties that application can parse to get more information about the failure.

* httpcode - HTTP Status Code
* message - Exception message containing the reason for the failure
* response - JsonElement containing the partial response if any otherwise null

So in the case of failure, application needs to parse the response to see if the action is partially successful or not.

Organization details
----------------------------------------------------

Applications can view the Organization details by using the following code snippet:

.. code:: java

    JsonObject orgDetail = apiClient.getOrganizationDetails();

Refer to the Organization Configuration section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

----

Bulk device operations
----------------------------------------------------

Applications can use bulk operations to get, add or remove devices in bulk from Internet of Things Foundation.

Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

Get Devices in bulk
~~~~~~~~~~~~~~~~~~~

Method getAllDevices() can be used to retrieve all the registered devices in an organization from Internet of Things Foundation, each request can contain a maximum of 512KB. For example,

.. code:: java

    JsonObject response = apiClient.getAllDevices();
    

The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of devices returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    import org.apache.http.message.BasicNameValuePair;
    
    ...
    
    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    parameters.add(new BasicNameValuePair("_sort","deviceId"));
    
    JsonObject response = apiClient.getAllDevices(parameters);
		
The above snippet sorts the response based on device id and uses the bookmark to page through the results.

Register Devices in bulk
~~~~~~~~~~~~~~~~~~~~~~~~

Method addMultipleDevices() can be used to register one or more devices to Internet of Things Foundation, each request can contain a maximum of 512KB. For example,

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
    
    JsonArray response = apiClient.addMultipleDevices(arryOfDevicesToBeAdded);
    
The response will contain the generated authentication tokens for all devices. Application must make sure to record these tokens when processing the response. The Internet of Things Foundation will not able to retrieve lost authentication tokens. 

Delete Devices in bulk
~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteMultipleDevices() can be used to delete multiple devices from Internet of Things Foundation, each request can contain a maximum of 512KB. For example,

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
    
    JsonArray devices = apiClient.deleteMultipleDevices(arryOfDevicesToBeDeleted);
	
----

Device Type operations
----------------------------------------------------

Applications can use device type operations to list all, create, delete, view and update device types in Internet of Things Foundation.

Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

Get all Device Types
~~~~~~~~~~~~~~~~~~~~~~~~

Method getAllDeviceTypes() can be used to retrieve all the registered device types in an organization from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getAllDeviceTypes();
    
The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of device types returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    parameters.add(new BasicNameValuePair("_sort","id"));
    
    JsonObject response = apiClient.getAllDeviceTypes(parameters);
		
The above snippet sorts the response based on device type id and uses the bookmark to page through the results.

Add a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

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
    
Delete a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDeviceType() can be used to delete a device type from Internet of Things Foundation. For example,

.. code:: java

    boolean status = this.apiClient.deleteDeviceType("SampleDT");
    
Get a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceType() can be used to retrieve a device type from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = this.apiClient.getDeviceType("SampleDT");
    
Update a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

Method updateDeviceType() can be used to modify one or more properties of a device type. The properties that needs to be modified should be passed in JSON format, For example,

.. code:: java
    
    JsonObject json = new JsonObject();
    json.addProperty("description", "Hello, I'm updated description");
    JsonObject response = this.apiClient.updateDeviceType("SampleDT", json);

----

Device operations
----------------------------------------------------

Applications can use device operations to list, add, remove, view, update, view location and view management information of a device in Internet of Things Foundation.

Refer to the Device section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

Get Devices of a particular Device Type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDevices() can be used to retrieve all the devices of a particular device type in an organization from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getDevices("SampleDT");
    
The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of devices returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    parameters.add(new BasicNameValuePair("_sort","deviceId"));
    
    JsonObject response = apiClient.getDevices("SampleDT", parameters);
		
The above snippet sorts the response based on device id and uses the bookmark to page through the results.

Add a Device
~~~~~~~~~~~~~~~~~~~~~~~

Method registerDevice() can be used to register a device to Internet of Things Foundation. For example,

.. code:: java

    // A sample JSON respresentation of different properties of a Device to be added
    
    private final static String locationToBeAdded = "{\"longitude\": 0, \"latitude\": 0, \"elevation\": "
			+ "0,\"measuredDateTime\": \"2015-23-07T11:23:23+00:00\"}";
	
    private final static String deviceInfoToBeAdded = "{\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My RasPi100 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"}";
    ....
		
    JsonParser parser = new JsonParser();
    JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
    JsonElement location = parser.parse(locationToBeAdded);
    JsonObject response = this.apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, "Password", 
					deviceInfo, location, null);

Application can use a overloaded method that accepts entire device properties in one JSON element and registers the device,

.. code:: java

    JsonParser parser = new JsonParser();
    // deviceToBeAdded contains the JSON representation of device properties
    JsonElement input = parser.parse(deviceToBeAdded); 
    
    JsonObject response = apiClient.registerDevice(DEVICE_TYPE, input);
    
Delete a Device
~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDevice() can be used to delete a device from Internet of Things Foundation. For example,

.. code:: java

    status = apiClient.deleteDevice("SampleDT", "RasPi100");
    
Get a Device
~~~~~~~~~~~~~~~~~~~~~~~~

Method getDevice() can be used to retrieve a device from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getDevice("SampleDT", "RasPi100");
    
Update a Device
~~~~~~~~~~~~~~~~~~~~~~~~

Method updateDevice() can be used to modify one or more properties of a device. The properties that needs to be modified should be passed in JSON format, For example, to update the device metadata,

.. code:: java
    
    JsonObject metadata = new JsonObject();
    metadata.addProperty("Hi", "Hello, I'm updated metadata");
    JsonObject updatedMetadata = new JsonObject();
    updatedMetadata.add("metadata", metadata);
    
    JsonObject response = apiClient.updateDevice("Sample DT", "RasPi100", updatedMetadata);

Get Location Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceLocation() can be used to get the location information of a device. For example, 

.. code:: java
    
    JsonObject response = apiClient.getDeviceLocation("Sample DT", "RasPi100");

Update Location Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method updateDeviceLocation() can be used to modify the location information for a device. If no date is supplied, the entry is added with the current date and time. For example,

.. code:: java
    
    private final static String newlocationToBeAdded = "{\"longitude\": 10, \"latitude\": 20, \"elevation\": 0}";
    
    ...
    
    JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
    JsonObject response = apiClient.updateDeviceLocation("SampleDT", "RasPi100", newLocation);

Get Device Management Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceMgmtInformation() can be used to get the device management information for a device. For example, 

.. code:: java
    
    JsonObject response = apiClient.getDeviceMgmtInformation("Sample DT", "RasPi100");

----

Device diagnostic operations
----------------------------------------------------

Applications can use Device diagnostic operations to clear logs, retrieve logs, add log information, delete logs, get specific log, clear error codes, get device error codes and add an error code to Internet of Things Foundation.

Refer to the Device Diagnostics section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

Get Diagnostic logs
~~~~~~~~~~~~~~~~~~~~~~

Method getAllDiagnosticLogs() can be used to get all diagnostic logs of the device. For example,

.. code:: java

    JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
    
Clear Diagnostic logs 
~~~~~~~~~~~~~~~~~~~~~~

Method clearDiagnosticLogs() can be used to clear the diagnostic logs of the device. For example,

.. code:: java

    boolean status = apiClient.clearDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);
    
Add a Diagnostic log
~~~~~~~~~~~~~~~~~~~~~~

Method addDiagnosticLog() can be used to add an entry in the log of diagnostic information for the device. The log may be pruned as the new entry is added. If no date is supplied, the entry is added with the current date and time. For example,

.. code:: java

    private static final String logToBeAdded = "{\"message\": \"Sample log\",\"severity\": 0,\"data\": "
			+ "\"sample data\",\"timestamp\": \"2015-10-24T04:17:23.889Z\"}";

    ....
    
    JsonArray response = apiClient.getAllDiagnosticLogs(DEVICE_TYPE, DEVICE_ID);

Get a Diagnostic log
~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDiagnosticLog() can be used to retrieve a diagnostic log based on the log id. For example,

.. code:: java

    JsonObject log = apiClient.getDiagnosticLog(DEVICE_TYPE, DEVICE_ID, "<logid>");
    
Delete a Diagnostic log
~~~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDiagnosticLog() can be used to delete a diagnostic log based on the log id. For example,

.. code:: java

    boolean status = apiClient.deleteDiagnosticLog(DEVICE_TYPE, DEVICE_ID, "<logid>");
    

Clear Diagnostic ErrorCodes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method clearDiagnosticErrorCodes() can be used to clear the list of error codes of the device. The list is replaced with a single error code of zero. For example,

.. code:: java

    boolean status = apiClient.clearDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);
    
Get Diagnostic ErrorCodes
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getAllDiagnosticErrorCodes() can be used to retrieve all diagnostic ErrorCodes of the device. For example,

.. code:: java

    JsonArray response = apiClient.getAllDiagnosticErrorCodes(DEVICE_TYPE, DEVICE_ID);

Add a Diagnostic ErrorCode
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method addDiagnosticLog() can be used to add an error code to the list of error codes for the device. The list may be pruned as the new entry is added. For example,

.. code:: java

    boolean status = this.apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, 10, new Date());

An overloaded method can be used to add rhe error code in JSON format as well,

.. code:: java

    private static final String errorcodeToBeAdded = "{\"errorCode\": 100,\"timestamp\": "
			+ "\"2015-10-24T04:17:23.892Z\"}";
	
    JsonParser parser = new JsonParser();
    JsonElement errorcode = parser.parse(errorcodeToBeAdded);
    boolean status = this.apiClient.addDiagnosticErrorCode(DEVICE_TYPE, DEVICE_ID, errorcode);

----

Connection problem determination
----------------------------------

Method getDeviceConnectLogs() can be used to list connection log events for a device to aid in diagnosing connectivity problems. The entries record successful connection, unsuccessful connection attempts, intentional disconnection and server-initiated disconnection.

.. code:: java

    JsonArray response = apiClient.getDeviceConnectLogs(DEVICE_TYPE, DEVICE_ID);

Refer to the Problem Determination section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

----

Historical Event Retrieval
----------------------------------
Application can use this operation to view events from all devices, view events from a device type and view events for a specific device.

Refer to the Historical Event Retrieval section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the response model and code.

View events from all devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalEvents() can be used to view events across all devices registered to the organization.

.. code:: java

    JsonElement response = apiClient.getHistoricalEvents();

The response will contain more parameters and application needs to retrieve the JSON element *events* from the response to get the array of events returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    parameters.add(new BasicNameValuePair("evt_type", "blink"));
    parameters.add(new BasicNameValuePair("start", "1445420849839"));
    
    JsonElement response = this.apiClient.getHistoricalEvents(parameters);

The above snippet returns the events which are of type *blink* and received after time *1445420849839*.

View events from a device type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalEvents() can be used to view events from all the devices of a particular device type. 

.. code:: java

    JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE);

The response will contain more parameters and application needs to retrieve the JSON element *events* from the response to get the array of events returned. As mentioned in the *view events from all devices* section, the overloaded method can be used to control the output.

.. code:: java

    parameters.add(new BasicNameValuePair("evt_type", "blink"));
    parameters.add(new BasicNameValuePair("summarize", "{cpu,mem}"));
    parameters.add(new BasicNameValuePair("summarize_type", "avg"));
    
    JsonElement response = this.apiClient.getHistoricalEvents("SampleDT", parameters);
			
The above snippet returns the events which are of device type *SampleDT*, event type *blink* and aggregates the fields *cpu* & *mem* and computes the average.

View events from a device
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalEvents() can be used to view events from a specific device.

.. code:: java

    JsonElement response = this.apiClient.getHistoricalEvents(DEVICE_TYPE, DEVICE_ID);

The response will contain more parameters and application needs to retrieve the JSON element *events* from the response to get the array of events returned. As mentioned in the *view events from all devices* section, the overloaded method can be used to control the output.

.. code:: java

    parameters.add(new BasicNameValuePair("evt_type", "blink"));
    parameters.add(new BasicNameValuePair("summarize", "{cpu,mem}"));
    parameters.add(new BasicNameValuePair("summarize_type", "avg"));
    
    JsonElement response = apiClient.getHistoricalEvents("SampleDT", "RasPi100", parameters);
			
The above snippet returns the events which are of device *RasPi100*, event type *blink* and aggregates the fields *cpu* & *mem* and computes the average.

----

Device Management request operations
----------------------------------------------------

Applications can use the device management operations to list all device management requests, initiate a request, clear request status, get details of a request, get list of request statuses for each affected device and get request status for a specific device.

Get all Device management requests
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getAllMgmtRequests() can be used to retrieve the list of device management requests, which can be in progress or recently completed. For example,

.. code:: java

    JsonObject response = apiClient.getAllMgmtRequests(parameters);
    
The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of device types returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    
    JsonObject response = apiClient.getAllMgmtRequests(parameters);
		
The above snippet uses the bookmark to page through the results.

Initiate a Device management request
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method initiateMgmtRequest() can be used to initiate a device management request, such as reboot. For example,

.. code:: java

    // Json representation of a reboot request
    private static final String rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
			+ "\"devices\": [ {\"typeId\": \"SampleDT\","
			+ "\"deviceId\": \"RasPi100\"}]}";
    ....
    
    JsonObject reboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
    boolean response = this.apiClient.initiateMgmtRequest(reboot);
    
----

Examples
-------------
* `RegisteredApplicationSubscribeSample <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/RegisteredApplicationSubscribeSample.java>`__ - A sample application that shows how to subscribe for various events like, device events, device commands, device status and application status.

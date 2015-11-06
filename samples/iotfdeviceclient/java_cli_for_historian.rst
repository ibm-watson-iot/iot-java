===============================================================
Java Client Library - Internet of Things Foundation API Support 
===============================================================
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

Application can use method getOrganizationDetails() to view the Organization details:

.. code:: java

    JsonObject orgDetail = apiClient.getOrganizationDetails();

Refer to the Organization Configuration section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

----

Bulk device operations
----------------------------------------------------

Applications can use bulk operations to get, add or remove devices in bulk from Internet of Things Foundation.

Refer to the Bulk Operations section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

Get Devices in bulk
~~~~~~~~~~~~~~~~~~~

Method getAllDevices() can be used to retrieve all the registered devices in an organization from Internet of Things Foundation, each request can contain a maximum of 512KB. 

.. code:: java

    JsonObject response = apiClient.getAllDevices();
    

The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of devices returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

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

Method addMultipleDevices() can be used to register one or more devices to Internet of Things Foundation, each request can contain a maximum of 512KB. For example, the following sample shows how to add a device using the bulk operation.

.. code:: java

    // A sample JSON respresentation of a device to be added
    
    private final static String deviceToBeAdded = "{\"typeId\": \"iotsample-ardunio\",\"deviceId\": "
			+ "\"ardunio01\",\"authToken\": \"password\",\"deviceInfo\": {\"serialNumber\": "
			+ "\"10087\",\"manufacturer\": \"IBM\",\"model\": \"7865\",\"deviceClass\": "
			+ "\"A\",\"description\": \"My ardunio01 Device\",\"fwVersion\": \"1.0.0\","
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

Method deleteMultipleDevices() can be used to delete multiple devices from Internet of Things Foundation, each request can contain a maximum of 512KB. For example, the following sample shows how to delete 2 devices using the bulk operation.

.. code:: java

    // A sample JSON respresentation of a device to be deleted
    private final static String deviceToBeDeleted1 = "{\"typeId\": \"iotsample-ardunio\", \"deviceId\": \"ardunio01\"}";
    private final static String deviceToBeDeleted2 = "{\"typeId\": \"iotsample-ardunio\", \"deviceId\": \"ardunio02\"}";
    
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

Refer to the Device Types section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

Get all Device Types
~~~~~~~~~~~~~~~~~~~~~~~~

Method getAllDeviceTypes() can be used to retrieve all the registered device types in an organization from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getAllDeviceTypes();
    
The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of device types returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

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
    
    private final static String deviceTypeToBeAdded = "{\"id\": \"iotsample-ardunio\",\"description\": "
			+ "\"iotsample-ardunio\",\"deviceInfo\": {\"fwVersion\": \"1.0.0\",\"hwVersion\": \"1.0\"},\"metadata\": {}}";
    
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
    JsonObject response = apiClient.addDeviceType("iotsample-ardunio", "sample description", deviceInfo, metadata);
    
Delete a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDeviceType() can be used to delete a device type from Internet of Things Foundation. For example,

.. code:: java

    boolean status = this.apiClient.deleteDeviceType("iotsample-ardunio");
    
Get a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceType() can be used to retrieve a device type from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = this.apiClient.getDeviceType("iotsample-ardunio");
    
Update a Device Type
~~~~~~~~~~~~~~~~~~~~~~~~

Method updateDeviceType() can be used to modify one or more properties of a device type. The properties that needs to be modified should be passed in JSON format, For example, following sample shows how to update the *description* of a device type,

.. code:: java
    
    JsonObject json = new JsonObject();
    json.addProperty("description", "Hello, I'm updated description");
    JsonObject response = this.apiClient.updateDeviceType("iotsample-ardunio", json);

----

Device operations
----------------------------------------------------

Applications can use device operations to list, add, remove, view, update, view location and view management information of a device in Internet of Things Foundation.

Refer to the Device section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

Get Devices of a particular Device Type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDevices() can be used to retrieve all the devices of a particular device type in an organization from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getDevices("iotsample-ardunio");
    
The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of devices returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    parameters.add(new BasicNameValuePair("_sort","deviceId"));
    
    JsonObject response = apiClient.getDevices("iotsample-ardunio", parameters);
		
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
			+ "\"A\",\"description\": \"My ardunio01 Device\",\"fwVersion\": \"1.0.0\","
			+ "\"hwVersion\": \"1.0\",\"descriptiveLocation\": \"EGL C\"}";
    ....
		
    JsonParser parser = new JsonParser();
    JsonElement deviceInfo = parser.parse(deviceInfoToBeAdded);
    JsonElement location = parser.parse(locationToBeAdded);
    JsonObject response = this.apiClient.registerDevice(iotsample-ardunio, ardunio01, "Password", 
					deviceInfo, location, null);

Application can use a overloaded method that accepts entire device properties in one JSON element and registers the device,

.. code:: java

    JsonParser parser = new JsonParser();
    // deviceToBeAdded contains the JSON representation of device properties
    JsonElement input = parser.parse(deviceToBeAdded); 
    
    JsonObject response = apiClient.registerDevice(iotsample-ardunio, input);
    
Delete a Device
~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDevice() can be used to delete a device from Internet of Things Foundation. For example,

.. code:: java

    status = apiClient.deleteDevice("iotsample-ardunio", "ardunio01");
    
Get a Device
~~~~~~~~~~~~~~~~~~~~~~~~

Method getDevice() can be used to retrieve a device from Internet of Things Foundation. For example,

.. code:: java

    JsonObject response = apiClient.getDevice("iotsample-ardunio", "ardunio01");
    
Update a Device
~~~~~~~~~~~~~~~~~~~~~~~~

Method updateDevice() can be used to modify one or more properties of a device. The properties that needs to be modified should be passed in JSON format, For example, following sample shows how to update a device metadata,

.. code:: java
    
    JsonObject metadata = new JsonObject();
    metadata.addProperty("Hi", "Hello, I'm updated metadata");
    JsonObject updatedMetadata = new JsonObject();
    updatedMetadata.add("metadata", metadata);
    
    JsonObject response = apiClient.updateDevice("iotsample-ardunio", "ardunio01", updatedMetadata);

Get Location Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceLocation() can be used to get the location information of a device. For example, 

.. code:: java
    
    JsonObject response = apiClient.getDeviceLocation("iotsample-ardunio", "ardunio01");

Update Location Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method updateDeviceLocation() can be used to modify the location information for a device. If no date is supplied, the entry is added with the current date and time. For example,

.. code:: java
    
    private final static String newlocationToBeAdded = "{\"longitude\": 10, \"latitude\": 20, \"elevation\": 0}";
    
    ...
    
    JsonElement newLocation = new JsonParser().parse(newlocationToBeAdded);
    JsonObject response = apiClient.updateDeviceLocation("iotsample-ardunio", "ardunio01", newLocation);

Get Device Management Information
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceManagementInformation() can be used to get the device management information for a device. For example, 

.. code:: java
    
    JsonObject response = apiClient.getDeviceManagementInformation("iotsample-ardunio", "ardunio01");

----

Device diagnostic operations
----------------------------------------------------

Applications can use Device diagnostic operations to clear logs, retrieve logs, add log information, delete logs, get specific log, clear error codes, get device error codes and add an error code to Internet of Things Foundation.

Refer to the Device Diagnostics section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

Get Diagnostic logs
~~~~~~~~~~~~~~~~~~~~~~

Method getAllDiagnosticLogs() can be used to get all diagnostic logs of the device. For example,

.. code:: java

    JsonArray response = apiClient.getAllDiagnosticLogs(iotsample-ardunio, ardunio01);
    
Clear Diagnostic logs 
~~~~~~~~~~~~~~~~~~~~~~

Method clearDiagnosticLogs() can be used to clear the diagnostic logs of the device. For example,

.. code:: java

    boolean status = apiClient.clearDiagnosticLogs(iotsample-ardunio, ardunio01);
    
Add a Diagnostic log
~~~~~~~~~~~~~~~~~~~~~~

Method addDiagnosticLog() can be used to add an entry in the log of diagnostic information for the device. The log may be pruned as the new entry is added. If no date is supplied, the entry is added with the current date and time. For example,

.. code:: java

    private static final String logToBeAdded = "{\"message\": \"Sample log\",\"severity\": 0,\"data\": "
			+ "\"sample data\",\"timestamp\": \"2015-10-24T04:17:23.889Z\"}";

    ....
    
    JsonArray response = apiClient.getAllDiagnosticLogs(iotsample-ardunio, ardunio01);

Get a Diagnostic log
~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDiagnosticLog() can be used to retrieve a diagnostic log based on the log id. For example,

.. code:: java

    JsonObject log = apiClient.getDiagnosticLog(iotsample-ardunio, ardunio01, "<logid>");
    
Delete a Diagnostic log
~~~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDiagnosticLog() can be used to delete a diagnostic log based on the log id. For example,

.. code:: java

    boolean status = apiClient.deleteDiagnosticLog(iotsample-ardunio, ardunio01, "<logid>");
    

Clear Diagnostic ErrorCodes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method clearDiagnosticErrorCodes() can be used to clear the list of error codes of the device. The list is replaced with a single error code of zero. For example,

.. code:: java

    boolean status = apiClient.clearDiagnosticErrorCodes(iotsample-ardunio, ardunio01);
    
Get Diagnostic ErrorCodes
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getAllDiagnosticErrorCodes() can be used to retrieve all diagnostic ErrorCodes of the device. For example,

.. code:: java

    JsonArray response = apiClient.getAllDiagnosticErrorCodes(iotsample-ardunio, ardunio01);

Add a Diagnostic ErrorCode
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method addDiagnosticLog() can be used to add an error code to the list of error codes for the device. The list may be pruned as the new entry is added. For example,

.. code:: java

    boolean status = this.apiClient.addDiagnosticErrorCode(iotsample-ardunio, ardunio01, 10, new Date());

An overloaded method can be used to add rhe error code in JSON format as well,

.. code:: java

    private static final String errorcodeToBeAdded = "{\"errorCode\": 100,\"timestamp\": "
			+ "\"2015-10-24T04:17:23.892Z\"}";
	
    JsonParser parser = new JsonParser();
    JsonElement errorcode = parser.parse(errorcodeToBeAdded);
    boolean status = this.apiClient.addDiagnosticErrorCode(iotsample-ardunio, ardunio01, errorcode);

----

Connection problem determination
----------------------------------

Method getDeviceConnectionLogs() can be used to list connection log events for a device to aid in diagnosing connectivity problems. The entries record successful connection, unsuccessful connection attempts, intentional disconnection and server-initiated disconnection.

.. code:: java

    JsonArray response = apiClient.getDeviceConnectionLogs(iotsample-ardunio, ardunio01);

Refer to the Problem Determination section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

----

Historical Event Retrieval
----------------------------------
Application can use this operation to view events from all devices, view events from a device type and view events for a specific device.

Refer to the Historical Event Retrieval section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

View events from all devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalEvents() can be used to view events across all devices registered to the organization.

.. code:: java

    JsonElement response = apiClient.getHistoricalEvents();

The response will contain more parameters and application needs to retrieve the JSON element *events* from the response to get the array of events returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    parameters.add(new BasicNameValuePair("evt_type", "blink"));
    parameters.add(new BasicNameValuePair("start", "1445420849839"));
    
    JsonElement response = this.apiClient.getHistoricalEvents(parameters);

The above snippet returns the events which are of type *blink* and received after time *1445420849839*.

View events from a device type
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalEvents() can be used to view events from all the devices of a particular device type. 

.. code:: java

    JsonElement response = this.apiClient.getHistoricalEvents(iotsample-ardunio);

The response will contain more parameters and application needs to retrieve the JSON element *events* from the response to get the array of events returned. As mentioned in the *view events from all devices* section, the overloaded method can be used to control the output.

.. code:: java

    parameters.add(new BasicNameValuePair("evt_type", "blink"));
    parameters.add(new BasicNameValuePair("summarize", "{cpu,mem}"));
    parameters.add(new BasicNameValuePair("summarize_type", "avg"));
    
    JsonElement response = this.apiClient.getHistoricalEvents("iotsample-ardunio", parameters);
			
The above snippet returns the events which are of device type *iotsample-ardunio*, event type *blink* and aggregates the fields *cpu* & *mem* and computes the average.

View events from a device
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalEvents() can be used to view events from a specific device.

.. code:: java

    JsonElement response = this.apiClient.getHistoricalEvents(iotsample-ardunio, ardunio01);

The response will contain more parameters and application needs to retrieve the JSON element *events* from the response to get the array of events returned. As mentioned in the *view events from all devices* section, the overloaded method can be used to control the output.

.. code:: java

    parameters.add(new BasicNameValuePair("evt_type", "blink"));
    parameters.add(new BasicNameValuePair("summarize", "{cpu,mem}"));
    parameters.add(new BasicNameValuePair("summarize_type", "avg"));
    
    JsonElement response = apiClient.getHistoricalEvents("iotsample-ardunio", "ardunio01", parameters);
			
The above snippet returns the events which are of device *ardunio01*, event type *blink* and aggregates the fields *cpu* & *mem* and computes the average.

----

Device Management request operations
----------------------------------------------------

Applications can use the device management operations to list all device management requests, initiate a request, clear request status, get details of a request, get list of request statuses for each affected device and get request status for a specific device.

Refer to the Device Management Requests section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

Get all Device management requests
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getAllDeviceManagementRequests() can be used to retrieve the list of device management requests, which can be in progress or recently completed. For example,

.. code:: java

    JsonObject response = apiClient.getAllDeviceManagementRequests();
    
The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of device types returned. Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    
    JsonObject response = apiClient.getAllDeviceManagementRequests(parameters);
		
The above snippet uses the bookmark to page through the results.

Initiate a Device management request
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method initiateDeviceManagementRequest() can be used to initiate a device management request, such as reboot. For example,

.. code:: java

    // Json representation of a reboot request
    private static final String rebootRequestToBeInitiated = "{\"action\": \"device/reboot\","
			+ "\"devices\": [ {\"typeId\": \"iotsample-ardunio\","
			+ "\"deviceId\": \"ardunio01\"}]}";
    ....
    
    JsonObject reboot = (JsonObject) new JsonParser().parse(rebootRequestToBeInitiated);
    boolean response = this.apiClient.initiateDeviceManagementRequest(reboot);

The above snippet triggers a reboot request on device *ardunio01*. Similarly use the following JSON message to initiate a firmware download request,

.. code:: js

    {
	"action": "firmware/download",
	"parameters": [
	{
	    "name": "version",
	    "value": "<Firmware Version>"
	},
	{
	    "name": "name",
	    "value": "<Firmware Name>"
	},
	{
	    "name": "verifier",
            "value": "<MD5 checksum to verify the firmware image>"
	},
	{
	    "name": "uri",
	    "value": "<URL location from where the firmware to be download>"
	}
	],
	"devices": [
	{
	    "typeId": "iotsample-ardunio",
	    "deviceId": "ardunio01"
	}
	]
    }
    
And use the following JSON message to initiate a firmware update request on *ardunio01*,

.. code:: js

    {
 	"action": "firmware/update",
 	"devices": [
 	{
 	    "typeId": "iotsample-ardunio",
 	    "deviceId": "ardunio01"
 	}
 	]
    }

Refer to `this recipe <https://developer.ibm.com/recipes/tutorials/connect-raspberry-pi-as-managed-device-to-ibm-iot-foundation/>`__ to know more about how to update a service on Raspberry Pi using this ibmiotf Java Client Library.

Delete a Device management request
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method deleteDeviceManagementRequest() can be used to clear the status of a device management request. Application can use this operation to clear the status of a completed request, or an in-progress request which may never complete due to a problem. For example,

.. code:: java

    // Pass the Request ID of a device management request
    boolean status = this.apiClient.deleteDeviceManagementRequest("id");
    
Get details of a Device management request
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceManagementRequest() can be used to get the details of the device management request. For example,

.. code:: java

    // Pass the Request ID of a device management request
    JsonObject details = this.apiClient.getDeviceManagementRequest("id");
    

Get status of a Device management request
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceManagementRequestStatus() can be used to get a list of device management request device statuses. For example,

.. code:: java

    // Pass the Request ID of a device management request
    JsonObject details = apiClient.getDeviceManagementRequestStatus(id);

The response will contain more parameters and application needs to retrieve the JSON element *results* from the response to get the array of device statuses returned. Each row contains the status of the action whether the action is successful or not. The status is returned as integer and will contain one of the following possible values,

* Success
* In progress
* Failure
* Time out

Other parameters in the response are required to make further call, for example, the *_bookmark* element can be used to page through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.

In order to pass the *_bookmark* or any other condition, the overloaded method must be used. The overloaded method takes the parameters in the form of org.apache.http.message.BasicNameValuePair as shown below,

.. code:: java

    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("_bookmark","<bookmark>"));
    
    // Pass the Request ID of a device management request
    JsonObject details = apiClient.getDeviceManagementRequestStatus(id, parameters);

The above snippet uses the bookmark to page through the results.

Get status of a Device management request by Device
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDeviceManagementRequestStatusByDevice() can be used to get an individual device management request device status. For example,

.. code:: java

    // Pass the Request ID of a device management request along with Device type & Id
    JsonObject response = apiClient.getDeviceManagementRequestStatusByDevice(id, iotsample-ardunio, ardunio01);

----

Usage management
----------------------------------------------------

Applications can use the usage management operations to retrieve the number of active devices over a period of time, retrieve amount of storage used by historical event data, retrieve total amount of data used.

Refer to the Usage management section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the list of query parameters, the request & response model and http status code.

Get active devices
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getActiveDevices() can be used to retrieve the number of active devices over a period of time. For example,

.. code:: java
    
    String start = "2015-09-01";
    String end = "2015-10-01";
    JsonElement response = this.apiClient.getActiveDevices(start, end, true);

The above snippet returns the devices that are active between 2015-09-01 and 2015-10-01 with a daily breakdown.

Get Historical data usage
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getHistoricalDataUsage() can be used to retrieve the amount of storage being used by historical event data for a specified period of time. For example,

.. code:: java
    
    String start = "2015-09-01";
    String end = "2015-10-01";
    JsonElement response = this.apiClient.getHistoricalDataUsage(start, end, false);

The above snippet returns the amount of storage being used by historical event data between 2015-09-01 and 2015-10-01 without a daily breakdown.

Get data traffic
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Method getDataTraffic() can be used to retrieve the amount of data used for a specified period of time. For example,

.. code:: java
    
    String start = "2015-09-01";
    String end = "2015-10-01";
    JsonElement response = this.apiClient.getDataTraffic(start, end, false);

The above snippet returns the amount of data traffic between 2015-09-01 and 2015-10-01 but without a daily breakdown.

----

Service status
----------------------------------------------------

Method getServiceStatus() can be used to retrieve the organization-specific status of each of the services offered by the Internet of Things Foundation. 

.. code:: java
    
    JsonElement response = this.apiClient.getServiceStatus();

Refer to the Service status section of the `IBM IoT Foundation API <https://docs.internetofthings.ibmcloud.com/swagger/v0002.html>`__ for information about the response model and http status code.

----

Examples
-------------
* `SampleBulkAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleBulkAPIOperations.java>`__ - Sample that showcases how to get, add or remove devices in bulk from Internet of Things Foundation.
* `SampleDeviceTypeAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleDeviceTypeAPIOperations.java>`__ - Sample that showcases various Device Type API operations like list all, create, delete, view and update device types in Internet of Things Foundation.
* `SampleDeviceAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleDeviceAPIOperations.java>`__ - A sample that showcases various Device operations like list, add, remove, view, update, view location and view management information of a device in Internet of Things Foundation.
* `SampleDeviceDiagnosticsAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleDeviceDiagnosticsAPIOperations.java>`__ - A sample that showcases various Device Diagnostic operations like clear logs, retrieve logs, add log information, delete logs, get specific log, clear error codes, get device error codes and add an error code to Internet of Things Foundation.
* `SampleHistorianAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleHistorianAPIOperations.java>`__ - A sample that showcases how to retrieve historical events from Internet of Things Foundation.
* `SampleDeviceManagementAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleDeviceManagementAPIOperations.java>`__ - A sample that showcases various device management request operations that can be performed on Internet of Things Foundation.
* `SampleUsageManagementAPIOperations <https://github.com/ibm-messaging/iot-java/blob/master/samples/iotfdeviceclient/src/com/ibm/iotf/sample/client/application/api/SampleUsageManagementAPIOperations.java>`__ - A sample that showcases various Usage management operations that can be performed on Internet of Things Foundation.


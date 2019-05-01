# DeviceConfigurationApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deviceTypesTypeIdDevicesDeviceIdDelete**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesDeviceIdDelete) | **DELETE** /device/types/{typeId}/devices/{deviceId} | Remove device
[**deviceTypesTypeIdDevicesDeviceIdDevicesGet**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesDeviceIdDevicesGet) | **GET** /device/types/{typeId}/devices/{deviceId}/devices | Get devices that are connected through the gateway specified by id {deviceId}
[**deviceTypesTypeIdDevicesDeviceIdEdgestatusGet**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesDeviceIdEdgestatusGet) | **GET** /device/types/{typeId}/devices/{deviceId}/edgestatus | Return the status of containers from an edge node.
[**deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet) | **GET** /device/types/{typeId}/devices/{deviceId}/edgestatus/{serviceId} | Return the status of containers from an edge node filtering by service.
[**deviceTypesTypeIdDevicesDeviceIdGet**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesDeviceIdGet) | **GET** /device/types/{typeId}/devices/{deviceId} | Get device
[**deviceTypesTypeIdDevicesDeviceIdPut**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesDeviceIdPut) | **PUT** /device/types/{typeId}/devices/{deviceId} | Update device
[**deviceTypesTypeIdDevicesGet**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesGet) | **GET** /device/types/{typeId}/devices | List devices
[**deviceTypesTypeIdDevicesPost**](DeviceConfigurationApi.md#deviceTypesTypeIdDevicesPost) | **POST** /device/types/{typeId}/devices | Add device


<a name="deviceTypesTypeIdDevicesDeviceIdDelete"></a>
# **deviceTypesTypeIdDevicesDeviceIdDelete**
> deviceTypesTypeIdDevicesDeviceIdDelete(typeId, deviceId)

Remove device

Removes a device.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
try {
    apiInstance.deviceTypesTypeIdDevicesDeviceIdDelete(typeId, deviceId);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesDeviceIdDelete");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |

### Return type

null (empty response body)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesDeviceIdDevicesGet"></a>
# **deviceTypesTypeIdDevicesDeviceIdDevicesGet**
> DeviceListResponse deviceTypesTypeIdDevicesDeviceIdDevicesGet(typeId, deviceId)

Get devices that are connected through the gateway specified by id {deviceId}

Gets information on devices that are connected through the specified gateway (typeId, deviceId) to Watson IoT Platform.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
try {
    DeviceListResponse result = apiInstance.deviceTypesTypeIdDevicesDeviceIdDevicesGet(typeId, deviceId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesDeviceIdDevicesGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |

### Return type

[**DeviceListResponse**](DeviceListResponse.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesDeviceIdEdgestatusGet"></a>
# **deviceTypesTypeIdDevicesDeviceIdEdgestatusGet**
> EdgeStatusResponse deviceTypesTypeIdDevicesDeviceIdEdgestatusGet(typeId, deviceId)

Return the status of containers from an edge node.

Return the status of containers from an edge node.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;


DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
try {
    EdgeStatusResponse result = apiInstance.deviceTypesTypeIdDevicesDeviceIdEdgestatusGet(typeId, deviceId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesDeviceIdEdgestatusGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |

### Return type

[**EdgeStatusResponse**](EdgeStatusResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet"></a>
# **deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet**
> EdgeStatusResponseByService deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet(typeId, deviceId, serviceId)

Return the status of containers from an edge node filtering by service.

Return the status of containers from an edge node filtering by service.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;


DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
String serviceId = "serviceId_example"; // String | Service ID
try {
    EdgeStatusResponseByService result = apiInstance.deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet(typeId, deviceId, serviceId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesDeviceIdEdgestatusServiceIdGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |
 **serviceId** | **String**| Service ID |

### Return type

[**EdgeStatusResponseByService**](EdgeStatusResponseByService.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesDeviceIdGet"></a>
# **deviceTypesTypeIdDevicesDeviceIdGet**
> Device deviceTypesTypeIdDevicesDeviceIdGet(typeId, deviceId, expand)

Get device

Gets device details.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
List<String> expand = Arrays.asList("expand_example"); // List<String> | Optional extensions to expand
try {
    Device result = apiInstance.deviceTypesTypeIdDevicesDeviceIdGet(typeId, deviceId, expand);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesDeviceIdGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |
 **expand** | [**List&lt;String&gt;**](String.md)| Optional extensions to expand | [optional]

### Return type

[**Device**](Device.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesDeviceIdPut"></a>
# **deviceTypesTypeIdDevicesDeviceIdPut**
> DeviceConcise deviceTypesTypeIdDevicesDeviceIdPut(typeId, deviceId, deviceUpdate)

Update device

Updates a device.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
DeviceUpdateRequest deviceUpdate = new DeviceUpdateRequest(); // DeviceUpdateRequest | Device update
try {
    DeviceConcise result = apiInstance.deviceTypesTypeIdDevicesDeviceIdPut(typeId, deviceId, deviceUpdate);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesDeviceIdPut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |
 **deviceUpdate** | [**DeviceUpdateRequest**](DeviceUpdateRequest.md)| Device update |

### Return type

[**DeviceConcise**](DeviceConcise.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesGet"></a>
# **deviceTypesTypeIdDevicesGet**
> DeviceListResponse deviceTypesTypeIdDevicesGet(typeId, bookmark, limit, sort, facets, deviceId, statusAlertEnabled, gatewayTypeId, gatewayId, mgmtDormant, mgmtSupportsDeviceActions, mgmtSupportsFirmwareActions, registrationDate, deviceInfoDescription, deviceInfoDescriptiveLocation, deviceInfoSerialNumber, deviceInfoDeviceClass, deviceInfoFwVersion, deviceInfoHwVersion, deviceInfoManufacturer, deviceInfoModel)

List devices

Sorting can be performed on any of the following properties (sort order can be reversed by prefixing the property name with &#39;-&#39;): - typeId - deviceId - deviceInfo.description - deviceInfo.descriptiveLocation - deviceInfo.serialNumber - deviceInfo.deviceClass - deviceInfo.fwVersion - deviceInfo.hwVersion - deviceInfo.manufacturer - deviceInfo.model - mgmt.dormant - mgmt.supports.deviceActions - mgmt.supports.firmwareActions - registration.date - status.alert.enabled - status.alert.timestamp  The following facets are supported: - typeId - deviceInfo.deviceClass - deviceInfo.fwVersion - deviceInfo.hwVersion - deviceInfo.manufacturer - deviceInfo.model - mgmt.dormant - mgmt.supports.deviceActions - mgmt.supports.firmwareActions - status.alert.enabled

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
String bookmark = "bookmark_example"; // String | Used for paging through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.
BigDecimal limit = new BigDecimal(); // BigDecimal | Maximum number of results returned per page.
List<String> sort = Arrays.asList("sort_example"); // List<String> | Define the sort order of results.  Specify a comma-separated list of properties, enclosed within square brackets. Prefix field names with '-' to sort in descending order.  For example: property1,property2
List<String> facets = Arrays.asList("facets_example"); // List<String> | Define the facets to return.  For example: property1,property2
String deviceId = "deviceId_example"; // String | Optional filter of results by device ID
Boolean statusAlertEnabled = true; // Boolean | Optional filter of results by alert state
String gatewayTypeId = "gatewayTypeId_example"; // String | Optional filter of results by gateway type ID
String gatewayId = "gatewayId_example"; // String | Optional filter of results by gateway ID
Boolean mgmtDormant = true; // Boolean | Optional filter of results by dormant state
Boolean mgmtSupportsDeviceActions = true; // Boolean | Optional filter of results by support for device actions
Boolean mgmtSupportsFirmwareActions = true; // Boolean | Optional filter of results by support for firmware actions
Object registrationDate = null; // Object | Optional filter of results by registration date
String deviceInfoDescription = "deviceInfoDescription_example"; // String | Optional filter of results by device description
String deviceInfoDescriptiveLocation = "deviceInfoDescriptiveLocation_example"; // String | Optional filter of results by device location
String deviceInfoSerialNumber = "deviceInfoSerialNumber_example"; // String | Optional filter of results by serial number
String deviceInfoDeviceClass = "deviceInfoDeviceClass_example"; // String | Optional filter of results by device class
String deviceInfoFwVersion = "deviceInfoFwVersion_example"; // String | Optional filter of results by firmware version
String deviceInfoHwVersion = "deviceInfoHwVersion_example"; // String | Optional filter of results by hardware version
String deviceInfoManufacturer = "deviceInfoManufacturer_example"; // String | Optional filter of results by manufacturer name
String deviceInfoModel = "deviceInfoModel_example"; // String | Optional filter of results by device model
try {
    DeviceListResponse result = apiInstance.deviceTypesTypeIdDevicesGet(typeId, bookmark, limit, sort, facets, deviceId, statusAlertEnabled, gatewayTypeId, gatewayId, mgmtDormant, mgmtSupportsDeviceActions, mgmtSupportsFirmwareActions, registrationDate, deviceInfoDescription, deviceInfoDescriptiveLocation, deviceInfoSerialNumber, deviceInfoDeviceClass, deviceInfoFwVersion, deviceInfoHwVersion, deviceInfoManufacturer, deviceInfoModel);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **bookmark** | **String**| Used for paging through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined. | [optional]
 **limit** | **BigDecimal**| Maximum number of results returned per page. | [optional] [default to 25]
 **sort** | [**List&lt;String&gt;**](String.md)| Define the sort order of results.  Specify a comma-separated list of properties, enclosed within square brackets. Prefix field names with &#39;-&#39; to sort in descending order.  For example: property1,property2 | [optional]
 **facets** | [**List&lt;String&gt;**](String.md)| Define the facets to return.  For example: property1,property2 | [optional]
 **deviceId** | **String**| Optional filter of results by device ID | [optional]
 **statusAlertEnabled** | **Boolean**| Optional filter of results by alert state | [optional]
 **gatewayTypeId** | **String**| Optional filter of results by gateway type ID | [optional]
 **gatewayId** | **String**| Optional filter of results by gateway ID | [optional]
 **mgmtDormant** | **Boolean**| Optional filter of results by dormant state | [optional]
 **mgmtSupportsDeviceActions** | **Boolean**| Optional filter of results by support for device actions | [optional]
 **mgmtSupportsFirmwareActions** | **Boolean**| Optional filter of results by support for firmware actions | [optional]
 **registrationDate** | [**Object**](.md)| Optional filter of results by registration date | [optional]
 **deviceInfoDescription** | **String**| Optional filter of results by device description | [optional]
 **deviceInfoDescriptiveLocation** | **String**| Optional filter of results by device location | [optional]
 **deviceInfoSerialNumber** | **String**| Optional filter of results by serial number | [optional]
 **deviceInfoDeviceClass** | **String**| Optional filter of results by device class | [optional]
 **deviceInfoFwVersion** | **String**| Optional filter of results by firmware version | [optional]
 **deviceInfoHwVersion** | **String**| Optional filter of results by hardware version | [optional]
 **deviceInfoManufacturer** | **String**| Optional filter of results by manufacturer name | [optional]
 **deviceInfoModel** | **String**| Optional filter of results by device model | [optional]

### Return type

[**DeviceListResponse**](DeviceListResponse.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesPost"></a>
# **deviceTypesTypeIdDevicesPost**
> DeviceAdditionResponse deviceTypesTypeIdDevicesPost(typeId, device)

Add device

Adds a device.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceConfigurationApi apiInstance = new DeviceConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
DeviceAdditionRequest device = new DeviceAdditionRequest(); // DeviceAdditionRequest | Device to be added
try {
    DeviceAdditionResponse result = apiInstance.deviceTypesTypeIdDevicesPost(typeId, device);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceConfigurationApi#deviceTypesTypeIdDevicesPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **device** | [**DeviceAdditionRequest**](DeviceAdditionRequest.md)| Device to be added |

### Return type

[**DeviceAdditionResponse**](DeviceAdditionResponse.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


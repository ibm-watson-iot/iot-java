# DeviceBulkConfigurationApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**bulkDevicesAddPost**](DeviceBulkConfigurationApi.md#bulkDevicesAddPost) | **POST** /bulk/devices/add | Register multiple new devices
[**bulkDevicesGet**](DeviceBulkConfigurationApi.md#bulkDevicesGet) | **GET** /bulk/devices | List devices
[**bulkDevicesRemovePost**](DeviceBulkConfigurationApi.md#bulkDevicesRemovePost) | **POST** /bulk/devices/remove | Delete multiple devices


<a name="bulkDevicesAddPost"></a>
# **bulkDevicesAddPost**
> DeviceWithPasswordList bulkDevicesAddPost(devices)

Register multiple new devices

Register multiple new devices, each request can contain a maximum of 512 kB.  The response body will contain the generated authentication tokens for all devices. You must make sure to record these tokens when processing the response. We are not able to retrieve lost authentication tokens.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceBulkConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceBulkConfigurationApi apiInstance = new DeviceBulkConfigurationApi();
DeviceBulkRegistrationRequestList devices = new DeviceBulkRegistrationRequestList(); // DeviceBulkRegistrationRequestList | Devices to be registered
try {
    DeviceWithPasswordList result = apiInstance.bulkDevicesAddPost(devices);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceBulkConfigurationApi#bulkDevicesAddPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devices** | [**DeviceBulkRegistrationRequestList**](DeviceBulkRegistrationRequestList.md)| Devices to be registered |

### Return type

[**DeviceWithPasswordList**](DeviceWithPasswordList.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="bulkDevicesGet"></a>
# **bulkDevicesGet**
> DeviceListResponse bulkDevicesGet(bookmark, limit, sort, facets, typeId, deviceId, gatewayTypeId, gatewayId, statusAlertEnabled, registrationDate, mgmtDormant, mgmtSupportsDeviceActions, mgmtSupportsFirmwareActions, deviceInfoDescription, deviceInfoDescriptiveLocation, deviceInfoSerialNumber, deviceInfoDeviceClass, deviceInfoFwVersion, deviceInfoHwVersion, deviceInfoManufacturer, deviceInfoModel)

List devices

Sorting can be performed on any of the following properties (sort order can be reversed by prefixing the property name with &#39;-&#39;): - typeId - deviceId - deviceInfo.description - deviceInfo.descriptiveLocation - deviceInfo.serialNumber - deviceInfo.deviceClass - deviceInfo.fwVersion - deviceInfo.hwVersion - deviceInfo.manufacturer - deviceInfo.model - mgmt.dormant - mgmt.supports.deviceActions - mgmt.supports.firmwareActions - registration.date - status.alert.enabled - status.alert.timestamp  The following facets are supported: - typeId - deviceInfo.deviceClass - deviceInfo.fwVersion - deviceInfo.hwVersion - deviceInfo.manufacturer - deviceInfo.model - mgmt.dormant - mgmt.supports.deviceActions - mgmt.supports.firmwareActions - status.alert.enabled

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceBulkConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceBulkConfigurationApi apiInstance = new DeviceBulkConfigurationApi();
String bookmark = "bookmark_example"; // String | Used for paging through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.
BigDecimal limit = new BigDecimal(); // BigDecimal | Maximum number of results returned per page.
List<String> sort = Arrays.asList("sort_example"); // List<String> | Define the sort order of results.  Specify a comma-separated list of properties, enclosed within square brackets. Prefix field names with '-' to sort in descending order.  For example: property1,property2
List<String> facets = Arrays.asList("facets_example"); // List<String> | Define the facets to return.  For example: property1,property2
String typeId = "typeId_example"; // String | Optional filter of results by type ID
String deviceId = "deviceId_example"; // String | Optional filter of results by device ID
String gatewayTypeId = "gatewayTypeId_example"; // String | Optional filter of results by gateway type ID
String gatewayId = "gatewayId_example"; // String | Optional filter of results by gateway ID
Boolean statusAlertEnabled = true; // Boolean | Optional filter of results by alert state
Object registrationDate = null; // Object | Optional filter of results by registration date
Boolean mgmtDormant = true; // Boolean | Optional filter of results by dormant state
Boolean mgmtSupportsDeviceActions = true; // Boolean | Optional filter of results by support for device actions
Boolean mgmtSupportsFirmwareActions = true; // Boolean | Optional filter of results by support for firmware actions
String deviceInfoDescription = "deviceInfoDescription_example"; // String | Optional filter of results by device description
String deviceInfoDescriptiveLocation = "deviceInfoDescriptiveLocation_example"; // String | Optional filter of results by device location
String deviceInfoSerialNumber = "deviceInfoSerialNumber_example"; // String | Optional filter of results by serial number
String deviceInfoDeviceClass = "deviceInfoDeviceClass_example"; // String | Optional filter of results by device class
String deviceInfoFwVersion = "deviceInfoFwVersion_example"; // String | Optional filter of results by firmware version
String deviceInfoHwVersion = "deviceInfoHwVersion_example"; // String | Optional filter of results by hardware version
String deviceInfoManufacturer = "deviceInfoManufacturer_example"; // String | Optional filter of results by manufacturer name
String deviceInfoModel = "deviceInfoModel_example"; // String | Optional filter of results by device model
try {
    DeviceListResponse result = apiInstance.bulkDevicesGet(bookmark, limit, sort, facets, typeId, deviceId, gatewayTypeId, gatewayId, statusAlertEnabled, registrationDate, mgmtDormant, mgmtSupportsDeviceActions, mgmtSupportsFirmwareActions, deviceInfoDescription, deviceInfoDescriptiveLocation, deviceInfoSerialNumber, deviceInfoDeviceClass, deviceInfoFwVersion, deviceInfoHwVersion, deviceInfoManufacturer, deviceInfoModel);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceBulkConfigurationApi#bulkDevicesGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **bookmark** | **String**| Used for paging through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined. | [optional]
 **limit** | **BigDecimal**| Maximum number of results returned per page. | [optional] [default to 25]
 **sort** | [**List&lt;String&gt;**](String.md)| Define the sort order of results.  Specify a comma-separated list of properties, enclosed within square brackets. Prefix field names with &#39;-&#39; to sort in descending order.  For example: property1,property2 | [optional]
 **facets** | [**List&lt;String&gt;**](String.md)| Define the facets to return.  For example: property1,property2 | [optional]
 **typeId** | **String**| Optional filter of results by type ID | [optional]
 **deviceId** | **String**| Optional filter of results by device ID | [optional]
 **gatewayTypeId** | **String**| Optional filter of results by gateway type ID | [optional]
 **gatewayId** | **String**| Optional filter of results by gateway ID | [optional]
 **statusAlertEnabled** | **Boolean**| Optional filter of results by alert state | [optional]
 **registrationDate** | [**Object**](.md)| Optional filter of results by registration date | [optional]
 **mgmtDormant** | **Boolean**| Optional filter of results by dormant state | [optional]
 **mgmtSupportsDeviceActions** | **Boolean**| Optional filter of results by support for device actions | [optional]
 **mgmtSupportsFirmwareActions** | **Boolean**| Optional filter of results by support for firmware actions | [optional]
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

<a name="bulkDevicesRemovePost"></a>
# **bulkDevicesRemovePost**
> DeviceBulkDeletionResponseList bulkDevicesRemovePost(devices)

Delete multiple devices

Delete multiple devices, each request can contain a maximum of 512 kB.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceBulkConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceBulkConfigurationApi apiInstance = new DeviceBulkConfigurationApi();
DeviceBulkDeletionRequestList devices = new DeviceBulkDeletionRequestList(); // DeviceBulkDeletionRequestList | Devices to be deleted
try {
    DeviceBulkDeletionResponseList result = apiInstance.bulkDevicesRemovePost(devices);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceBulkConfigurationApi#bulkDevicesRemovePost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devices** | [**DeviceBulkDeletionRequestList**](DeviceBulkDeletionRequestList.md)| Devices to be deleted |

### Return type

[**DeviceBulkDeletionResponseList**](DeviceBulkDeletionResponseList.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


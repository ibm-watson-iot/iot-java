# DeviceTypeConfigurationApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deviceTypesGet**](DeviceTypeConfigurationApi.md#deviceTypesGet) | **GET** /device/types | List device types
[**deviceTypesPost**](DeviceTypeConfigurationApi.md#deviceTypesPost) | **POST** /device/types | Create device type
[**deviceTypesTypeIdDelete**](DeviceTypeConfigurationApi.md#deviceTypesTypeIdDelete) | **DELETE** /device/types/{typeId} | Delete device type
[**deviceTypesTypeIdGet**](DeviceTypeConfigurationApi.md#deviceTypesTypeIdGet) | **GET** /device/types/{typeId} | Get device type
[**deviceTypesTypeIdPut**](DeviceTypeConfigurationApi.md#deviceTypesTypeIdPut) | **PUT** /device/types/{typeId} | Update device type


<a name="deviceTypesGet"></a>
# **deviceTypesGet**
> DeviceTypeListResponse deviceTypesGet(bookmark, limit, sort, facets, id, description, deviceInfoDescription, deviceInfoDescriptiveLocation, deviceInfoSerialNumber, deviceInfoDeviceClass, deviceInfoFwVersion, deviceInfoHwVersion, deviceInfoManufacturer, deviceInfoModel)

List device types

Sorting can be performed on any of the following properties (sort order can be reversed by prefixing the property name with &#39;-&#39;): - id - description - deviceInfo.description - deviceInfo.descriptiveLocation - deviceInfo.serialNumber - deviceInfo.deviceClass - deviceInfo.fwVersion - deviceInfo.hwVersion - deviceInfo.manufacturer - deviceInfo.model - edgeConfiguration.enabled - edgeConfiguration.architecture - edgeConfiguration.edgeServices.id - edgeConfiguration.edgeServices.deploymentOverrides  The following facets are supported: - deviceInfo.deviceClass - deviceInfo.fwVersion - deviceInfo.hwVersion - deviceInfo.manufacturer - deviceInfo.model

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceTypeConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceTypeConfigurationApi apiInstance = new DeviceTypeConfigurationApi();
String bookmark = "bookmark_example"; // String | Used for paging through results. Issue the first request without specifying a bookmark, then take the bookmark returned in the response and provide it on the request for the next page. Repeat until the end of the result set indicated by the absence of a bookmark. Each request must use exactly the same values for the other parameters, or the results are undefined.
BigDecimal limit = new BigDecimal(); // BigDecimal | Maximum number of results returned per page.
List<String> sort = Arrays.asList("sort_example"); // List<String> | Define the sort order of results.  Specify a comma-separated list of properties, enclosed within square brackets. Prefix field names with '-' to sort in descending order.  For example: property1,property2
List<String> facets = Arrays.asList("facets_example"); // List<String> | Define the facets to return.  For example: property1,property2
String id = "id_example"; // String | Optional filter of results by ID
String description = "description_example"; // String | Optional filter of results by description
String deviceInfoDescription = "deviceInfoDescription_example"; // String | Optional filter of results by device description
String deviceInfoDescriptiveLocation = "deviceInfoDescriptiveLocation_example"; // String | Optional filter of results by device location
String deviceInfoSerialNumber = "deviceInfoSerialNumber_example"; // String | Optional filter of results by serial number
String deviceInfoDeviceClass = "deviceInfoDeviceClass_example"; // String | Optional filter of results by device class
String deviceInfoFwVersion = "deviceInfoFwVersion_example"; // String | Optional filter of results by firmware version
String deviceInfoHwVersion = "deviceInfoHwVersion_example"; // String | Optional filter of results by hardware version
String deviceInfoManufacturer = "deviceInfoManufacturer_example"; // String | Optional filter of results by manufacturer name
String deviceInfoModel = "deviceInfoModel_example"; // String | Optional filter of results by device model
try {
    DeviceTypeListResponse result = apiInstance.deviceTypesGet(bookmark, limit, sort, facets, id, description, deviceInfoDescription, deviceInfoDescriptiveLocation, deviceInfoSerialNumber, deviceInfoDeviceClass, deviceInfoFwVersion, deviceInfoHwVersion, deviceInfoManufacturer, deviceInfoModel);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceTypeConfigurationApi#deviceTypesGet");
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
 **id** | **String**| Optional filter of results by ID | [optional]
 **description** | **String**| Optional filter of results by description | [optional]
 **deviceInfoDescription** | **String**| Optional filter of results by device description | [optional]
 **deviceInfoDescriptiveLocation** | **String**| Optional filter of results by device location | [optional]
 **deviceInfoSerialNumber** | **String**| Optional filter of results by serial number | [optional]
 **deviceInfoDeviceClass** | **String**| Optional filter of results by device class | [optional]
 **deviceInfoFwVersion** | **String**| Optional filter of results by firmware version | [optional]
 **deviceInfoHwVersion** | **String**| Optional filter of results by hardware version | [optional]
 **deviceInfoManufacturer** | **String**| Optional filter of results by manufacturer name | [optional]
 **deviceInfoModel** | **String**| Optional filter of results by device model | [optional]

### Return type

[**DeviceTypeListResponse**](DeviceTypeListResponse.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesPost"></a>
# **deviceTypesPost**
> DeviceType deviceTypesPost(deviceType)

Create device type

Creates a device type for a normal device or a gateway. Gateways are specialization of devices but have the additional permission to register new devices and act on behalf of a device that is assigned to the gateway.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceTypeConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceTypeConfigurationApi apiInstance = new DeviceTypeConfigurationApi();
DeviceTypeCreationRequest deviceType = new DeviceTypeCreationRequest(); // DeviceTypeCreationRequest | Device type to be created
try {
    DeviceType result = apiInstance.deviceTypesPost(deviceType);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceTypeConfigurationApi#deviceTypesPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deviceType** | [**DeviceTypeCreationRequest**](DeviceTypeCreationRequest.md)| Device type to be created |

### Return type

[**DeviceType**](DeviceType.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDelete"></a>
# **deviceTypesTypeIdDelete**
> deviceTypesTypeIdDelete(typeId)

Delete device type

Deletes a device type.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceTypeConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceTypeConfigurationApi apiInstance = new DeviceTypeConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
try {
    apiInstance.deviceTypesTypeIdDelete(typeId);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceTypeConfigurationApi#deviceTypesTypeIdDelete");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |

### Return type

null (empty response body)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdGet"></a>
# **deviceTypesTypeIdGet**
> DeviceType deviceTypesTypeIdGet(typeId)

Get device type

Gets device type details.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceTypeConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceTypeConfigurationApi apiInstance = new DeviceTypeConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
try {
    DeviceType result = apiInstance.deviceTypesTypeIdGet(typeId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceTypeConfigurationApi#deviceTypesTypeIdGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |

### Return type

[**DeviceType**](DeviceType.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdPut"></a>
# **deviceTypesTypeIdPut**
> DeviceType deviceTypesTypeIdPut(typeId, deviceTypeUpdate)

Update device type

Updates a device type.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceTypeConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceTypeConfigurationApi apiInstance = new DeviceTypeConfigurationApi();
String typeId = "typeId_example"; // String | Device type ID
DeviceTypeUpdateRequest deviceTypeUpdate = new DeviceTypeUpdateRequest(); // DeviceTypeUpdateRequest | Device type update
try {
    DeviceType result = apiInstance.deviceTypesTypeIdPut(typeId, deviceTypeUpdate);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceTypeConfigurationApi#deviceTypesTypeIdPut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceTypeUpdate** | [**DeviceTypeUpdateRequest**](DeviceTypeUpdateRequest.md)| Device type update |

### Return type

[**DeviceType**](DeviceType.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


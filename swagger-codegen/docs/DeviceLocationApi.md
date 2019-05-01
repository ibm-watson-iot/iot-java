# DeviceLocationApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deviceTypesTypeIdDevicesDeviceIdLocationGet**](DeviceLocationApi.md#deviceTypesTypeIdDevicesDeviceIdLocationGet) | **GET** /device/types/{typeId}/devices/{deviceId}/location | Get device location information
[**deviceTypesTypeIdDevicesDeviceIdLocationPut**](DeviceLocationApi.md#deviceTypesTypeIdDevicesDeviceIdLocationPut) | **PUT** /device/types/{typeId}/devices/{deviceId}/location | Update device location information


<a name="deviceTypesTypeIdDevicesDeviceIdLocationGet"></a>
# **deviceTypesTypeIdDevicesDeviceIdLocationGet**
> DeviceLocation deviceTypesTypeIdDevicesDeviceIdLocationGet(typeId, deviceId)

Get device location information

Gets location information for a device.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceLocationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceLocationApi apiInstance = new DeviceLocationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
try {
    DeviceLocation result = apiInstance.deviceTypesTypeIdDevicesDeviceIdLocationGet(typeId, deviceId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceLocationApi#deviceTypesTypeIdDevicesDeviceIdLocationGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |

### Return type

[**DeviceLocation**](DeviceLocation.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesTypeIdDevicesDeviceIdLocationPut"></a>
# **deviceTypesTypeIdDevicesDeviceIdLocationPut**
> DeviceLocation deviceTypesTypeIdDevicesDeviceIdLocationPut(typeId, deviceId, location)

Update device location information

Updates the location information for a device. If no date is supplied, the entry is added with the current date and time.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceLocationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceLocationApi apiInstance = new DeviceLocationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
UpdateableDeviceLocation location = new UpdateableDeviceLocation(); // UpdateableDeviceLocation | Device location information
try {
    DeviceLocation result = apiInstance.deviceTypesTypeIdDevicesDeviceIdLocationPut(typeId, deviceId, location);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceLocationApi#deviceTypesTypeIdDevicesDeviceIdLocationPut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |
 **location** | [**UpdateableDeviceLocation**](UpdateableDeviceLocation.md)| Device location information |

### Return type

[**DeviceLocation**](DeviceLocation.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


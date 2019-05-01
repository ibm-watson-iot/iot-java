# DeviceLocationWeatherApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet**](DeviceLocationWeatherApi.md#deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet) | **GET** /device/types/{typeId}/devices/{deviceId}/exts/twc/ops/geocode | Retrieve current meteorological observations for the location associated with your device


<a name="deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet"></a>
# **deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet**
> Object deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet(typeId, deviceId)

Retrieve current meteorological observations for the location associated with your device

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceLocationWeatherApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceLocationWeatherApi apiInstance = new DeviceLocationWeatherApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
try {
    Object result = apiInstance.deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet(typeId, deviceId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceLocationWeatherApi#deviceTypesTypeIdDevicesDeviceIdExtsTwcOpsGeocodeGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |

### Return type

**Object**

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


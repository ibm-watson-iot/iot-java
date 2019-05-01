# LastEventCacheApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**configLecGet**](LastEventCacheApi.md#configLecGet) | **GET** /config/lec | Retrieve the Last Event Cache configuration for your organization
[**configLecPut**](LastEventCacheApi.md#configLecPut) | **PUT** /config/lec | Update the Last Event Cache configuration for your organization
[**deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet**](LastEventCacheApi.md#deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet) | **GET** /device/types/{deviceType}/devices/{deviceId}/events/{eventName} | Get last event for a specific event id for a specific device
[**deviceTypesDeviceTypeDevicesDeviceIdEventsGet**](LastEventCacheApi.md#deviceTypesDeviceTypeDevicesDeviceIdEventsGet) | **GET** /device/types/{deviceType}/devices/{deviceId}/events | Get all last events for a specific device


<a name="configLecGet"></a>
# **configLecGet**
> LECConfig configLecGet()

Retrieve the Last Event Cache configuration for your organization

Retrieves the current configuration for the Last Event Cache (LEC) feature for your organization. You can use this endpoint to determine whether or not the LEC feature is currently enabled, along with how many days cached device events will persist for before being removed from the cache.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.LastEventCacheApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

LastEventCacheApi apiInstance = new LastEventCacheApi();
try {
    LECConfig result = apiInstance.configLecGet();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LastEventCacheApi#configLecGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**LECConfig**](LECConfig.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="configLecPut"></a>
# **configLecPut**
> LECConfig configLecPut(config)

Update the Last Event Cache configuration for your organization

Updates the current configuration for the Last Event Cache (LEC) feature for your organization. You can use this endpoint to control whether or not the LEC feature is currently enabled, along with how many days cached device events will persist for before being removed from the cache.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.LastEventCacheApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

LastEventCacheApi apiInstance = new LastEventCacheApi();
LECConfig config = new LECConfig(); // LECConfig | LEC configuration to apply
try {
    LECConfig result = apiInstance.configLecPut(config);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LastEventCacheApi#configLecPut");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **config** | [**LECConfig**](LECConfig.md)| LEC configuration to apply |

### Return type

[**LECConfig**](LECConfig.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet"></a>
# **deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet**
> Event deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet(deviceType, deviceId, eventName)

Get last event for a specific event id for a specific device

Get last event for a specific event id for a specific device

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.LastEventCacheApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

LastEventCacheApi apiInstance = new LastEventCacheApi();
String deviceType = "deviceType_example"; // String | Device type
String deviceId = "deviceId_example"; // String | Device id
String eventName = "eventName_example"; // String | Event name
try {
    Event result = apiInstance.deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet(deviceType, deviceId, eventName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LastEventCacheApi#deviceTypesDeviceTypeDevicesDeviceIdEventsEventNameGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deviceType** | **String**| Device type |
 **deviceId** | **String**| Device id |
 **eventName** | **String**| Event name |

### Return type

[**Event**](Event.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="deviceTypesDeviceTypeDevicesDeviceIdEventsGet"></a>
# **deviceTypesDeviceTypeDevicesDeviceIdEventsGet**
> Events deviceTypesDeviceTypeDevicesDeviceIdEventsGet(deviceType, deviceId)

Get all last events for a specific device

Get all last events for a specific device

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.LastEventCacheApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

LastEventCacheApi apiInstance = new LastEventCacheApi();
String deviceType = "deviceType_example"; // String | Device type
String deviceId = "deviceId_example"; // String | Device id
try {
    Events result = apiInstance.deviceTypesDeviceTypeDevicesDeviceIdEventsGet(deviceType, deviceId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling LastEventCacheApi#deviceTypesDeviceTypeDevicesDeviceIdEventsGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deviceType** | **String**| Device type |
 **deviceId** | **String**| Device id |

### Return type

[**Events**](Events.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


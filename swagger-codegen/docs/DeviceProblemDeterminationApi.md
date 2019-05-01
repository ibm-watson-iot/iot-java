# DeviceProblemDeterminationApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**logsConnectionGet**](DeviceProblemDeterminationApi.md#logsConnectionGet) | **GET** /logs/connection | List device connection log events


<a name="logsConnectionGet"></a>
# **logsConnectionGet**
> ListOfLogEntries logsConnectionGet(typeId, deviceId, limit, offset, fromTime, toTime)

List device connection log events

List connection log events for a device to aid in diagnosing connectivity problems. The entries record successful connection, unsuccessful connection attempts, intentional disconnection and server-initiated disconnection.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.DeviceProblemDeterminationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

DeviceProblemDeterminationApi apiInstance = new DeviceProblemDeterminationApi();
String typeId = "typeId_example"; // String | Device type ID
String deviceId = "deviceId_example"; // String | Device ID
BigDecimal limit = new BigDecimal(); // BigDecimal | The maximum number of events to retrieve. The range is 1-20.
BigDecimal offset = new BigDecimal(); // BigDecimal | The number of events to skip before returning the list. The range is 1-1000.
String fromTime = "fromTime_example"; // String | The starting date/time of the event range, in ISO 8601 format. fromTime and toTime must me used together.
String toTime = "toTime_example"; // String | The ending date/time of the event range, in ISO 8601 format. fromTime and toTime must me used together.
try {
    ListOfLogEntries result = apiInstance.logsConnectionGet(typeId, deviceId, limit, offset, fromTime, toTime);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceProblemDeterminationApi#logsConnectionGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **typeId** | **String**| Device type ID |
 **deviceId** | **String**| Device ID |
 **limit** | **BigDecimal**| The maximum number of events to retrieve. The range is 1-20. | [optional]
 **offset** | **BigDecimal**| The number of events to skip before returning the list. The range is 1-1000. | [optional]
 **fromTime** | **String**| The starting date/time of the event range, in ISO 8601 format. fromTime and toTime must me used together. | [optional]
 **toTime** | **String**| The ending date/time of the event range, in ISO 8601 format. fromTime and toTime must me used together. | [optional]

### Return type

[**ListOfLogEntries**](ListOfLogEntries.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


# UsageManagementApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**usageDataTrafficGet**](UsageManagementApi.md#usageDataTrafficGet) | **GET** /usage/data-traffic | Retrieve the amount of data used


<a name="usageDataTrafficGet"></a>
# **usageDataTrafficGet**
> DataTraffic usageDataTrafficGet(start, end, detail)

Retrieve the amount of data used

Retrieve the amount of data used

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.UsageManagementApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

UsageManagementApi apiInstance = new UsageManagementApi();
String start = "start_example"; // String | Start date in one of the following formats: YYYY (first day of the year), YYYY-MM (first day of the month), YYYY-MM-DD (specific day)
String end = "end_example"; // String | End date in one of the following formats: YYYY (last day of the year), YYYY-MM (last day of the month), YYYY-MM-DD (specific day)
Boolean detail = true; // Boolean | Indicates whether a daily breakdown will be included in the result set
try {
    DataTraffic result = apiInstance.usageDataTrafficGet(start, end, detail);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsageManagementApi#usageDataTrafficGet");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **start** | **String**| Start date in one of the following formats: YYYY (first day of the year), YYYY-MM (first day of the month), YYYY-MM-DD (specific day) |
 **end** | **String**| End date in one of the following formats: YYYY (last day of the year), YYYY-MM (last day of the month), YYYY-MM-DD (specific day) |
 **detail** | **Boolean**| Indicates whether a daily breakdown will be included in the result set | [optional]

### Return type

[**DataTraffic**](DataTraffic.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


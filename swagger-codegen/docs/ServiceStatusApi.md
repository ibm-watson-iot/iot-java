# ServiceStatusApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**serviceStatusGet**](ServiceStatusApi.md#serviceStatusGet) | **GET** /service-status | Retrieve the status of services for an organization


<a name="serviceStatusGet"></a>
# **serviceStatusGet**
> String serviceStatusGet()

Retrieve the status of services for an organization

Retrieve the organization-specific status of each of the services offered by Watson IoT Platform.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.ServiceStatusApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

ServiceStatusApi apiInstance = new ServiceStatusApi();
try {
    String result = apiInstance.serviceStatusGet();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServiceStatusApi#serviceStatusGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**String**

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


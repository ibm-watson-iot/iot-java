# OrganizationConfigurationApi

All URIs are relative to *https://localhost/api/v0002*

Method | HTTP request | Description
------------- | ------------- | -------------
[**rootGet**](OrganizationConfigurationApi.md#rootGet) | **GET** / | Get organization details


<a name="rootGet"></a>
# **rootGet**
> Organization rootGet()

Get organization details

Get details about an organization.

### Example
```java
// Import classes:
//import com.ibm.wiotp.swagger.ApiClient;
//import com.ibm.wiotp.swagger.ApiException;
//import com.ibm.wiotp.swagger.Configuration;
//import com.ibm.wiotp.swagger.auth.*;
//import com.ibm.wiotp.swagger.api.OrganizationConfigurationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure HTTP basic authorization: ApiKey
HttpBasicAuth ApiKey = (HttpBasicAuth) defaultClient.getAuthentication("ApiKey");
ApiKey.setUsername("YOUR USERNAME");
ApiKey.setPassword("YOUR PASSWORD");

OrganizationConfigurationApi apiInstance = new OrganizationConfigurationApi();
try {
    Organization result = apiInstance.rootGet();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling OrganizationConfigurationApi#rootGet");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Organization**](Organization.md)

### Authorization

[ApiKey](../README.md#ApiKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


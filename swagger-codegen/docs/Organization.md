
# Organization

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **String** | Organization ID | 
**name** | **String** | User assigned name for the organization | 
**enabled** | **Boolean** | Whether the organization is enabled.  If the organization is disabled all API calls will be rejected and messaging will be disabled across all devices and applications | 
**type** | [**TypeEnum**](#TypeEnum) |  | 
**bluemix** | [**OrganizationBluemixConfig**](OrganizationBluemixConfig.md) |  |  [optional]
**ibmMarketplace** | [**OrganizationIbmMarketplaceConfig**](OrganizationIbmMarketplaceConfig.md) |  |  [optional]
**config** | [**OrganizationConfig**](OrganizationConfig.md) |  | 
**created** | [**OffsetDateTime**](OffsetDateTime.md) | ISO8601 date-time that the organization was created | 
**updated** | [**OffsetDateTime**](OffsetDateTime.md) | ISO8601 date-time that the organization was last updated | 


<a name="TypeEnum"></a>
## Enum: TypeEnum
Name | Value
---- | -----
BLUEMIX_FREE | &quot;Bluemix Free&quot;
BLUEMIX_BRONZE | &quot;Bluemix Bronze&quot;
BLUEMIX_SILVER | &quot;Bluemix Silver&quot;
BLUEMIX_GOLD | &quot;Bluemix Gold&quot;
PENDING | &quot;Pending&quot;
TRIAL | &quot;Trial&quot;
BRONZE | &quot;Bronze&quot;
SILVER | &quot;Silver&quot;
GOLD | &quot;Gold&quot;
SUBSCRIPTION | &quot;Subscription&quot;




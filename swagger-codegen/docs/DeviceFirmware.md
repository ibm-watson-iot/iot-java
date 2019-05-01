
# DeviceFirmware

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**version** | **String** | The version of the firmware |  [optional]
**name** | **String** | The name of the firmware to be used on the device |  [optional]
**url** | **String** | The URL from which the firmware image can be downloaded |  [optional]
**verifier** | **String** | The verifier such as a checksum for the firmware image to validate its integrity |  [optional]
**state** | [**BigDecimal**](BigDecimal.md) | Indicates the state of the firmware image |  [optional]
**updateStatus** | [**BigDecimal**](BigDecimal.md) | Indicates the status of the update |  [optional]
**updatedDateTime** | [**OffsetDateTime**](OffsetDateTime.md) | Date and time of initiating the last update (ISO8601) |  [optional]




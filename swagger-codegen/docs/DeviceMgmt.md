
# DeviceMgmt

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**lastActivityDateTime** | [**OffsetDateTime**](OffsetDateTime.md) | Date and time of last device management server activity, updated periodically (ISO8601) | 
**dormantDateTime** | [**OffsetDateTime**](OffsetDateTime.md) | Date and time at which the managed device will automatically be set dormant (ISO8601) |  [optional]
**dormant** | **Boolean** | Whether the device has become dormant | 
**supports** | [**DeviceMgmtSupports**](DeviceMgmtSupports.md) |  | 
**firmware** | [**DeviceFirmware**](DeviceFirmware.md) |  |  [optional]




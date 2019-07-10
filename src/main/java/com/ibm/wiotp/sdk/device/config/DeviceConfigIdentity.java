package com.ibm.wiotp.sdk.device.config;

import java.util.Map;

public class DeviceConfigIdentity {
	public String orgId;
	public String typeId;
	public String deviceId;
	
	public DeviceConfigIdentity() {}
	
	public DeviceConfigIdentity(String orgId, String typeId, String deviceId) {
		this.orgId = orgId;
		this.typeId = typeId;
		this.deviceId = deviceId;
	}

	
	public static DeviceConfigIdentity generateFromEnv() {
		DeviceConfigIdentity identity = new DeviceConfigIdentity();
		identity.orgId = System.getenv("WIOTP_IDENTITY_ORGID");
		identity.typeId = System.getenv("WIOTP_IDENTITY_TYPEID");
		identity.deviceId = System.getenv("WIOTP_IDENTITY_DEVICEID");
				
		return identity;
	}

	public static DeviceConfigIdentity generateFromConfig(Map<String, Object> yamlIdentity) {
		DeviceConfigIdentity identity = new DeviceConfigIdentity();
		identity.orgId = (String) yamlIdentity.get("orgId");
		identity.typeId = (String) yamlIdentity.get("typeId");
		identity.deviceId = (String) yamlIdentity.get("deviceId");
		
		return identity;
	}
}

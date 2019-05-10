package com.ibm.wiotp.sdk.gateway.config;

import com.ibm.wiotp.sdk.device.config.DeviceConfig;
import com.ibm.wiotp.sdk.device.config.DeviceConfigAuth;
import com.ibm.wiotp.sdk.device.config.DeviceConfigIdentity;
import com.ibm.wiotp.sdk.device.config.DeviceConfigOptions;

public class GatewayConfig extends DeviceConfig {
	
	public GatewayConfig(DeviceConfigIdentity identity, DeviceConfigAuth auth, DeviceConfigOptions options) {
		super(identity, auth, options);
	}

	@Override
	public String getClientId() {
		return "g:" + identity.orgId + ":" + identity.typeId + ":" + identity.deviceId;
	}
	
	
	public static GatewayConfig generateFromEnv() {
		GatewayConfig cfg = new GatewayConfig(
				DeviceConfigIdentity.generateFromEnv(), 
				DeviceConfigAuth.generateFromEnv(), 
				DeviceConfigOptions.generateFromEnv());
		
		return cfg;
	}

}

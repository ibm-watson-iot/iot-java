package com.ibm.wiotp.sdk.device.config;

public class DeviceConfigAuth {
	public String token;
	
	public DeviceConfigAuth() {}
	
	public DeviceConfigAuth(String token) {
		this.token = token;
	}
	
	public static DeviceConfigAuth generateFromEnv() {
		DeviceConfigAuth auth = new DeviceConfigAuth();
		auth.token = System.getenv("WIOTP_AUTH_TOKEN");
		
		return auth;
	}
}

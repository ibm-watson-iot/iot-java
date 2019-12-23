package com.ibm.wiotp.sdk.device.config;

import java.util.Map;

public class DeviceConfigAuth {
	public String token;

	public DeviceConfigAuth() {
	}

	public DeviceConfigAuth(String token) {
		this.token = token;
	}

	public static DeviceConfigAuth generateFromEnv() {
		DeviceConfigAuth auth = new DeviceConfigAuth();
		auth.token = System.getenv("WIOTP_AUTH_TOKEN");

		return auth;
	}

	public static DeviceConfigAuth generateFromConfig(Map<String, Object> yamlAuth) {
		DeviceConfigAuth auth = new DeviceConfigAuth();
		auth.token = (String) yamlAuth.get("token");

		return auth;
	}
}
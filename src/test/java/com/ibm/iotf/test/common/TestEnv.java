package com.ibm.iotf.test.common;

import java.util.Properties;

/**
 * 
 * Provides methods to retrieve test environment variables.
 * The test environment variables are generated via 'travis encrypt'
 * and stored in .travis.yml of this project
 * 
 */
public final class TestEnv {

	public static final String getOrgId() {
		return System.getenv("IOT_JAVA_ORG_ID");
	}
	
	public static final String getAPIKey() {
		return System.getenv("IOT_JAVA_API_KEY");
	}
	
	public static final String getAPIToken() {
		return System.getenv("IOT_JAVA_API_TOKEN");
	}
	
	public static final String getDeviceToken() {
		return System.getenv("IOT_JAVA_DEVICE_TOKEN");
	}
	
	public static final String getGatewayToken() {
		return System.getenv("IOT_JAVA_GATEWAY_TOKEN");
	}
	
	public static Properties getDeviceProperties(String deviceType, String deviceId) {
		Properties props = new Properties();
		props.setProperty("Organization-ID", getOrgId());
		props.setProperty("Device-Type", deviceType);
		props.setProperty("Device-ID", deviceId);
		props.setProperty("Authentication-Method", "token");
		props.setProperty("Authentication-Token", getDeviceToken());
		props.setProperty("Clean-Session", "true");
		props.setProperty("WebSocket", "false");
		props.setProperty("Secure", "true");
		props.setProperty("MaxInflightMessages", "256");
		return props;
	}
	
	public static Properties getAppProperties(String appId, boolean sharedSubscription, String deviceType, String deviceId) {
		Properties props = new Properties();
		props.setProperty("id", appId);
		props.setProperty("Organization-ID", getOrgId());
		if (deviceType != null)
			props.setProperty("Device-Type", deviceType);
		if (deviceId != null)
			props.setProperty("Device-ID", deviceId);
		props.setProperty("Authentication-Method", "apikey");
		props.setProperty("API-Key", getAPIKey());
		props.setProperty("Authentication-Token", getAPIToken());
		props.setProperty("Shared-Subscription", Boolean.toString(sharedSubscription));
		props.setProperty("Clean-Session", "true");
		props.setProperty("WebSocket", "false");
		props.setProperty("Secure", "true");
		props.setProperty("MaxInflightMessages", "256");

		return props;
	}
}

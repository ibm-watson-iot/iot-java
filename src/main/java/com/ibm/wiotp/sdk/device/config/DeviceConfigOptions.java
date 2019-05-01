package com.ibm.wiotp.sdk.device.config;

public class DeviceConfigOptions {
	public String domain;
	public String logLevel;
	public DeviceConfigOptionsMqtt mqtt;
	
	public DeviceConfigOptions() {}
	
	public DeviceConfigOptions(String domain, String logLevel, DeviceConfigOptionsMqtt mqtt) {
		this.domain = domain;
		this.logLevel = logLevel;
		this.mqtt = mqtt;
	}

	
	public static DeviceConfigOptions generateFromEnv() {
		DeviceConfigOptions options = new DeviceConfigOptions();
		options.domain = System.getenv("WIOTP_OPTIONS_DOMAIN");
		options.logLevel = System.getenv("WIOTP_OPTIONS_LOGLEVEL");
		
		options.mqtt = DeviceConfigOptionsMqtt.generateFromEnv();
		
		return options;
	}
}

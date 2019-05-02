package com.ibm.wiotp.sdk.device.config;

import java.util.logging.Level;

public class DeviceConfigOptions {
	public String domain;
	public Level logLevel;
	public DeviceConfigOptionsMqtt mqtt;
	
	public DeviceConfigOptions() {
		this.domain = "internetofthings.ibmcloud.com";
		this.logLevel = Level.INFO;
		this.mqtt = new DeviceConfigOptionsMqtt();
	}
	
	public DeviceConfigOptions(String domain, String logLevel, DeviceConfigOptionsMqtt mqtt) {
		this.domain = domain;
		this.logLevel = Level.parse(logLevel);
		this.mqtt = mqtt;
	}

	
	public static DeviceConfigOptions generateFromEnv() {
		DeviceConfigOptions options = new DeviceConfigOptions();
		
		if (System.getenv("WIOTP_OPTIONS_DOMAIN") != null)
			options.domain = System.getenv("WIOTP_OPTIONS_DOMAIN");
		
		if (System.getenv("WIOTP_OPTIONS_LOGLEVEL") != null) {
			final String logLevelName = System.getenv("WIOTP_OPTIONS_LOGLEVEL");
			options.logLevel = Level.parse(logLevelName);
		}
		
		options.mqtt = DeviceConfigOptionsMqtt.generateFromEnv();
		
		return options;
	}
}

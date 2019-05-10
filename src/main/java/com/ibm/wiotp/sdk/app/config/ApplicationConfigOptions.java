package com.ibm.wiotp.sdk.app.config;

import java.util.logging.Level;

public class ApplicationConfigOptions {
	public String domain;
	public Level logLevel;
	public ApplicationConfigOptionsMqtt mqtt;
	public ApplicationConfigOptionsHttp http;
	
	public ApplicationConfigOptions() {
		domain = "internetofthings.ibmcloud.com";
		logLevel = Level.INFO;
		this.mqtt = new ApplicationConfigOptionsMqtt();
		this.http = new ApplicationConfigOptionsHttp();
	}
	
	public ApplicationConfigOptions(String domain, Level logLevel, ApplicationConfigOptionsMqtt mqtt) {
		this.domain = domain;
		this.logLevel = logLevel;
		this.mqtt = mqtt;
	}

	
	public static ApplicationConfigOptions generateFromEnv() {
		ApplicationConfigOptions options = new ApplicationConfigOptions();
		
		if (System.getenv("WIOTP_OPTIONS_DOMAIN") != null)
			options.domain = System.getenv("WIOTP_OPTIONS_DOMAIN");
		
		if (System.getenv("WIOTP_OPTIONS_LOGLEVEL") != null) {
			final String logLevelName = System.getenv("WIOTP_OPTIONS_LOGLEVEL");
			options.logLevel = Level.parse(logLevelName);
		}
		
		options.mqtt = ApplicationConfigOptionsMqtt.generateFromEnv();
		
		return options;
	}
}

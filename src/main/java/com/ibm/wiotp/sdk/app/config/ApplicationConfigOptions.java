package com.ibm.wiotp.sdk.app.config;

import java.util.Map;
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

	@SuppressWarnings("unchecked")
	public static ApplicationConfigOptions generateFromConfig(Map<String, Object> yamlOptions) {
		ApplicationConfigOptions options = new ApplicationConfigOptions();
		
		if (yamlOptions.get("domain") != null)
			options.domain = (String) yamlOptions.get("domain");
		
		if (yamlOptions.get("logLevel") != null) {
			final String logLevelName = (String) yamlOptions.get("logLevel");
			options.logLevel = Level.parse(logLevelName);
		}
		
		if(yamlOptions.get("mqtt") instanceof Map<?, ?>) {
			options.mqtt = ApplicationConfigOptionsMqtt.generateFromConfig((Map<String, Object>) yamlOptions.get("mqtt"));
		}
		//else mqtt is missing or in the wrong format	
		
		return options;	}
}

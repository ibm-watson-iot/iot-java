package com.ibm.wiotp.sdk.gateway.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import com.ibm.wiotp.sdk.device.config.DeviceConfig;
import com.ibm.wiotp.sdk.device.config.DeviceConfigAuth;
import com.ibm.wiotp.sdk.device.config.DeviceConfigIdentity;
import com.ibm.wiotp.sdk.device.config.DeviceConfigOptions;

import org.yaml.snakeyaml.Yaml;

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

	@SuppressWarnings("unchecked")
	public static GatewayConfig generateFromConfig(String fileName) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream(fileName);
		Map<String, Object> yamlContents = yaml.load(inputStream);	

		if(yamlContents.get("identity") instanceof Map<?, ?>) {
			if(yamlContents.get("auth") instanceof Map<?, ?>) {
				if(yamlContents.get("options") instanceof Map<?, ?>) {
					GatewayConfig cfg = new GatewayConfig(
					DeviceConfigIdentity.generateFromConfig((Map<String, Object>) yamlContents.get("identity")), 
					DeviceConfigAuth.generateFromConfig((Map<String, Object>) yamlContents.get("auth")), 
					DeviceConfigOptions.generateFromConfig((Map<String, Object>) yamlContents.get("options")));
					return cfg;
				}
				//else options is missing or in the wrong format			
			}		
			//else auth is missing or in the wrong format			
		}
		//else identity is missing or in the wrong format			
		return null;
	}

}

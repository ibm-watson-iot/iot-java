package com.ibm.wiotp.sdk.app.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import com.ibm.wiotp.sdk.AbstractConfig;

public class ApplicationConfig implements AbstractConfig {

//    identity:
//        appId: myApp
//      auth:
//        key: a-23gh56-sdsdajhjnee
//        token: Ab$76s)asj8_s5
//      options:
//        domain: internetofthings.ibmcloud.com
//        logLevel: error|warning|info|debug
//        mqtt:
//          port: 8883
//          transport: tcp
//          cleanStart: false
//          sessionExpiry: 3600
//          keepAlive: 60
//          sharedSubscription: false
//          caFile: /path/to/certificateAuthorityFile.pem
//        http:
//          verify: true 
	
	public ApplicationConfigIdentity identity;
    public ApplicationConfigAuth auth;
    public ApplicationConfigOptions options;

    public ApplicationConfig(ApplicationConfigIdentity identity, ApplicationConfigAuth auth, ApplicationConfigOptions options) {
    	this.identity = identity;
    	this.auth = auth;
    	this.options = options;
    }
    
	public static ApplicationConfig generateFromEnv() {
		ApplicationConfig cfg = new ApplicationConfig(
				ApplicationConfigIdentity.generateFromEnv(), 
				ApplicationConfigAuth.generateFromEnv(), 
				ApplicationConfigOptions.generateFromEnv());
		
		return cfg;
	}
	
	public MqttConnectOptions getMqttConnectOptions() throws NoSuchAlgorithmException, KeyManagementException {
		MqttConnectOptions connectOptions = new MqttConnectOptions();
		
		connectOptions.setConnectionTimeout(60);
		
		connectOptions.setUserName(getMqttUsername());
		connectOptions.setPassword(getMqttPassword().toCharArray());
		
		connectOptions.setCleanSession(!this.options.mqtt.cleanStart);
		connectOptions.setKeepAliveInterval(this.options.mqtt.keepAlive);
		connectOptions.setMaxInflight(DEFAULT_MAX_INFLIGHT_MESSAGES);
		connectOptions.setAutomaticReconnect(true);
		
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, null, null);
		
		connectOptions.setSocketFactory(sslContext.getSocketFactory());
		
		return connectOptions; 
	}

	@Override
	public String getOrgId() {
		if (auth.key != null && ! auth.key.trim().equals("")) {
			if (auth.key.length() >=8) {
				return auth.key.substring(2, 8);
			}
			else {
				throw new RuntimeException("Invalid format Watson IoT Platform API Key provided: " + auth.key);
			}
		} else {
			return "quickstart";
		}
	}

	@Override
	public String getClientId() {
		if (options.mqtt.sharedSubscription) {
			return "A:" + getOrgId() + ":" + identity.appId;
		} else {
			return "a:" + getOrgId() + ":" + identity.appId;
		}
	}

	@Override
	public String getMqttServerURI() {
		String protocol = "ssl://";
		if (options.mqtt.transport == "websockets") {
			protocol = "wss://";
		}
		return protocol + getOrgId() + ".messaging." + options.domain + ":" + String.valueOf(options.mqtt.port);
	}
	
	@Override
	public boolean isCleanSession() {
		return !options.mqtt.cleanStart;
	}
	
	@Override
	public boolean isCleanStart() {
		return options.mqtt.cleanStart;
	}

	@Override
	public String getMqttUsername() {
		return auth.key;
	}

	@Override
	public String getMqttPassword() {
		return auth.token;
	}
	
	@Override
	public String getDeviceId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTypeId() {
		throw new UnsupportedOperationException();
	}

}


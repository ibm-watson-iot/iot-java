package com.ibm.wiotp.sdk.device.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import com.ibm.wiotp.sdk.AbstractConfig;

public class DeviceConfig implements AbstractConfig {
	
	public DeviceConfigIdentity identity;
    public DeviceConfigAuth auth;
    public DeviceConfigOptions options;

    public DeviceConfig(DeviceConfigIdentity identity, DeviceConfigAuth auth, DeviceConfigOptions options) {
    	if (auth == null) {
    		auth = new DeviceConfigAuth();
    	}
    	if (options == null) {
    		options = new DeviceConfigOptions();
    	}
    	
    	this.identity = identity;
    	this.auth = auth;
    	this.options = options;
    }

	public static DeviceConfig generateFromEnv() {
		DeviceConfig cfg = new DeviceConfig(
				DeviceConfigIdentity.generateFromEnv(), 
				DeviceConfigAuth.generateFromEnv(), 
				DeviceConfigOptions.generateFromEnv());
		
		return cfg;
	}

	public MqttConnectOptions getMqttConnectOptions() throws NoSuchAlgorithmException, KeyManagementException {
		MqttConnectOptions connectOptions = new MqttConnectOptions();
		
		connectOptions.setConnectionTimeout(60);
		
		if (getMqttPassword() != null) {
			connectOptions.setUserName(getMqttUsername());
			connectOptions.setPassword(getMqttPassword().toCharArray());
		}
		
		connectOptions.setCleanSession(this.options.mqtt.cleanStart);
		connectOptions.setKeepAliveInterval(this.options.mqtt.keepAlive);
		connectOptions.setMaxInflight(DEFAULT_MAX_INFLIGHT_MESSAGES);
		connectOptions.setAutomaticReconnect(true);
		
		if (! Arrays.asList(1883, 80).contains(options.mqtt.port)) {
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);
			
			connectOptions.setSocketFactory(sslContext.getSocketFactory());
		}
		
		return connectOptions; 
	}

	@Override
	public String getOrgId() {
		return identity.orgId;
	}

	@Override
	public String getClientId() {
		return "d:" + identity.orgId + ":" + identity.typeId + ":" + identity.deviceId;
	}

	@Override
	public String getMqttServerURI() {
		String protocol = "ssl://";
		if (Arrays.asList(1883, 80).contains(options.mqtt.port)) {
			protocol = "tcp://";
		}

		if (options.mqtt.transport.equals("websockets")) {
			protocol = "wss://";
			if (Arrays.asList(1883, 80).contains(options.mqtt.port)) {
				protocol = "ws://";
			}
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
		return "use-token-auth";
	}

	@Override
	public String getMqttPassword() {
		return auth.token;
	}

	@Override
	public String getDeviceId() {
		return identity.deviceId;
	}

	@Override
	public String getTypeId() {
		return identity.typeId;
	}

	@Override
	public String getHttpApiBasePath() {
		return "https://" + getOrgId() + "." + options.domain + "/api/v0002";
	}

}

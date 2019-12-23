package com.ibm.wiotp.sdk;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public interface AbstractConfig {
	public static final int DEFAULT_MAX_INFLIGHT_MESSAGES = 100;
	public static final int DEFAULT_CONNECTION_TIMEMOUT = 80;

	public String getOrgId();

	public String getHttpApiBasePath();

	public String getMqttServerURI();

	public MqttConnectOptions getMqttConnectOptions() throws NoSuchAlgorithmException, KeyManagementException;

	public String getDeviceId();

	public String getTypeId();

	public String getClientId();

	public String getMqttUsername();

	public String getMqttPassword();

	public boolean isCleanSession();

	public boolean isCleanStart();

}

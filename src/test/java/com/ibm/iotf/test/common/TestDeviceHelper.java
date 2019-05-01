package com.ibm.iotf.test.common;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.test.common.TestApplicationHelper;
import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.util.LoggerUtility;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.callbacks.TestDeviceCommandCallback;
import com.google.gson.JsonObject;

public class TestDeviceHelper extends TestApplicationHelper {

	static final String CLASS_NAME = TestDeviceHelper.class.getName();
	private String devType = null;
	private String deviceId = null;
	DeviceClient devClient = null;
	TestDeviceCommandCallback callback = null;
	
	public TestDeviceHelper(String devType, String deviceId) throws Exception {
		super(null);
		this.devType = devType;
		this.deviceId = deviceId;
		Properties props = TestEnv.getDeviceProperties(this.devType, this.deviceId);
		devClient = new DeviceClient(props);
		callback = new TestDeviceCommandCallback();
		devClient.setCommandCallback(callback);
	}

	public TestDeviceHelper(String devType, String deviceId, Properties props) throws Exception {
		super(null);
		this.devType = devType;
		this.deviceId = deviceId;
		devClient = new DeviceClient(props);
		callback = new TestDeviceCommandCallback();
		devClient.setCommandCallback(callback);
	}
	
	public String getDeviceType() { return devType; }
	public String getDeviceId() { return deviceId; }
	public String getClientID() { return devClient.getClientID(); }

	public void connect() throws MqttException {
		final String METHOD = "connect";
		devClient.connect();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " connected ? " + devClient.isConnected());
	}
	
	public void disconnect() {
		final String METHOD = "disconnect";
		devClient.disconnect();
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " connected ? " + devClient.isConnected());
	}
	
	public DeviceClient getDeviceClient() {
		return devClient;
	}
	
	public boolean commandReceived() {
		return callback.commandReceived();
	}
	
	public boolean publishEvent(String eventName, JsonObject event) {
		final String METHOD = "publishEvent";
		boolean rc = devClient.publishEvent(eventName, event);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " success ? " + rc);
		return rc;
	}

	public boolean publishEvent(String eventName, JsonObject event, int qos) {
		final String METHOD = "publishEvent";
		boolean rc = devClient.publishEvent(eventName, event, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " QOS=" + qos + " success ? " + rc);
		return rc;
	}

	public boolean publishEvent(String eventName, Object event) {
		final String METHOD = "publishEvent";
		boolean rc = devClient.publishEvent(eventName, event);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " success ? " + rc);
		return rc;
	}

	public boolean publishEvent(String eventName, Object event, int qos) {
		final String METHOD = "publishEvent";
		boolean rc = devClient.publishEvent(eventName, event, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " QOS=" + qos + " success ? " + rc);
		return rc;
	}

	public boolean publishEvent(String eventName, Object event, String format, int qos) throws Exception {
		final String METHOD = "publishEvent";
		boolean rc = devClient.publishEvent(eventName, event, format, qos);
		LoggerUtility.info(CLASS_NAME, METHOD, getClientID() + " QOS=" + qos + " success ? " + rc);
		return rc;
	}

}

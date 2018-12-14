package com.ibm.iotf.test.common;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.test.common.TestHelper;
import com.ibm.iotf.util.LoggerUtility;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.test.common.TestCommandCallback;

import com.ibm.iotf.client.device.DeviceClient;

public class TestDeviceHelper extends TestHelper {

	private String devType = null;
	private String deviceId = null;
	DeviceClient devClient = null;
	TestCommandCallback callback = null;
	
	public TestDeviceHelper(String devType, String deviceId) throws Exception {
		super(null);
		this.devType = devType;
		this.deviceId = deviceId;
		Properties props = TestEnv.getDeviceProperties(this.devType, this.deviceId);
		devClient = new DeviceClient(props);
		callback = new TestCommandCallback();
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
	
	public boolean commandReceived() {
		return callback.commandReceived();
	}
	
	
}

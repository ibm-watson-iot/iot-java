package com.ibm.iotf.test.common;

import com.ibm.iotf.client.gateway.GatewayCallback;
import com.ibm.iotf.client.gateway.Notification;
import com.ibm.iotf.util.LoggerUtility;

public class TestGatewayCallback implements GatewayCallback {
	private boolean commandReceived = false;
	private boolean notificationReceived = false;
	private static final String CLASS_NAME = TestGatewayCallback.class.getName();
	
	/**
	 * This method is invoked by the library whenever there is command matching the subscription criteria
	 */
	@Override
	public void processCommand(com.ibm.iotf.client.gateway.Command cmd) {
		final String METHOD = "processCommand";
		commandReceived = true;
		LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommand() +
				", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload() + ", time = "+cmd.getTimestamp());
	}

	@Override
	public void processNotification(Notification notification) {
		final String METHOD = "processNotification";
		notificationReceived = true;
		LoggerUtility.info(CLASS_NAME, METHOD, "Received notification, Device Type (" 
				+ notification.getDeviceType() + ") Device ID ("
				+ notification.getDeviceId() + ")");
	}
	
	public void clear() {
		commandReceived = false;
		notificationReceived = false;
	}
	
	public boolean commandReceived() { return this.commandReceived; }
	public boolean notificationReceived() { return this.notificationReceived; }
	

}

package com.ibm.iotf.test.common.callbacks;

import com.ibm.wiotp.sdk.gateway.GatewayCallback;
import com.ibm.wiotp.sdk.gateway.Notification;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestGatewayCallback implements GatewayCallback {
	private boolean commandReceived = false;
	private boolean notificationReceived = false;
	private static final String CLASS_NAME = TestGatewayCallback.class.getName();
	
	/**
	 * This method is invoked by the library whenever there is command matching the subscription criteria
	 */
	@Override
	public void processCommand(com.ibm.wiotp.sdk.gateway.Command cmd) {
		final String METHOD = "processCommand";
		commandReceived = true;
		LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommand() +
				", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload());
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

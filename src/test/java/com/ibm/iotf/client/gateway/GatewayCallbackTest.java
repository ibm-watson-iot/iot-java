/**
 * 
 */
package com.ibm.iotf.client.gateway;

import java.util.Properties;
import java.util.logging.Level;

import com.ibm.iotf.client.gateway.Notification;
import com.ibm.iotf.client.gateway.Command;
import com.ibm.iotf.util.LoggerUtility;

/**
 * @author MIKETRAN
 *
 */
public class GatewayCallbackTest implements GatewayCallback {
	
	private static final String CLASS_NAME = GatewayCallbackTest.class.getName();
	
	Properties props;
	
	public GatewayCallbackTest(Properties properties) {
		props = properties;
	}

	/* (non-Javadoc)
	 * @see com.ibm.iotf.client.gateway.GatewayCallback#processCommand(com.ibm.iotf.client.gateway.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
		final String METHOD = "processCommand";
		String payload = new String(cmd.getRawPayload());
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Command for type(" 
				+ cmd.getDeviceType()
				+ ") ID(" + cmd.getDeviceId() + ") Payload(" + payload + ")");
	}

	/* (non-Javadoc)
	 * @see com.ibm.iotf.client.gateway.GatewayCallback#processNotification(com.ibm.iotf.client.gateway.Notification)
	 */
	@Override
	public void processNotification(Notification notification) {
		final String METHOD = "processNotification";
		String payload = new String(notification.getMessage().getPayload());
		LoggerUtility.log(Level.INFO, CLASS_NAME, METHOD, "Notification for type(" 
				+ notification.getDeviceType()
				+ ") ID(" + notification.getDeviceId() + ") Payload(" + payload + ")");
	}

}

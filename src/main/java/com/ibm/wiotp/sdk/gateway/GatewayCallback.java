/**
 *****************************************************************************
 * Copyright (c) 2016-19 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 */
package com.ibm.wiotp.sdk.gateway;

/**
 * <p> A callback interface that needs to be implemented by the 
 * Gateway to handle the commands or notifications from IBM Watson IoT Platform.</p>
 * 
 * <p>Gateway can subscribe to commands directed at the gateway itself and 
 * to any device connected via the gateway. To process specific commands, 
 * the Gateway needs to register a command callback method. Once the Command 
 * callback is added to the GatewayClient, the {@link #processCommand(com.ibm.wiotp.sdk.gateway.Command)}
 * method is invoked whenever any command is published on the subscribed criteria.</p>
 *
 */
public interface GatewayCallback<T> {
	
	/**
	 * Method to be called by the {@link com.ibm.wiotp.sdk.gateway.GatewayClient} when any 
	 * command is published on the subscribed criteria.
	 * 
	 * @param cmd an instance of {@link com.ibm.wiotp.sdk.gateway.Command}.
	 */
	public void processCommand(Command<T> cmd);
	
	/**
	 * <p> If a gateways subscribes to a topic of a device or sends data on behalf of a device 
	 * where the gateway does not have permission for, the message or the subscription is being ignored. 
	 * This behavior is different compared to applications where the connection will be terminated. 
	 * The Gateway will be notified on the notification topic:.</p>
	 * 
	 * <ul class="simple">
	 * <li>iot-2/type/+/id/+/notify
	 * 	</ul>
	 * @param notification Notification from the Watson IoT Platform that needs to be processed.
	 */ 
	public void processNotification(Notification notification);
}

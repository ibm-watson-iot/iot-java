/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Patrizia Gufler1 - Initial Contribution
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ibm.iotf.client.gateway.Command;
import com.ibm.iotf.client.gateway.GatewayCallback;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.client.gateway.Notification;
import com.ibm.iotf.sample.gateway.home.Device;

/**
 * A Command call back class that handles the LED blink command
 * 
 * The callback method processCommand() is invoked by the GatewayClient when it receives any command 
 * for Arduino Uno from Watson IoT Platform. The Gateway CommandCallback defines a BlockingQueue 
 * to store and process the commands (in separate thread) for smooth handling of MQTT publish message.
 */
public class GatewayCommandCallback implements GatewayCallback, Runnable {

	private GatewayClient gateway = null;
	
	// A queue to hold & process the commands
	private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
	private Map<String, Device> deviceMap = new HashMap<String, Device>();

	public GatewayCommandCallback(GatewayClient gateway) {
		this.gateway = gateway;
	}
	
	public void addDeviceInterface(String deviceId, Device device) {
		deviceMap.put(deviceId, device);
	}

	@Override
	public void processCommand(Command cmd) {
		try {
			queue.put(cmd);
		} catch (InterruptedException e) {
		}			
	}

	@Override
	public void run() {
		while(true) {
			Command cmd = null;
			try {
				cmd = queue.take();
				Device device = deviceMap.get(getKey(cmd));
				// check if this command is for the gateway
				if(device == null && cmd.getDeviceId().equals(this.gateway.getGWDeviceId()) &&
						cmd.getDeviceType().equals(this.gateway.getGWDeviceType())) {
					System.out.println("-->(GW) Got command for this gateway:: "+cmd);
					return;
				} else {
					System.out.println("-->(DE) Got command for the device:: "+cmd.getDeviceId());
				}
				device.processCommand(cmd.getPayload());
			} catch (InterruptedException e1) {
					// Ignore the Interuppted exception, retry
					continue;
			}
				
		}
	}

	/**
	 * Let us use the WIoTP client Id as the key to identify the device
	 * @return
	 */
	private String getKey(Command cmd) {
		return new StringBuilder("d:").
				append(this.gateway.getOrgId()).
				append(':').
				append(cmd.getDeviceType()).
				append(':').
				append(cmd.getDeviceId()).toString();
	}

	@Override
	public void processNotification(Notification notification) {
		// TODO Auto-generated method stub
		
	}
}
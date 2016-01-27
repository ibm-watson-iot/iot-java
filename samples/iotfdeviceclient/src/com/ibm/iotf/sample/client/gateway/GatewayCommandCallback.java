/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.iotf.client.gateway.Command;
import com.ibm.iotf.client.gateway.CommandCallback;

/**
 * A Command call back class that handles the LED blink command
 * 
 * The callback method processCommand() is invoked by the GatewayClient when it receives any command 
 * for Arduino Uno from Watson IoT Platform. The Gateway CommandCallback defines a BlockingQueue 
 * to store and process the commands (in separate thread) for smooth handling of MQTT publish message.
 */
public class GatewayCommandCallback implements CommandCallback, Runnable {

	protected final static JsonParser JSON_PARSER = new JsonParser();
	
	private static String LED_DATAPOINT_NAME = "led";
	
	// A queue to hold & process the commands
	private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();
	private Map<String, DeviceInterface> deviceMap = new HashMap<String, DeviceInterface>();

	public GatewayCommandCallback() {
		
	}
	
	public void addDeviceInterface(String deviceId, DeviceInterface device) {
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
				DeviceInterface device = deviceMap.get(cmd.getDeviceId());
				
				String value = null;
				try {
					JsonObject payloadJson = JSON_PARSER.parse(cmd.getPayload()).getAsJsonObject();
					if (payloadJson.has("d")) {
						payloadJson = payloadJson.get("d").getAsJsonObject();
					} 
					value = payloadJson.get(LED_DATAPOINT_NAME).getAsString();
				} catch (JsonSyntaxException e) {
					System.err.println("JsonSyntaxException thrown");
				} catch (JsonParseException jpe) {
					System.err.println("JsonParseException thrown");							
				}
				
				device.sendCommand(value);
			} catch (InterruptedException e1) {
					// Ignore the Interuppted exception, retry
					continue;
			}
				
		}
	}
}
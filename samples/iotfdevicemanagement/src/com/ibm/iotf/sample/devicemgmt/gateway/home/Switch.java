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
package com.ibm.iotf.sample.devicemgmt.gateway.home;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.ibm.iotf.client.gateway.GatewayClient;

/**
 * <p>This class act as a On/Off Smart Switch simulator. <br> 
 *  
 * This Object sends an event to Watson IoT Platform every 1 minute, 
 * and listens for command to on/off the switch.</p>
 *      
 */
public class Switch extends Device {
	/**
	 * Switch parameters
	 */
	private String status = "OFF"; // default value is off
	
	public Switch(String deviceId, GatewayClient gwClient, int updateInterval) {
		super(deviceId, gwClient, updateInterval);
		this.deviceType = DeviceType.SWITCH;
		this.gwClient = gwClient;
	}
	
	/**
	 * This method sends the status event (Switch status event) at every interval.
	 */
	public void run() {
		// Ideally one should read the current values from the Switch and report to IoT Platform
		JsonObject event = new JsonObject();
		event.addProperty("status", status);
		System.out.println("<--("+this.getDeviceId()+") "+event);
		boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "status", event);
		if(status == false) {
			System.err.println("Event publish from device "+this.getDeviceId() + " failed");
		}
	}

	/**
	 * The gateway sends command to on/off switch
	 */
	public void sendCommand(String cmd) {
		try {
			JsonObject payloadJson = JSON_PARSER.parse(cmd).getAsJsonObject();
			if (payloadJson.has("d")) {
				payloadJson = payloadJson.get("d").getAsJsonObject();
			}
			this.status = payloadJson.get("status").getAsString();
			System.out.println("-->(" + this.getDeviceId() +") Switch status is changed to " + this.status);
		
		} catch (JsonSyntaxException e) {
			System.err.println("JsonSyntaxException thrown");
		} catch (JsonParseException jpe) {
			System.err.println("JsonParseException thrown");							
		}
	}

}
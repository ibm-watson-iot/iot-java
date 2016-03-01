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
package com.ibm.iotf.sample.devicemgmt.gateway.home;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.ibm.iotf.client.gateway.GatewayClient;

/**
 *  <p>This class act as a Light Simulator. <br>
 *  
 *  This Object is used to control a light source, such as a LED or other light. 
 *  It allows a light to be turned on or off and its brightness setting to be controlled 
 *  as a % between 0 and 100. <br>
 *  
 *  This Object does send an event to Watson IoT Platform every 1 minute, 
 *  and listens for the following,<br>
 *  
 *  <ul class="simple">
 * 		<li>Command to control the light parameters.
 * 		<li>Listen for Device reboot (DM request) from Watson IoT Platform
 * </ul>
 * </p>
 *      
 */
public class Light extends Device {
	/**
	 * Light parameters
	 */
	private int brightness = 100; // default value is 100%
	private String status = "OFF"; // default value is off
	
	public Light(String deviceId, GatewayClient gwClient, int updateInterval) {
		super(deviceId, gwClient, updateInterval);
		this.deviceType = DeviceType.LIGHT;
	}
	
	public void initialize() {
		brightness = 100; // default value is 100%
		status = "OFF"; // default value is off
	}
	
	/**
	 * This method sends the status event (Light status event) whenever requested by the gateway
	 */
	public void run() {
		// Ideally one should read the current values from the Light and report to IoT Platform
		JsonObject event = new JsonObject();
		event.addProperty("brightness", brightness);
		event.addProperty("status", status);
		System.out.println("<--("+this.getDeviceId()+") "+event);
		boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "status", event);
		if(status == false) {
			System.err.println("Failed to publish the event from Oven");
		}

	}


	/**
	 * The gateway sends command to start/stop the light, and increase the brightness of the light as well
	 */
	public void sendCommand(String cmd) {
		try {
			JsonObject payloadJson = JSON_PARSER.parse(cmd).getAsJsonObject();
			if (payloadJson.has("d")) {
				payloadJson = payloadJson.get("d").getAsJsonObject();
			}
			
			this.brightness = payloadJson.get("brightness").getAsInt();
			this.status = payloadJson.get("status").getAsString();
			
		} catch (JsonSyntaxException e) {
			System.err.println("JsonSyntaxException thrown");
		} catch (JsonParseException jpe) {
			System.err.println("JsonParseException thrown");							
		}
		
	}
}
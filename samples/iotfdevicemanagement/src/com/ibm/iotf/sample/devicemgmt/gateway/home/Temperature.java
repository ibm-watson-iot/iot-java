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

import java.util.Random;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.gateway.GatewayClient;

/**
 * <p>This class act as a Temperature device that reads the temperature. <br> 
 *  
 * This Object sends an event to Watson IoT Platform every 1 minute, 
 * and listens for command to on/off the switch.</p>
 *      
 */
public class Temperature extends Device {
	
	private Random random = new Random();

	public Temperature(String deviceId, GatewayClient gwClient, int updateInterval) {
		super(deviceId, gwClient, updateInterval);
		this.deviceType = DeviceType.TEMPERATURE;
	}
	
	/**
	 * This method sends the status event (Switch status event).
	 */
	public void run() {
		// Ideally one should read the current values from the Switch and report to IoT Platform
		JsonObject event = new JsonObject();
		// send higher value randomly
		if(getRandomValue(0, 100) == 30) {
			event.addProperty("temp", 103);	// in fahrenheit
		} else {
			event.addProperty("temp", getRandomValue(80, 100));	// in fahrenheit
		}
		event.addProperty("humidity", getRandomValue(40, 100));	// in fahrenheit
		
		System.out.println("<--("+this.getDeviceId()+") "+event);
		boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "reading", event);
		if(status == false) {
			System.err.println("Event publish from device "+this.getDeviceId() + " failed");
		}
	}
	
	private int getRandomValue(int min, int max) {
		return (min + random.nextInt(max - min) + 1);
	}

}
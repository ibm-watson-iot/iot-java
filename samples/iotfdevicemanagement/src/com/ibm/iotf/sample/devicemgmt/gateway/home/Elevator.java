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

class Elevator extends Device {
	
	private Random random = new Random();
	
	public Elevator(String deviceId, GatewayClient gwClient, int updateInterval) {
		super(deviceId, gwClient, updateInterval);
		this.deviceType = DeviceType.ELEVATOR;
	}
	
	/**
	 * This method sends the status event (Elevator status event) at every interval.
	 */
	public void run() {
		// Ideally one should read the current values from the Switch and report to IoT Platform
		JsonObject event = new JsonObject();
		event.addProperty("floor", getRandomValue(0, 7));
		event.addProperty("fan", (getRandomValue(0, 1)==0?true:false));
		event.addProperty("load", getRandomValue(0, 20));
		System.out.println("<--("+this.getDeviceId()+") "+event);
		boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "status", event);
		if(status == false) {
			System.err.println("Event publish from device "+this.getDeviceId() + " failed");
		}
	}

	private int getRandomValue(int min, int max) {
		return (min + random.nextInt(max - min) + 1);
	}

}

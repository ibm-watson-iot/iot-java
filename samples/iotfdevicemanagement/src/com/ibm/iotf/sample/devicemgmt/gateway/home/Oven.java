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
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.ibm.iotf.client.gateway.GatewayClient;

/**
 *  <p>This class act as a Oven Simulator. 
 *  
 *  This Object does send an event to Watson IoT Platform every 
 *  5 seconds when the Oven is instructed to cook something, and it listens for the following,<br>
 *  
 *  <ul class="simple">
 * 		<li>Command to start the cooking process with various parameters
 * 		<li>Listen for Device reboot (DM request) from Watson IoT Platform
 * 		<li>Listen for Firmware Actions (DM request) from Watson IoT Platform
 * </ul>
 * </p>
 * 
 * Also, this object initially starts with a 1 minute cooking, so that the Oven can send the events to IoT Platform.
 *      
 */
public class Oven extends Device {
	
	/**
	 * Oven parameters
	 */
	
	/**
	 * Cool - 110 - 140ºC 
	 * Slow - 150 - 160ºC
	 * warm - 180 - 190ºC
	 * Moderately-hot - 190 - 220ºC
	 * Hot - 220 - 230ºC
	 * Very hot - 250 - 260ºC
	 */
	private MODE mode = MODE.HOT;
	
	
	private static int temperatureRange[][] = {{110, 140}, {150, 160},
		{180, 190}, {190,220}, {220, 230}, {250, 260}};
	
	/**
	 * Top level of the oven - this level is best for quick cooking and high temperature cooking
	 * Middle level of the oven - this level is good for moderate temperature cooking
	 * Bottom level of the oven - this level is best for slow cooking and low temperature cooking
	 */
	private String position = "top";
	
	private int timer = 120; // in seconds
	private int timeRun = 0; // in seconds
	private boolean bRunning = true; // state of the Oven

	private Random random = new Random();

	public Oven(String deviceId, GatewayClient gwClient, int updateInterval) {
		super(deviceId, gwClient, updateInterval);
		this.deviceType = DeviceType.OVEN;
	}
	
	/**
	 * The gateway sends command to start/stop the cooking
	 */
	public void sendCommand(String cmd) {
		
		try {
			JsonObject payloadJson = JSON_PARSER.parse(cmd).getAsJsonObject();
			if (payloadJson.has("d")) {
				payloadJson = payloadJson.get("d").getAsJsonObject();
			}
			
			this.mode = MODE.get(payloadJson.get("mode").getAsString());
			this.position = payloadJson.get("position").getAsString();
			this.timer = payloadJson.get("timer").getAsInt();
			
			this.timeRun = 0;
			this.sendStartEvent();
		
		} catch (JsonSyntaxException e) {
			System.err.println("JsonSyntaxException thrown");
		} catch (JsonParseException jpe) {
			System.err.println("JsonParseException thrown");							
		}
	}
	
	/**
	 * This method sends the status event (Oven status event) every 5 seconds
	 * from the time its start running (when some cooking is started) to till it
	 * finishes.
	 * 
	 * The oven is started listening from the command from the application.
	 * Its stopped when the timer is elapsed.
	 */
	
	public void run() {
		
		try {
		if(this.bRunning) {
			if(this.timeRun >= this.timer) {
				// The oven is finished cooking, so send a stop event.
				sendStopEvent();
				return;
			}
			
			JsonObject event = new JsonObject();
			event.addProperty("temp", getRandomValue(temperatureRange[mode.getMode()][0], temperatureRange[mode.getMode()][1]));
			event.addProperty("position", position);
			event.addProperty("mode", mode.toString());
			event.addProperty("time-run", timeRun);
			
			System.out.println("<--("+this.getDeviceId()+") "+event);
			boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "status", event);
			if(status == false) {
				System.err.println("Event publish from device "+this.getDeviceId() + " failed");
			}
			
			timeRun = timeRun + this.getEventUpdateInterval();
		} 
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Once finished cooking send a stop event, Ideally these events come from the Oven to
	 * Gateway via serial or through WiFi communication, but in this case, we simulate the
	 * event in this object itself, just to demonstrate the Gateway functionality.
	 */
	private void sendStopEvent() {
		while(this.bRunning) {
			JsonObject event = new JsonObject();
			System.out.println("<--("+this.getDeviceId()+") stop event "+event);
			boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "stop", event);
			if(status == false) {
				System.err.println("Failed to publish the event from Oven");
			} 
			this.bRunning = false;
			this.timer = 0;
			this.timeRun = 0;
		}
	}
	
	/**
	 * Once cooking is started either remotely/locally send a start event, Ideally these events come from the Oven to
	 * Gateway via serial or through WiFi communication, but in this case, we simulate the
	 * event in this object itself, just to demonstrate the Gateway functionality.
	 */
	private void sendStartEvent() {
		if(this.bRunning) {
			JsonObject event = new JsonObject();
			event.addProperty("temp", getRandomValue(temperatureRange[mode.getMode()][0], temperatureRange[mode.getMode()][1]));
			event.addProperty("position", position);
			event.addProperty("timer", timer);
			System.out.println("<--("+this.getDeviceId()+") start event "+event);
			boolean status = this.gwClient.publishDeviceEvent(deviceType.getDeviceType(), this.getDeviceId(), "start", event);
			if(status == false) {
				System.err.println("Failed to publish the event from Oven");
			}
		}
		
	}

	private int getRandomValue(int min, int max) {
		return (min + random.nextInt(max - min) + 1);
	}
	
	enum MODE {
		COOL(0), SLOW(1), WARM(2), MODHOT(3), HOT(4), VERYHOT(5);
		
		private final int mode;
		
		private int getMode() {
			return this.mode;
		}
		
		private MODE(int mode) {
			this.mode = mode;
		}
		
		public static MODE get(String mode) {
			switch(mode) {
			case "COOL":	return MODE.COOL;
			case "SLOW":	return MODE.SLOW;
			case "WARM":	return MODE.WARM;
			case "MODHOT": 	return MODE.MODHOT;
			case "HOT":		return MODE.HOT;
			case "VERYHOT":	return MODE.VERYHOT;
			}
			return null;
		}
	}
}
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

import java.util.Date;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.devicemgmt.gateway.GatewayFirmwareHandlerSample;

/**
 *  <p>This class act as a Arduino Uno Simulator. Please take a look at the ArduinoSerialInterface for
 *  a real implementation. </p>
 *      
 */
public class ArduinoSimulatorInterface extends Thread implements DeviceInterface {
	
	private static String LED_DATAPOINT_NAME = "led";
	
	/**
	 * Watson IoT Platform related parameters
	 */
	private String deviceId;
	private String deviceType;

	// Let us have a counter that gets written after a firmware update
	// also, gets reset whenever there is a reboot
	private int count;
	
	private GatewayClient gwClient;
	private Random random = new Random();
	
	public ArduinoSimulatorInterface(String deviceId, String deviceType, String port, GatewayClient gatewayClient) {
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.gwClient = gatewayClient;
	}
	
	private String downloadedFirmwareName;
	private volatile boolean bDisplay = false;
	private volatile boolean firmwareUpdated;
	private volatile boolean updateInProgress;
	private static final String CLASS_NAME = ArduinoSimulatorInterface.class.getName();

	
	public void initialize() {
		this.start();
		
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		// do nothing
	}

	/**
	 * The Raspberry Pi gateway can send data to Arduino Uno by writing data 
	 * on the output stream that we opened earlier:
	 */
	public void sendCommand(String cmd) {
		System.out.println("writing the cmd to arduino "+cmd);
		
		String value = null;
		try {
			JsonObject payloadJson = JSON_PARSER.parse(cmd).getAsJsonObject();
			if (payloadJson.has("d")) {
				payloadJson = payloadJson.get("d").getAsJsonObject();
			} 
			value = payloadJson.get(LED_DATAPOINT_NAME).getAsString();
		} catch (JsonSyntaxException e) {
			System.err.println("JsonSyntaxException thrown");
		} catch (JsonParseException jpe) {
			System.err.println("JsonParseException thrown");							
		}
		
		
		int number = Integer.parseInt(value);
		if(number != 0) {
			System.out.println("Arduino Uno blinked " + number +" times  !!");
		}
	}
	
	public void run() {
	
		while(true) {
			if(!this.updateInProgress) {
				JsonObject event = new JsonObject();
				event.addProperty("pir", random.nextInt(2));
				event.addProperty("temp", 34 + random.nextDouble() - random.nextInt(3));
				
				if(this.firmwareUpdated) {
					count++;
					event.addProperty("count", count);
				}
				boolean status = this.gwClient.publishDeviceEvent(this.deviceType, this.deviceId, "status", event);
				if(status == false) {
					System.err.println("Failed to publish the temperature from Arduino");
				} else if(bDisplay) {
					System.out.println("<-- (DE) Arduino event :: "+event);
				}
			}
			
			try {
				Thread.sleep(500);
			} catch(Exception e) {}
		}
	}

	@Override
	public void setFirmwareName(String downloadedFirmwareName) {
		this.downloadedFirmwareName = downloadedFirmwareName;
		
	}

	/**
	 * <p>A sample firmware update method that pretends to install the arduino.hex sketch
	 * to Arduino Uno.</p>
	 * 
	 * This method adds a diagnostic log containing the progress to the IoT Platform.
	 * 
	 */
	@Override
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		
		System.out.println(CLASS_NAME + ": Firmware update start... for device = "+deviceFirmware.getDeviceId());
		
		updateInProgress = true;
		ManagedGateway gateway = ((ManagedGateway) this.gwClient);
		
		Date timestamp = new Date();
		String message = "Firmware Update Event start";
		LogSeverity severity = LogSeverity.informational;
		// Inform the server about the status through Diaglog if needed
		gateway.addDeviceLog(this.deviceType, this.deviceId, message, timestamp, severity);
		
		System.out.println("Progress ::");
		
		for(int i = 1; i < 21; i++) {
			try {
				Thread.sleep(200);
			} catch(Exception e) {
				
			}
			// Inform the server about the progress through Diaglog if needed
			gateway.addDeviceLog(this.deviceType, this.deviceId, "progress " + (i * 5) , new Date(), severity);
			System.out.print("  "+ (i * 5) + "%");
		}

		// Inform the server about the status through Diaglog if needed
		message = "Firmware Update Event End";
		gateway.addDeviceLog(this.deviceType, this.deviceId, message, new Date(), severity);
				
		System.out.println("Firmware Update successfull !!");
		deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
		deviceFirmware.setState(FirmwareState.IDLE);
		
		/**
		 * Delete the temporary firmware file
		 */
		GatewayFirmwareHandlerSample.deleteFile(downloadedFirmwareName);
		
		this.downloadedFirmwareName = null;
		System.out.println(CLASS_NAME + ": Firmware update End...");
		firmwareUpdated = true;
		updateInProgress = false;
	}

	/**
	 * A sample method to handle the Arduino's reboot request from the DM server.
	 * resets the couter datapoint to 0.
	 * 
	 */
	@Override
	public void reboot(DeviceAction action) {
		updateInProgress = true;
		// Don nothing, just wait for sometime and reset the counter
		
		try {
			Thread.sleep(1000 * 10);
		} catch(InterruptedException ie) {}
		
		this.count = 0;
		
		// We must send a manage request inorder to complete the reboot request successfully
		System.out.println("The Arduino Uno device is reset successfully !!");
		try {
			ManagedGateway gateway = ((ManagedGateway) this.gwClient);
			gateway.sendDeviceManageRequest(this.deviceType, this.deviceId, 0, true, true);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		updateInProgress = false;
	}

	/**
	 * To trun on/off the sensor event output in the console (System.out)
	 */
	public void toggleDisplay() {
		this.bDisplay = !this.bDisplay;
	}
	
	@Override
	public void setLog(LogSeverity severity, String message, String data, Date date) {
		ManagedGateway gw = (ManagedGateway)this.gwClient;
		gw.addDeviceLog(this.deviceType, this.deviceId, message, date, severity);
	}
}
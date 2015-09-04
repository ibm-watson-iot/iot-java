/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.client.device;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.client.device.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.client.device.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.util.LoggerUtility;

/**
 * If a device supports device actions like reboot and factory reset, 
 * this abstract class <code>DeviceActionHandler</code>
 * should be extended by the device code.  
 * 
 * The <code>handleReboot</code> and <code>handleFactoryReset</code>
 * must be implemented to handle the actions
 *
 */
public abstract class DeviceActionHandler extends Thread implements PropertyChangeListener {
	
	private static final String CLASS_NAME = DeviceActionHandler.class.getName();
	
	private DeviceData device = null;
	
	private static final String REBOOT_ACTION = "Reboot";
	private static final String FACTORY_RESET_ACTION = "FactoryReset";
	
	private BlockingQueue<JsonObject> queue;
	private JsonObject dummy = new JsonObject();
	private boolean running = false;
	
	
	public DeviceActionHandler() {
		queue = new LinkedBlockingQueue<JsonObject>();
		this.setName("ActionHandler-Thread");
	}
	
	@Override
	public void run() {
		
		final String METHOD = "run";
		running = true;
		while (running) {
			try {
				JsonObject o = queue.take();
				if (o.equals(dummy)) {
					LoggerUtility.info(CLASS_NAME, METHOD,  "Is it time to quit?");
				} else {
					String action = o.get("action").getAsString();
					if (action.equals(REBOOT_ACTION)) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "Invoking reboot handler");
						handleReboot(this.device.getDeviceAction());
						device.getDeviceAction().fireEvent(DeviceAction.REBOOT_STOP);
					} else if (action.equals(FACTORY_RESET_ACTION)) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "Invoking factory reset handler");
						handleFactoryReset(this.device.getDeviceAction());
						device.getDeviceAction().fireEvent(DeviceAction.FACTORY_RESET_STOP);
					}
				}
			} catch (InterruptedException e) {
				LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected exception " + e.getMessage());
			}
		}
		LoggerUtility.info(CLASS_NAME, METHOD,  "Exiting...");
	}

	public void terminate() {
		running = false;
		try {
			queue.put(dummy);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final String METHOD = "propertyChange";
		try {
			String propName = evt.getPropertyName();
			if (propName.equals(DeviceAction.DEVICE_REBOOT_START)) {
				JsonObject o = new JsonObject();
				o.add("action", new JsonPrimitive(REBOOT_ACTION));
				queue.put(o);
			} else if (propName.equals(DeviceAction.DEVICE_FACTORY_RESET_START)) {
				JsonObject o = new JsonObject();
				o.add("action", new JsonPrimitive(FACTORY_RESET_ACTION));
				queue.put(o);
			}
		} catch (Exception e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected exception " + e.getMessage());
		}
	}
	
	/**
	 * Subclass must implement this method.  
	 * <br>If the device supports reboot, subclass should start rebooting the 
	 * device.  
	 *
	 * If reboot attempt fails, the “rc” is set to 500 and the “message” 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set “rc” to 501 and optionally set “message” accordingly
	 */
	public abstract void handleReboot(DeviceAction action);
	
	/**
	 * Subclass must implement this method.  
	 * <br>If the device supports factory reset, subclass should reset the 
	 * device to factory settings
	 *
	 * If the factory reset attempt fails, the “rc” should be 500 and the “message” 
	 * field should be set accordingly, if the factory reset action is not supported, 
	 * set “rc” to 501 and optionally set “message” accordingly.
	 */
	public abstract void handleFactoryReset(DeviceAction action);

	public void setDeviceData(DeviceData deviceData) {
		this.device = deviceData;
		
	}
}

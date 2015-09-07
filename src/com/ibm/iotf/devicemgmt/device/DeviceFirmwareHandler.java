/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Modified to include Resource Model
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.device;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.device.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.util.LoggerUtility;

/**
 * If a device supports firmware update, this abstract class <code>DMDeviceFirmwareHandler</code>
 * should be extended by the device code.  The <code>downloadFirmware</code> and <code>updateFirmware</code>
 * must be implemented to handle
 *
 */
public abstract class DeviceFirmwareHandler extends Thread implements PropertyChangeListener {
	
	private static final String CLASS_NAME = DeviceFirmwareHandler.class.getName();
	
	private DeviceData device = null;
	
	private static final String FIRMWARE_DOWNLOAD = "FirmwareDownload";
	private static final String FIRMWARE_UPDATE = "FirmwareUpdate";
	
	private BlockingQueue<JsonObject> queue;
	private JsonObject dummy = new JsonObject();
	private boolean running = false;
	
	
	public DeviceFirmwareHandler() {
		queue = new LinkedBlockingQueue<JsonObject>();
		this.setName("FirmwareHandler-Thread");
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
					if (action.equals(FIRMWARE_DOWNLOAD)) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "starting download firmware");
						device.getDeviceFirmware().setState(FirmwareState.DOWNLOADING);
						downloadFirmware(device.getDeviceFirmware());
					} else if (action.equals(FIRMWARE_UPDATE)) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "starting firmware update");
						device.getDeviceFirmware().setUpdateStatus(FirmwareUpdateStatus.IN_PROGRESS);
						
						updateFirmware(device.getDeviceFirmware());

						device.getDeviceFirmware().setState(FirmwareState.IDLE);
						if(device.getDeviceFirmware().getUpdateStatus() == FirmwareUpdateStatus.SUCCESS.getStatus()) {
							String version = device.getDeviceFirmware().getVersion();
							if(null != version && !("".equals(version))) {
								device.getDeviceInfo().setFwVersion(version);
							}
						}
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
			if (propName.equals(DeviceFirmware.FIRMWARE_DOWNLOAD_START)) {
				DeviceFirmware firmware = (DeviceFirmware)evt.getNewValue();
				if (firmware.getUrl() != null) {
					JsonObject o = new JsonObject();
					o.add("action", new JsonPrimitive(FIRMWARE_DOWNLOAD));
					queue.put(o);
				} else {
					LoggerUtility.severe(CLASS_NAME, METHOD, "URL is NULL");
				}
			} else if (propName.equals(DeviceFirmware.FIRMWARE_UPDATE_START)) {
				JsonObject o = new JsonObject();
				o.add("action", new JsonPrimitive(FIRMWARE_UPDATE));
				queue.put(o);
			}
		} catch (Exception e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected exception " + e.getMessage());
		}
	}
	
	/**
	 * Subclass must implement this method.  
	 * <br>If the device supports firmware download, subclass should start downloading
	 * firmware to the device.  When done, set the state and status accordingly.
	 *
	 * @param deviceFirmware
	 * @see DeviceFirmware
	 */
	public abstract void downloadFirmware(DeviceFirmware deviceFirmware);
	
	/**
	 * Subclass must implement this method.
	 * <br>If the device supports firmware update, subclass should start updating the
	 * firmware on the device.  When done, set the update status accordingly.
	 * @param deviceFirmware
	 * @see DeviceFirmware
	 */
	public abstract void updateFirmware(DeviceFirmware deviceFirmware);

	public void setDeviceData(DeviceData deviceData) {
		this.device = deviceData;
		
	}
}

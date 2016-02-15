/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
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
package com.ibm.iotf.devicemgmt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.util.LoggerUtility;
import com.ibm.iotf.devicemgmt.resource.Resource;
;/**
 * <p>If a Gateway (and devices behind Gateway) and directly connected device, supports firmware update, this abstract class <code>DeviceFirmwareHandler</code>
 * should be extended by the Gateway/Device code.</p> 
 * 
 * <p>The <code>downloadFirmware</code> and <code>updateFirmware</code>
 * must be implemented to handle</p>
 *
 */
public abstract class DeviceFirmwareHandler extends Thread implements PropertyChangeListener {
	
	private static final String CLASS_NAME = DeviceFirmwareHandler.class.getName();
	
	private static final int FIRMWARE_DOWNLOAD = 0;
	private static final int FIRMWARE_UPDATE = 1;
	
	private BlockingQueue<Event> queue;
	private Event dummy = new Event();
	private boolean running = false;
	
	
	public DeviceFirmwareHandler() {
		queue = new LinkedBlockingQueue<Event>();
		this.setName("FirmwareHandler-Thread");
	}
	
	@Override
	public void run() {
		
		final String METHOD = "run";
		running = true;
		while (running) {
			try {
				Event evt = queue.take();
				if (evt == dummy) {
					LoggerUtility.info(CLASS_NAME, METHOD,  "Is it time to quit?");
				} else {
					if (evt.action == FIRMWARE_DOWNLOAD) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "starting download firmware");
						evt.firmware.setState(FirmwareState.DOWNLOADING);
						downloadFirmware(evt.firmware);
					} else if (evt.action == FIRMWARE_UPDATE) {
						LoggerUtility.info(CLASS_NAME, METHOD,  "starting firmware update");
						evt.firmware.setUpdateStatus(FirmwareUpdateStatus.IN_PROGRESS);
						
						updateFirmware(evt.firmware);

						evt.firmware.setState(FirmwareState.IDLE);
						if(evt.firmware.getUpdateStatus() == FirmwareUpdateStatus.SUCCESS.getStatus()) {
							String version = evt.firmware.getVersion();
							if(null != version && !("".equals(version))) {
								Resource mgmt = evt.firmware.getParent();
								Resource root = mgmt.getParent();
								Resource deviceInfo = root.getChild(DeviceInfo.RESOURCE_NAME);
								if(deviceInfo != null) {
									((DeviceInfo) deviceInfo).setFwVersion(version);
								}
							}
						}
						evt.firmware.setVerifier(null);
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

	/**
	 * This method listens for the Firmware events and calls the
	 * appropriate methods to complete the action. 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		final String METHOD = "propertyChange";
		try {
			String propName = pce.getPropertyName();
			if (propName.equals(DeviceFirmware.FIRMWARE_DOWNLOAD_START)) {
				DeviceFirmware firmware = (DeviceFirmware)pce.getNewValue();
				if (firmware.getUrl() != null) {
					Event evnt = new Event();
					evnt.action = FIRMWARE_DOWNLOAD;
					evnt.firmware = (DeviceFirmware) pce.getNewValue();
					queue.put(evnt);
				} else {
					LoggerUtility.severe(CLASS_NAME, METHOD, "URL is NULL");
				}
			} else if (propName.equals(DeviceFirmware.FIRMWARE_UPDATE_START)) {
				Event evnt = new Event();
				evnt.action = FIRMWARE_UPDATE;
				evnt.firmware = (DeviceFirmware) pce.getNewValue();
				queue.put(evnt);
			}
		} catch (Exception e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unexpected exception " + e.getMessage());
		}
	}
	
	private class Event {
		private int action;
		private DeviceFirmware firmware;
	}
	
	/**
	 * <p>Subclass must implement this method.</p>  
	 * <p>If the Gateway or Device supports firmware download, subclass must add logic to
	 *  download the firmware to the device. When done, set the state and status accordingly.</p>
	 *  
	 *  <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceFirmware} to retrieve the 
	 *  DeviceType and DeviceId for which the firmware download request is received and act accordingly.</p> 
	 *
	 * @param deviceFirmware DeviceFirmware where the device code can set the Firmware Download progress
	 * @see DeviceFirmware
	 */
	public abstract void downloadFirmware(DeviceFirmware deviceFirmware);
	
	/**
	 * <p> Subclass must implement this method. </p>
	 * <p>If the device supports firmware update, subclass should start updating the
	 * firmware on the device.  When done, set the update status accordingly.</p>
	 * 
	 * <p>Gateway must use the class {@link com.ibm.iotf.devicemgmt.DeviceFirmware} to retrieve the 
	 *  DeviceType and DeviceId for which the firmware update request is received and act accordingly.</p>
	 *  
	 * @param deviceFirmware DeviceFirmware where the device code can set the Firmware Update progress
	 * @see DeviceFirmware
	 */
	public abstract void updateFirmware(DeviceFirmware deviceFirmware);
}

/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 Prasanna Alur Mathada - Initial Contribution
 *****************************************************************************
 *
 */

package com.ibm.iotf.sample.devicemgmt.device;

import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceFirmwareHandler;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

public abstract class Handler extends DeviceFirmwareHandler {
	
	protected static String FACTORY_FIRMWARE_NAME = "iot_1.0-1_armhf.deb";
	protected static String FACTORY_FIRMWARE_VERSION = "1.0.1";
	protected ManagedDevice dmClient = null;
	
	public Handler(ManagedDevice dmClient) {
		this.dmClient = dmClient;
		DeviceData deviceData = dmClient.getDeviceData();
		if(null != deviceData) {
			DeviceFirmware firm = deviceData.getDeviceFirmware();
			if(null != firm) {
				currentFirmware = firm.getName();
				currentFirmwareVersion = firm.getVersion();
			}
		}
	}
	
	protected abstract void prepare(String propertiesFile);
	
	protected String currentFirmware = "iot_1.0-2_armhf.deb";
	protected String latestFirmware;
	protected String currentFirmwareVersion = "1.0.2";
	
	protected HTTPFirmwareDownload downloadTask;
	protected DebianFirmwareUpdate updateTask;
	
	/**
	 * Gets the current firmware
	 * @return
	 */	
	protected String getCurrentFirmware() {
		return currentFirmware;
	}
	
	/**
	 * Sets the current firmware
	 * @param currentFirmware
	 */
	protected void setCurrentFirmware(String currentFirmware) {
		this.currentFirmware = currentFirmware;
	}
	
	/**
	 * Gets the latest firmware
	 * @return
	 */
	protected String getLatestFirmware() {
		return latestFirmware;
	}
	
	/**
	 * Sets the latest firmware
	 * @param latestFirmware
	 */
	protected void setLatestFirmware(String latestFirmware) {
		this.latestFirmware = latestFirmware;
	}
	
	/**
	 * Based on the User input on the mode or choice of source of Firmware Upgrade, associated
	 * CASE invokes relevant methods.  
	 * @param option
	 * @param dmClient
	 * @return
	 */
	public static Handler createHandler(String option, ManagedDevice dmClient) {
		switch(option) {
			case "Device": return new DeviceInitiatedHandlerSample(dmClient);
			
			case "Platform": return new PlatformInitiatedHandlerSample(dmClient);
			
			case "PlatformBackground": return new PlatformInitiatedWithBkgrndDwnldHandlerSample(dmClient);
		}
		
		return null;
	}
}

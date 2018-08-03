/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
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
package com.ibm.iotf.sample.devicemgmt.device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

/**
 * This Platform Initiated Firmware handler demonstrates how the Firmware Upgrade 
 * is performed, with the package being downloaded by providing the URL over the 
 * Platform and have it upgraded in simple steps. On failure, the sample prefers 
 * to restore the current firmware version on the Device and if that doesn't 
 * happen successfully, then it performs factory reset, to apply factory version
 * of the Firmware
 */

public class PlatformInitiatedHandlerSample extends Handler {
	
	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	public PlatformInitiatedHandlerSample(ManagedDevice dmClient) {
		super(dmClient);
	}
	
	@Override
	public void prepare(String propertiesFile) {
		
		// Create download task
		downloadTask = new HTTPFirmwareDownload(true, this.dmClient);
		
		// Create update task
		updateTask = new DebianFirmwareUpdate(true);
		
		try {
			dmClient.addFirmwareHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void downloadFirmware(final DeviceFirmware deviceFirmware) {
		System.out.println("Recieved Firmware Download Request");		
		Runnable r = new Runnable() {
			public void run() {
				downloadTask.setDeviceFirmware(deviceFirmware);
				setLatestFirmware(downloadTask.downloadFirmware());
				System.out.println("Latest Firmware is " +latestFirmware);
			}
		};
		
		executor.execute(r);
	}
	
	@Override
	public void updateFirmware(final DeviceFirmware deviceFirmware) {
		System.out.println("Recieved Firmware Update Request");		
		Runnable r = new Runnable(){
			public void run(){
				updateTask.setDeviceFirmware(deviceFirmware);
				boolean status = updateTask.updateFirmware(getLatestFirmware());
				
				System.out.println("value of status is " +status);
				
				/**
				 * Trigger the upgrade to the latest firmware
				 */
				if ( status == true ){
					setCurrentFirmware(getLatestFirmware());
					currentFirmwareVersion = deviceFirmware.getVersion();
					System.out.println("Successfully Upgraded the Device with latest firmware");
				} else {
					System.out.println("Upgrade failed. Rolling back to the current version");
					status = updateTask.updateFirmware(getCurrentFirmware());
					if (status == true) {
						System.out.println("Successfully rolled back. Retained Current Firmware as-is ");
					} else {
						updateTask.updateFirmware(Handler.FACTORY_FIRMWARE_NAME);
						System.out.println("Restored Factory Firmware version after roll back to Current version failed");
						setCurrentFirmware(Handler.FACTORY_FIRMWARE_NAME);
						currentFirmwareVersion = Handler.FACTORY_FIRMWARE_VERSION;
					} 
				}
				updateWatsonIoT();
			}
		};
		
		executor.execute(r);
	}
	
	/**
	 * The updateWatsonIoT() method, post completion of Firmware Upgrade, updates the 
	 * Watson IoT Platform with the Firmware version details
	 */
	private void updateWatsonIoT() {
		DeviceInfo deviceInfo = dmClient.getDeviceData().getDeviceInfo();
		System.out.println("Updating the current firmware version "+currentFirmwareVersion+" as the Device Firmware Version on Watson IoT Platform Dashboard ");
		deviceInfo.setFwVersion(currentFirmwareVersion);
		try {
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			System.err.println("Failed to update the new Firmware version to the Watson IoT Platform");
			e.printStackTrace();
		}	
	}
}	
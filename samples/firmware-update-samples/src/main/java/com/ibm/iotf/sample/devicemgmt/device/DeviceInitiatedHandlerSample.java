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

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Properties;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.*;
import com.cloudant.client.api.model.ChangesResult.Row;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.DeviceInfo;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;

import org.apache.commons.net.util.Base64;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * This Device Initiated Firmware handler demonstrates how the device snoops for the 
 * availability of the newer version of firmware in the repository. Once a newer version
 * is made available on the Cloudant NoSQL Database, Firmware Download and Firmware Upgrade 
 * are triggered and performed. On failure, the sample performs the factory reset.
 */

/**
 * Device Initiated Factory Reset
 * --------------------------------
 * Based on the Properties file parameter, as set by the user preference, the DeviceInitiated action shall be executed
 * based on the flow defined in this section. The Device shall snoop the Cloudant NoSQL DB ( Firmware Repository)
 * and checks for the availability of a newer version of firmware. If a newer firmware version is available, then it 
 * shall initiate download and subsequently trigger firmware update process. If upgrade fails, then, the action is to 
 * fall back on the Active firmware. If successful, then fine, else next action is to fall back on to the Base firmware
 * version 
 * 
 * Here, the flow is as follows:
 * 1. Snoop the Cloudant NoSQL DB to see if there's a newer version of firmware available for download
 * 2. If No, then, do nothing
 * 3. If Yes, then, cross verify, if the latest version is different from the Active Firmware available on the Device
 * 4. On confirmation, download the latest firmware and pass on the file information to the 'applyFirmware() method
 * 
 */

public class DeviceInitiatedHandlerSample extends Handler implements Runnable {
	
	public DeviceInitiatedHandlerSample(ManagedDevice dmClient) {
		super(dmClient);
	}

	private Database firmwareDB;
	private String firmwareDBSequence;
	private String docId;
	private String latestFirmwareVersion;
	
	/**
	 * Connect to Cloundant NoSQL DB based on the authentication credentials provided as
	 * user inputs in the DMDeviceSample.properties file. 
	 * 
	 **/
	@Override
	public void prepare(String propertiesFile) {
	
		/**
		 * Load device properties file
		 */
		Properties props = new Properties();
		try {
			props.load(DeviceInitiatedHandlerSample.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		/**
		 * Read individual parameter values from device properties file
		 */
			
		String username = trimedValue(props.getProperty("User-Name"));
		String password = trimedValue(props.getProperty("Password"));
		
		currentFirmwareVersion = (props.getProperty("DeviceInfo.fwVersion"));

		StringBuilder sb = new StringBuilder();
		sb.append("https://")
		.append(username)
		.append(":")
		.append(password)
		.append("@")
		.append(username)
		.append(".cloudant.com");
			
		System.out.println(sb);
		
		CloudantClient client = new CloudantClient(sb.toString(), username, password);
			
		System.out.println("Connected to Cloudant");
		System.out.println("Server Version: " + client.serverVersion());

		/**
		 * Pass the name of the Cloudant NoSQL Database to 'firmwareDB'
		 * To create the Cloudant NoSQL Database 'firmware_repository', replace 'false' with 'true'
		 */
		
		firmwareDB = client.database("firmware_repository", false);
		
		// Create update task
		updateTask = new DebianFirmwareUpdate(false);
		
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try{
				/**
				 * Compare the firmware version on device with the one on Cloudant Database
				 * If the firmware version on Cloudant Database is higher, then initiate
				 * Firmware Download from Cloudant and trigger Firmware Upgrade.
				 * Finally, update the firmware version to Watson IoT Platform.
				 */
				if (checkAndSetFirmware()){
					downloadFromCloudant();
					// ToDo: update the firmware
					updateFirmware(dmClient.getDeviceData().getDeviceFirmware()); // ToDo: we need to maintain and keep updating the DeviceFirmware object
					updateWatsonIoT();
				} 
				/** If the firmware version on device and Cloudant Database is same,
				 * then, sleep for 60 Seconds and compare again after sleep times out.
				 */
			    Thread.sleep(1000 * 60); // ToDo: configure 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * The checkAndSetFirmware method snoops the Cloudant NoSQL DB for any new addition of
	 * Firmware package, by comparing the firmware version on the device with the latest 
	 * Document on Cloudant Database. If a latest firmware package is detected or identified,
	 * then, the method returns the name and version of the Firmware package.
	 **/
	
	private boolean checkAndSetFirmware() {
			
		ChangesResult changes = null;
		if(this.firmwareDBSequence == null) {
			changes = firmwareDB.changes()
					.includeDocs(true)
					.getChanges();
		} else {
			changes = firmwareDB.changes()
					.includeDocs(true)
					.since(firmwareDBSequence)
					.getChanges();
		}
		firmwareDBSequence = firmwareDB.info().getUpdateSeq();
		String version = this.currentFirmwareVersion;
		boolean updatedNeeded = false;
		if(changes.getResults().size() != 0) {
			List<ChangesResult.Row> rows = changes.getResults();
			List<JsonObject> jsonList = new ArrayList<JsonObject>(rows.size());
			
			for (Row row : rows) {
				jsonList.add(row.getDoc());
				// Fetch the Cloudant Document(s)
				JsonObject attachment = row.getDoc();
				// Ignore deleted Documents
				if(attachment.get("_deleted") != null && attachment.get("_deleted").getAsBoolean() == true) {
					continue;
				}
				
				System.out.println(attachment);
				// Retrieve the version of the firmware package
				String retrievedVersion = attachment.get("version").getAsString();
				System.out.println(retrievedVersion);
				// Compare the firmware version on device with the one retrieved from Cloudant Database 
                if(isVersionGreater(version, retrievedVersion)) {
                	// Successful comparison indicates that Firmware update is needed
                	updatedNeeded = true;
                	// Fetch Document ID of the latest firmware
                	docId = row.getId();
                	// Fetch Firmware version of the latest firmware
                	latestFirmwareVersion = retrievedVersion;
                	version = retrievedVersion;
                    
	                JsonObject obj = attachment.get("_attachments").getAsJsonObject();
					Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
									
					Iterator<Entry<String, JsonElement>> itr = entrySet.iterator();
					if(itr.hasNext()) {
						Entry<String, JsonElement> entry = itr.next();
						// Set the name of the latest firmware package
						setLatestFirmware(entry.getKey());
						System.out.println("Setting latest firmware to "+entry.getKey());
					}
				}
			}
		} 
		// Return True if Firmware Update is needed, else, return False
		return updatedNeeded;
	}
	
	/**
	 * This method checks whether the retrieved version ( Firmware version on Cloudant DB) 
	 * is higher than the deviceVersion (Firmware version on the Device).
	 */
	private boolean isVersionGreater(String deviceVersion, String retrievedVersion) {
		String[] retrieved = retrievedVersion.split("\\.");
		String[] device = deviceVersion.split("\\.");
		try {
			for(int i = 0; i < device.length; i++) {
				int retInt = Integer.parseInt(retrieved[i]);
				int deviceInt = Integer.parseInt(device[i]);
				if(retInt > deviceInt) {
					return true;
        		}
        	}
        } catch(Exception e) {}
		return false;
	}
	
	/** 
	* Placeholder for an Empty Foo class
	*/
	
	private static class Foo extends Document {
			
	}
	

	/**
	 * If the output of checkAndSetFirmware() is true, then Firmware Download is initiated
	 * by passing the Cloudant Document ID of the latest firmware to downloadFromCloudant().
	 * The method reads the contents of the Cloudant Document and writes it to a file on the
	 * local file system. If it encounters issues while writing to a file, then, it raises
	 * exception.
	*/
	
	public void downloadFromCloudant() {
					
		try{
			Foo foo = firmwareDB.find(Foo.class, docId, new Params().attachments());
			String attachmentData = foo.getAttachments().get(getLatestFirmware()).getData();
			String bytes = attachmentData;
			byte[] buffer = Base64.decodeBase64(bytes);
			FileOutputStream outputStream = new FileOutputStream(getLatestFirmware());
			outputStream.write(buffer);
			outputStream.close();       
			System.out.println("Completed Restoration of Cloudant Document ID " +docId + " into the Debian package " +getLatestFirmware());
										
		} catch(FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(IOException ex) {
			System.out.println("Error writing to the Debian package" +getLatestFirmware());
		}
	}

	
	/**
	 * Method that helps trim the output of the Device Properties File
	 * @param value
	 * @return
	 */
	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}

	@Override
	public void downloadFirmware(DeviceFirmware deviceFirmware) {
		
	}
	
	/**
	 * Successful completion of downloadFromCloudant() shall trigger the updateFirmware()
	 * Here, the method is designed to handle three different scenarios:
	 * 		1. Successfully Upgrade to the latest Firmware version
	 * 		2. If the Upgrade fails, then, tries to restore the current firmware version
	 * 		3. If the restoration to current firmware fails, then Factory-Reset is initiated
	 */
	@Override
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		boolean status = updateTask.updateFirmware(getLatestFirmware());
		
		System.out.println("value of status is " +status);
		
		/**
		 * Trigger the upgrade to the latest firmware
		 */
		if ( status == true ){
			System.out.println("Successfully Upgraded Latest Firmware");
			setCurrentFirmware(getLatestFirmware());
			this.currentFirmwareVersion = this.latestFirmwareVersion;
		} else {
			System.out.println("Upgrade failed. Reverting back to the current version");
			status = updateTask.updateFirmware(getCurrentFirmware());
			if (status == true) {
				System.out.println("Retained Current Firmware as is ");
			} else {
				updateTask.updateFirmware(Handler.FACTORY_FIRMWARE_NAME);
				this.currentFirmwareVersion = Handler.FACTORY_FIRMWARE_VERSION;
				System.out.println("Restored Factory Firmware version after failing to revert back to Current version");
				setCurrentFirmware(Handler.FACTORY_FIRMWARE_NAME);
			} 
		}
	}

	/**
	 * The updateWatsonIoT() method, post completion of Firmware Upgrade, updates the 
	 * Watson IoT Platform with the Firmware version details
	 */
	private void updateWatsonIoT() {
		DeviceInfo deviceInfo = dmClient.getDeviceData().getDeviceInfo();
		System.out.println("Updating the Firmware Version to the current version "+currentFirmwareVersion);
		deviceInfo.setFwVersion(currentFirmwareVersion);
		try {
			dmClient.sendManageRequest(0, true, true);
		} catch (MqttException e) {
			System.err.println("Failed to update the new Firmware version to the Watson IoT Platform");
			e.printStackTrace();
		}	
	}

}	
/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.device;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

/**
 *  A sample device that listens for backup & restore command from an Application and
 *  does the following action.
 *  
 *  1. On receiving the backup command, the device sends the contents of sample.config file
 *     to the application as an event.
 *  
 *  2. On receiving the restore command, the device replaces the contents of the sample.config file
 *     with the new contents.
 */
public class BackupAndRestoreDeviceSample {

	private final static String PROPERTIES_FILE_NAME = "/device.properties";
	
	// Sample config file name - present in the sample location as of device.prop
	private final static String SAMPLE_CONFIG_FILE_NAME = "/sample.config";
	
	private static String BACKUP_EVENT_NAME = "backup-event";
	private static String RESTORE_EVENT_ACK_NAME = "restore-ack";
	private static String BACKUP_COMMAND_NAME = "backup-command";
	private static String RESTORE_COMMAND_NAME = "restore-command";
	
	protected final static JsonParser JSON_PARSER = new JsonParser();
	
	private DeviceClient myDeviceClient = null;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting the backup & restore Device sample...");
		
		BackupAndRestoreDeviceSample sample = new BackupAndRestoreDeviceSample();
		
		try {
			sample.createDeviceClient(PROPERTIES_FILE_NAME);
			sample.setCommandCallback(SAMPLE_CONFIG_FILE_NAME);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} 
	}
	
	/**
	 * Set the command callback function where we receive the commands
	 * @param configFileName 
	 */
	private void setCommandCallback(String configFile) {
		/**
		 * Set the command callback function where we receive the commands
		 */
		MyCommandCallback cmd = new MyCommandCallback(this.myDeviceClient, configFile);
		myDeviceClient.setCommandCallback(cmd);
		Thread t = new Thread(cmd);
		t.start();
	}
	
	/**
	 * Create the Device client instance and connect to IBM Watson IoT Platform
	 * @param fileName
	 */
	private void createDeviceClient(String fileName) {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(BackupAndRestoreDeviceSample.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		try {
			//Instantiate the class by passing the properties file
			myDeviceClient = new DeviceClient(props);
			// Connect to IBM Watson IoT Platform
			myDeviceClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * A Command call back class that handles the backup and restore commands
	 */
	private static class MyCommandCallback implements CommandCallback, Runnable {

		private String configFileName;
		private DeviceClient deviceClient;
		
		// A queue to hold & process the commands
		private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();

		public MyCommandCallback(DeviceClient deviceClient, String configFileName) {
			/**
			 * Load properties file "device.prop"
			 */
			this.configFileName = configFileName;
			this.deviceClient = deviceClient;
		}

		@Override
		public void processCommand(Command cmd) {
			System.out.println("Got command " + cmd.getCommand());
			try {
				queue.put(cmd);
			} catch (InterruptedException e) {
			}			
		}

		@Override
		public void run() {
			while(true) {
				Command cmd = null;
				try {
					cmd = queue.take();
				} catch (InterruptedException e1) {
					// Ignore the Interuppted exception, retry
					continue;
				}
				
				if(cmd.getCommand().equals(BACKUP_COMMAND_NAME)) {
					processBackupCommand();
				
				} else if(cmd.getCommand().equals(RESTORE_COMMAND_NAME)) {
					processRestoreCommand(cmd);
				}
			}
		}
		
		private void processBackupCommand() {
			/**
			  * Load device properties
			  */
			Properties props = new Properties();
			try {
				props.load(MyCommandCallback.class.getResourceAsStream(configFileName));
			} catch (IOException e1) {
				System.err.println("Not able to read the properties file, exiting..");
				System.exit(-1);
			}
			
			JsonObject event = new JsonObject();
			Set<Entry<Object,Object>> entries = props.entrySet();
			Iterator<Entry<Object,Object>> itr = entries.iterator();
			while(itr.hasNext()) {
				Entry<Object, Object> entry = itr.next();
				event.addProperty((String)entry.getKey(), (String)entry.getValue());
			}
			this.deviceClient.publishEvent(BACKUP_EVENT_NAME, event, 2);
			System.out.println("Sent the backup event containing the configuration to application ");
		}
		
		private void processRestoreCommand(Command cmd) {
			JsonObject content = null;
			JsonObject payloadJson = JSON_PARSER.parse(cmd.getPayload()).getAsJsonObject();
			if (payloadJson.has("d")) {
				content = payloadJson.get("d").getAsJsonObject();
				try {
					URI uri = MyCommandCallback.class.getResource(configFileName).toURI();
					File file = new File(uri);
					FileOutputStream out = new FileOutputStream(file);
					Properties props = new Properties();
					Set<Entry<String, JsonElement>> entries = content.entrySet();
					Iterator<Entry<String, JsonElement>> itr = entries.iterator();
					while(itr.hasNext()) {
						Entry<String, JsonElement> entry = itr.next();
						props.setProperty(entry.getKey(), entry.getValue().getAsString());
					}
					
					props.store(out, null);
					out.close();
				} catch(Exception e) {
					System.out.println("Exception while restoring the config file");
					e.printStackTrace();
				
					// Send a negative restore ack
					JsonObject ack = new JsonObject();
					ack.addProperty("status", "false");
					this.deviceClient.publishEvent(RESTORE_EVENT_ACK_NAME, ack, 2);
					return;
				}
				
				System.out.println("Restored the configuration file "+this.configFileName +" successfully !!");
				// Send a positive restore ack
				JsonObject ack = new JsonObject();
				ack.addProperty("status", "true");
				this.deviceClient.publishEvent(RESTORE_EVENT_ACK_NAME, ack, 2);
			}
		}
	}
}

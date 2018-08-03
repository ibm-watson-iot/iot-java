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

package com.ibm.iotf.sample.client.application;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;

/**
 *  A sample application that does backup & restore of the device configuration based on the
 *  user input.
 *  
 *  1. On receiving the backup input from the user, the application sends a backup command to a device
 *     specified, and listens for the backup event from the device.
 *  
 *  2. On receiving the backup event from the device, the application stores the event in Cloudant NoSQL database.
 *  
 *  3. On receiving the restore input from the user, the application retrieves the corresponding config record
 *     from cloudant DB and then sends a restore command along with the config file content to the device.
 *  
 *  4. Application then listens for the response from the device for the restore command. It displays the results
 *     on the console. 
 */ 
public class BackupAndRestoreApplicationSample {

	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private static String BACKUP_EVENT_NAME = "backup-event";
	private static String RESTORE_EVENT_ACK_NAME = "restore-ack";
	private static String BACKUP_COMMAND_NAME = "backup-command";
	private static String RESTORE_COMMAND_NAME = "restore-command";
	
	protected final static JsonParser JSON_PARSER = new JsonParser();

	private ApplicationClient myAppClient = null;
	private String deviceType;
	private String deviceId;
	
	private Database backupAndRestoreDB;
	
	public static void main(String[] args) throws Exception {
		
		if(args.length < 2) {
			System.err.println("Please run the application with Cloudant DB username & Password as follows\n");
			System.out.println("BackupAndRestoreApplicationSample <username> <password>");
			System.exit(-1);
		}
		
		String username = args[0];
		String password = args[1];
		System.out.println("Starting the backup & restore application sample...");
		
		BackupAndRestoreApplicationSample sample = new BackupAndRestoreApplicationSample();
		
		try {
			
			sample.createCloudantNoSQLDB(username, password);
			sample.createApplicationClient(PROPERTIES_FILE_NAME);
			sample.subscribeToEvents();
			sample.userAction();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} 
	}
	
	/**
	 * Connect to Cloundant NoSQL DB based on the user inputs and 
	 * create the config DB if its not created already
	 * 
	 */
	private void createCloudantNoSQLDB(String username, String password) {

		StringBuilder sb = new StringBuilder();
		sb.append("https://")
		  .append(username)
		  .append(":")
		  .append(password)
		  .append("@")
		  .append(username)
		  .append(".cloudant.com");
		
        CloudantClient client = new CloudantClient(sb.toString(), username, password);

		
		System.out.println("Connected to Cloudant");
		System.out.println("Server Version: " + client.serverVersion());

		backupAndRestoreDB = client.database("config", true);
	}

	/**
	 * Subscribe to all events from a particular device type and ID
	 */
	private void subscribeToEvents() {
		myAppClient.setEventCallback(new MyEventCallback(this.backupAndRestoreDB));
		myAppClient.subscribeToDeviceEvents(this.deviceType, this.deviceId);
	}
	
	/**
	 * Create the Application client instance and connect to IBM Watson IoT Platform
	 * @param fileName
	 */
	private void createApplicationClient(String fileName) {
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(BackupAndRestoreApplicationSample.class.getResourceAsStream(fileName));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		deviceType = trimedValue(props.getProperty("Device-Type"));
		deviceId = trimedValue(props.getProperty("Device-ID"));
		
		try {
			//Instantiate the class by passing the properties file
			myAppClient = new ApplicationClient(props);
			// Connect to IBM Watson IoT Platform
			myAppClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * 
	 * The configuration class that will be stored in Cloudant DB
	 */
	private static class Config {
		private String deviceType;
		private String deviceId;
		private JsonObject data;
		
		public Config(String deviceType, String deviceId, JsonObject data) {
			super();
			this.deviceType = deviceType;
			this.deviceId = deviceId;
			this.data = data;
		}

		public JsonObject getData() {
			return data;
		}
		
	}

	/**
	 * An event call back class that handles the backup event and restore acknowledgement
	 */
	private static class MyEventCallback implements EventCallback {
		
		private Database backupAndRestoreDB;
		
		MyEventCallback(Database backupAndRestoreDB) {
			this.backupAndRestoreDB = backupAndRestoreDB;
		}
		/**
		 * Processes the backup & restore ack event.
		 * 
		 * On receiving the backup event, it parses the event and stores the content into the cloundant DB.
		 * Also, returns the id of the document where the contents of the configuration file is stored.
		 * 
		 * On receiving the restore-ack event, it parses and displays the status in the console.
		 */
		public void processEvent(Event e) {
			if(e.getEvent().equalsIgnoreCase(BACKUP_EVENT_NAME)) {
				String payload = e.getPayload();
				JsonObject content;
				JsonObject payloadJson = JSON_PARSER.parse(payload).getAsJsonObject();
				if (payloadJson.has("d")) {
					content = payloadJson.get("d").getAsJsonObject();
				} else {
					content = payloadJson;
				}
				Config cfg = new Config(e.getDeviceType(), e.getDeviceId(), content);
				Response response = backupAndRestoreDB.save(cfg);
				System.out.println("Stored the event in Cloudant NoSQL DB and the id for the same is " + response.getId());
			} else if(e.getEvent().equalsIgnoreCase(RESTORE_EVENT_ACK_NAME)) {
				JsonObject payloadJson = JSON_PARSER.parse(e.getPayload()).getAsJsonObject();
				JsonObject content = payloadJson.get("d").getAsJsonObject();
				boolean status = content.get("status").getAsBoolean();
				System.out.println("Got the restore ack event from device " + e.getDeviceId() +", status = "+status);
			}
			printOptions();
		}

		public void processCommand(Command cmd) {
		
		}
	}
	
	/**
	 * Send a backup command to the device. The body is just empty.
	 */
	private void sendBackupCommand() {
		this.myAppClient.publishCommand(this.deviceType, deviceId, BACKUP_COMMAND_NAME, new JsonObject(), 2);
		System.out.println("Sent the backup command to device "+deviceId);
	}
	
	/**
	 * Send a restore command along with the contents of the configuration file to the device.
	 */
	private void sendRestoreCommand(String id) {
		Config cfg = backupAndRestoreDB.find(Config.class, id);
		this.myAppClient.publishCommand(this.deviceType, deviceId, RESTORE_COMMAND_NAME, cfg.getData(), 2);
		System.out.println("Sent the Restore command to device "+deviceId +" and waiting for acknowledgement");
	}

	private void userAction() {
    	Scanner in = new Scanner(System.in);
    	printOptions();
    	while(true) {
    		try {
	            String input = in.nextLine();
	            
	            String[] parameters = input.split(" ");
	            
	            switch(parameters[0]) {
	            
	            	case "backup":
	            		this.sendBackupCommand();
	            		break;
	            		
	            	case "restore":
	            		if(parameters.length == 1) {
	            			System.out.println("Please specify the id of the document to restore !!");
	            			break;
	            		}
	            		this.sendRestoreCommand(parameters[1]);
	            		break;
	            		
	            	case "quit":
	            		this.myAppClient.disconnect();
	            		System.exit(-1);
	            		break;
	
	            	default:
	            		System.out.println("Unknown command received :: "+input);
	            		printOptions();
	            		
	            }
    		} catch(Exception e) {
    			System.out.println("Operation failed with exception "+e.getMessage());
    			printOptions();
    			continue;
    		}
    	}
    }
	
	private static void printOptions() {
		System.out.println("backup     :: Sends a backup command to the list of device(s)");
		System.out.println("restore id :: Sends a restore command along with the config file. "
				+ "Specify the ID that you received during the backup");
		System.out.println("quit       :: quit this application");
		System.out.println("Enter the command ");
	}

	private static String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}	
}

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
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.sample.client.application;

import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.sample.util.Utility;

/**
 * This sample shows how one application can publish commands to the device
 *
 */
public class RegisteredApplicationCommandPublish {

	private final static String PROPERTIES_FILE_NAME = "application.prop";
	private final static String DEFAULT_PATH = "samples/iotfdeviceclient/src";

	public static void main(String[] args) throws Exception {
		String fileName = null;
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = Utility.getDefaultFilePath(PROPERTIES_FILE_NAME, DEFAULT_PATH);
		}

		/**
		 * Load properties file "application.prop"
		 */
		Properties props = Utility.loadPropertiesFile(PROPERTIES_FILE_NAME, fileName);
		ApplicationClient myAppClient = null;
		try {
			//Instantiate the class by passing the properties file
			myAppClient = new ApplicationClient(props);
			myAppClient.connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/**
		 * Get the Device Type and Device Id to which the application will publish the command
		 */
		String deviceType = Utility.trimedValue(props.getProperty("Device-Type"));
		String deviceId = Utility.trimedValue(props.getProperty("Device-ID"));
		
		JsonObject data = new JsonObject();
		data.addProperty("name", "stop-rotation");
		data.addProperty("delay",  0);
		
		//Registered flow allows 0, 1 and 2 QoS
		myAppClient.publishCommand(deviceType, deviceId, "stop", data);
		myAppClient.disconnect();
	}
}

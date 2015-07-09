/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

/**
 * This sample shows how we can write a device client which subscribes to commands, in a Registered mode <br>
 * It uses the Java Client Library for IBM IoT Foundation
 * This sample code should be executed in a JRE running on the device
 * 
 */

package com.ibm.iotf.sample.client.device;

import java.util.Properties;


import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;


//Implement the CommandCallback class to provide the way in which you want the command to be handled
class MyNewCommandCallback implements CommandCallback{
	
	public MyNewCommandCallback() {
	}

	//In this sample, we are just displaying the command the moment the device recieves it
	@Override
	public void processCommand(Command command) {
		System.out.println("COMMAND RECEIVED...." + command.getPayload());
		
	}
}

public class RegisteredDeviceCommandSubscribe {

	
	public static void main(String[] args) {
		
		//Provide the device specific data, as well as Auth-key and token using Properties class		
		Properties options = new Properties();
		
		options.setProperty("org", "uguhsp");
		options.setProperty("type", "iotsample-arduino");
		options.setProperty("id", "00aabbccde03");
		options.setProperty("auth-method", "token");
		options.setProperty("auth-token", "AUTH TOKEN OF THE DEVICE");
		
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file			
			myClient = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Pass the above implemented CommandCallback as an argument to this device client
		myClient.setCommandCallback(new MyNewCommandCallback());

		//Connect to the IBM IoT Foundation	
		myClient.connect();
	}
}

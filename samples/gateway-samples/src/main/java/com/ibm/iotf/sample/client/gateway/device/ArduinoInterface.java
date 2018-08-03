/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.sample.client.gateway.device;

import com.ibm.iotf.client.gateway.GatewayClient;

/**
 *  <p> Initializes the Arduino device interface, if it finds that the hardware
 *  Arduino Uno device is available, then creates the ArduinoSerialInterface instance
 *  that interacts with Arduino Uno device through RxTx library, otherwise Swicthes
 *  to Simulator - ArduinoSimulatorInterface.</p>
 *      
 */
public class ArduinoInterface {
	
	public static DeviceInterface createDevice(String deviceId,
			String deviceType, String port, GatewayClient gwClient) {
		/**
		 * Let us create the ArduinoSerialInterface first, that tries to initialize with the hardware.
		 * If there is a failure, then switch back to the software version, i.e ArduinoSimulatorInterface
		 */
		ArduinoSerialInterface hardware = new ArduinoSerialInterface(deviceId, deviceType, port, gwClient);
		boolean status = hardware.initialize();
		if(status == false) {
			System.out.println("switching to Simulator mode as hardware is not found !!");
			ArduinoSimulatorInterface simulator = 
					new ArduinoSimulatorInterface(deviceId, deviceType, port, gwClient);
			simulator.initialize();
			return simulator;
		} else {
			return hardware;
		}
	}

	
	
}
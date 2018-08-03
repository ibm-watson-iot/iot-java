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
package com.ibm.iotf.sample.devicemgmt.device.task;

import java.util.Random;
import java.util.TimerTask;

import com.ibm.iotf.devicemgmt.DeviceLocation;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;


/**
 * 
 * Timer task that updates the device location to Watson IoT Platform 
 */
public class LocationUpdateTask extends TimerTask {
	
	private Random random = new Random();
	private ManagedDevice dmClient;
	
	public LocationUpdateTask(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
	
	@Override
	public void run() {
		// ...update location
		double latitude = random.nextDouble() + 30;
		double longitude = random.nextDouble() - 98;
		double elevation = (double)random.nextInt(100);
		int rc = dmClient.updateLocation(latitude, longitude, elevation);
		if(rc == 200) {
			System.out.println("Updating random location (" + latitude + " " + longitude +" " + elevation + ")");
		} else {
			System.err.println("Failed to update the location (" + 
							latitude + " " + longitude +" " + elevation + "), rc ="+rc);
		}
	}
	
}

/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
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

import com.ibm.iotf.devicemgmt.device.DeviceLocation;

/**
 * 
 * Timer task that updates the device location to IoT Foundation 
 */
public class LocationUpdateTask extends TimerTask {
	
	private DeviceLocation deviceLocation;
	private Random random = new Random();
	
	public LocationUpdateTask(DeviceLocation location) {
		this.deviceLocation = location;
	}
	
	@Override
	public void run() {
		// ...update location
		int rc = deviceLocation.update(random.nextDouble() + 30,   // latitude
							  random.nextDouble() - 98,	  // longitude
							  (double)random.nextInt(100));		  // elevation
		if(rc == 200) {
			System.out.println("Current location (" + deviceLocation.toString() + ")");
		} else {
			System.err.println("Failed to update the location");
		}
	}
	
}

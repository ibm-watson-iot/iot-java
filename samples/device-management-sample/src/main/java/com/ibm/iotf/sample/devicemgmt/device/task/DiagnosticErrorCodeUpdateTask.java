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

import com.ibm.iotf.devicemgmt.device.ManagedDevice;



/**
 * Timer task that appends/clears Error code to Watson IoT Platform
 * 
 *  Clears the error at every 25th iteration
 */
public class DiagnosticErrorCodeUpdateTask extends TimerTask {
		
	private ManagedDevice dmClient;
	private Random random = new Random();
	private int count = 0;
		
	public DiagnosticErrorCodeUpdateTask(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
		
	@Override
	public void run() {
		int errorCode = random.nextInt(500);
		int rc = dmClient.addErrorCode(errorCode);
		if(rc == 200) {
			System.out.println("Current Errorcode (" + errorCode + ")");
		} else {
			System.out.println("Errorcode addition failed!, rc = "+rc);
		}
			
		if(count++ == 25) {
			rc = dmClient.clearErrorCodes();
			if(rc == 200) {
				System.out.println("ErrorCodes are cleared successfully!");
			} else {
				System.out.println("Failed to clear the ErrorCodes! rc = "+rc);
			}
				
			this.count = 0;
		}
	}
		
}
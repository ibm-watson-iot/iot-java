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

import com.ibm.iotf.devicemgmt.device.DiagnosticErrorCode;

/**
 * Timer task that appends/clears Error code to IoT Foundation
 * 
 *  Clears the error at every 25th iteration
 */
public class DiagnosticErrorCodeUpdateTask extends TimerTask {
		
	private DiagnosticErrorCode diagErr;
	private Random random = new Random();
	private int count = 0;
		
	public DiagnosticErrorCodeUpdateTask(DiagnosticErrorCode diagErr) {
		this.diagErr = diagErr;
	}
		
	@Override
	public void run() {
		
		int rc = diagErr.append(random.nextInt(500));
		if(rc == 200) {
			System.out.println("Current Errorcode (" + diagErr + ")");
		} else {
			System.out.println("Errorcode addition failed!");
		}
			
		if(count++ == 25) {
			rc = diagErr.clear();
			if(rc == 200) {
				System.out.println("ErrorCodes are cleared successfully!");
			} else {
				System.out.println("Failed to clear the ErrorCodes!");
			}
				
			this.count = 0;
		}
	}
		
}
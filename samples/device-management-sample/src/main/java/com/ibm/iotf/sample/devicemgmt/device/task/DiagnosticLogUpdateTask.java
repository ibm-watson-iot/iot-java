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

import java.util.Date;
import java.util.TimerTask;

import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;


/**
 * Timer task that appends/clears Log information to Watson IoT Platform
 * 
 *  Clears the error at every 25th iteration
 */
public class DiagnosticLogUpdateTask extends TimerTask {
	
	ManagedDevice dmClient;
	private int count = 0;
	
	public DiagnosticLogUpdateTask(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
	
	@Override
	public void run() {
		String message = "Log event " + count++;
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.informational;
		int rc = dmClient.addLog(message, timestamp, severity);
			
		if(rc == 200) {
			System.out.println("Current Log (" + message + " " + timestamp + " " + severity + ")");
		} else {
			System.out.println("Log Addition failed!, rc = "+rc);
		}
			
		if(count == 25) {
			rc = this.dmClient.clearLogs();
			if(rc == 200) {
				System.out.println("Logs are cleared successfully");
			} else {
				System.out.println("Failed to clear the Logs, rc = "+rc);
			}	
		}
	}
}

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

import java.util.Date;
import java.util.TimerTask;

import com.ibm.iotf.devicemgmt.device.DiagnosticLog;

/**
 * Timer task that appends/clears Log information to IoT Foundation
 * 
 *  Clears the error at every 25th iteration
 */
public class DiagnosticLogUpdateTask extends TimerTask {
	
	private DiagnosticLog diagLog;
	private int count = 0;
	
	public DiagnosticLogUpdateTask(DiagnosticLog diagLog) {
		this.diagLog = diagLog;
	}
	
	@Override
	public void run() {
		int rc = diagLog.append("Log event " + count++, new Date(), 
				DiagnosticLog.LogSeverity.informational);
			
		if(rc == 200) {
			System.out.println("Current Log (" + diagLog + ")");
		} else {
			System.out.println("Log Addition failed");
		}
			
		if(count == 25) {
			rc = diagLog.clear();
			if(rc == 200) {
				System.out.println("Logs are cleared successfully");
			} else {
				System.out.println("Failed to clear the Logs");
			}	
		}
	}
}

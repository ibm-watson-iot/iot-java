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
package com.ibm.iotf.sample.devicemgmt.gateway.task;

import java.util.Date;
import java.util.TimerTask;

import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;

/**
 * Timer task that appends/clears random Log information to IoT Foundation and clears the error at every 25th iteration
 * 
 * If you create the task instance without the deviceType and Id, then its considered for Gateway.
 */
public class DiagnosticLogUpdateTask extends TimerTask {
	
	ManagedGateway dmClient;
	private int count = 0;
	
	private String deviceType;
	private String deviceId;
	
	public DiagnosticLogUpdateTask(ManagedGateway managedGateway, String deviceType, String deviceId) {
		this.dmClient = managedGateway;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
	}
	
	public DiagnosticLogUpdateTask(ManagedGateway dmClient) {
		this.dmClient = dmClient;
	}
	
	@Override
	public void run() {
		if(deviceId == null) {
			updateGateway();
		} else {
			updateDevice();
		}
		
	}
	
	private void updateGateway() {
		String message = "Log event " + count++;
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.informational;
		int rc = dmClient.addGatewayLog(message, timestamp, severity);
			
		if(rc == 200) {
			System.out.println("Current Gateway Log (" + message + " " + timestamp + " " + severity + ")");
		} else {
			System.out.println("Gateway Log Addition failed!, rc = "+rc);
		}
			
		if(count == 25) {
			rc = this.dmClient.clearGatewayLogs();
			if(rc == 200) {
				System.out.println("Gateway Logs are cleared successfully");
			} else {
				System.out.println("Failed to clear the Gateway Logs, rc = "+rc);
			}	
		}
	}
	
	private void updateDevice() {
		String message = "Log event " + count++;
		Date timestamp = new Date();
		LogSeverity severity = LogSeverity.warning;
		int rc = dmClient.addDeviceLog(deviceType, deviceId, message, timestamp, severity);
			
		if(rc == 200) {
			System.out.println("Current device("+deviceId+") Log (" + message + " " + timestamp + " " + severity + ")");
		} else {
			System.out.println("Device("+deviceId+") Log Addition failed!, rc = "+rc);
		}
			
		if(count == 25) {
			rc = this.dmClient.clearDeviceLogs(deviceType, deviceId);
			if(rc == 200) {
				System.out.println("Device("+deviceId+") Logs are cleared successfully");
			} else {
				System.out.println("Failed to clear the device("+deviceId+") Logs, rc = "+rc);
			}	
		}
	}
}

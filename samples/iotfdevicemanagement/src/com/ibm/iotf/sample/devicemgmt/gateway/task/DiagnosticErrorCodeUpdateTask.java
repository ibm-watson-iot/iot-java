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

import java.util.Random;
import java.util.TimerTask;

import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;

/**
 * Timer task that appends/clears random Error code to IoT Foundation and clears the error at every 25th iteration
 * 
 * if you create the task instance without the deviceType and Id, then its considered 
 * for Gateway.
 */
public class DiagnosticErrorCodeUpdateTask extends TimerTask {
		
	private ManagedGateway dmClient;
	private Random random = new Random();
	private int count = 0;
	
	private String deviceType;
	private String deviceId;
	
	public DiagnosticErrorCodeUpdateTask(ManagedGateway managedGateway, String deviceType, String deviceId) {
		this.dmClient = managedGateway;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
	}
		
	public DiagnosticErrorCodeUpdateTask(ManagedGateway dmClient) {
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
		int errorCode = random.nextInt(500);
		int rc = dmClient.addGatewayErrorCode(errorCode);
		if(rc == 200) {
			System.out.println("Current Gateway Errorcode (" + errorCode + ")");
		} else {
			System.out.println("Errorcode addition failed for Gateway!, rc = "+rc);
		}
			
		if(count++ == 25) {
			rc = dmClient.clearGatewayErrorCodes();
			if(rc == 200) {
				System.out.println("ErrorCodes are cleared successfully for Gateway!");
			} else {
				System.out.println("Failed to clear the Gateway ErrorCodes! rc = "+rc);
			}
				
			this.count = 0;
		}
	}
	
	private void updateDevice() {
		int errorCode = random.nextInt(500);
		int rc = dmClient.addDeviceErrorCode(deviceType, deviceId, errorCode);
		if(rc == 200) {
			System.out.println("Current device(" +deviceId + ") Errorcode (" + errorCode + ")");
		} else {
			System.out.println("Errorcode addition failed for device " +deviceId +", reason = "+rc);
		}
			
		if(count++ == 25) {
			rc = dmClient.clearDeviceErrorCodes(deviceType, deviceId);
			if(rc == 200) {
				System.out.println("ErrorCodes are cleared successfully! for device "+deviceId);
			} else {
				System.out.println("Failed to clear the device ErrorCodes! rc = "+rc);
			}
				
			this.count = 0;
		}
	}
		
}
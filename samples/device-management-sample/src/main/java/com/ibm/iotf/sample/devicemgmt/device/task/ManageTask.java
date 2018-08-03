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

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.devicemgmt.device.ManagedDevice;



/**
 * 
 * Timer task that sends the manage command before the lifetime
 * expires, otherwise the device will become dormant and can't 
 * participate in device management actions
 *
 */
public class ManageTask implements Runnable {
	private int lifetime;
	private ManagedDevice managedDevice;
	
	public ManageTask(ManagedDevice managedDevice, int lifetime) {
		this.lifetime = lifetime;
		this.managedDevice = managedDevice;
	}
	
	@Override
	public void run() {
		try {
			boolean status = managedDevice.sendManageRequest(lifetime, true, true);
			System.out.println("Resent the manage request at time "+new Date() +
					" status("+status+")");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
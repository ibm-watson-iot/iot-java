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
package com.ibm.iotf.devicemgmt.device;

/**
 * List of device topics where device sends the device management 
 * requests to the server
 * 
 */
public enum DeviceTopic {
	MANAGE("iotdevice-1/mgmt/manage"),
	UNMANAGE("iotdevice-1/mgmt/unmanage"),
	UPDATE_LOCATION("iotdevice-1/device/update/location"),
	CREATE_DIAG_ERRCODES("iotdevice-1/add/diag/errorCodes"),
	CLEAR_DIAG_ERRCODES("iotdevice-1/clear/diag/errorCodes"),
	ADD_DIAG_LOG("iotdevice-1/add/diag/log"),
	CLEAR_DIAG_LOG("iotdevice-1/clear/diag/log"),
	NOTIFY("iotdevice-1/notify"),
	RESPONSE("iotdevice-1/response");
		
	private DeviceTopic(String name) {
		this.name = name;
	}
		
	private final String name;
	
	public String getName() {
		return name;
	}
		
	public static DeviceTopic get(String topic) {
		switch(topic) {
		
			case "iotdevice-1/mgmt/manage": return DeviceTopic.MANAGE;
			case "iotdevice-1/mgmt/unmanage": return DeviceTopic.UNMANAGE;
			case "iotdevice-1/device/update/location": return UPDATE_LOCATION;
			case "iotdevice-1/add/diag/errorCodes": return CREATE_DIAG_ERRCODES;
			case "iotdevice-1/clear/diag/errorCodes": return CLEAR_DIAG_ERRCODES;
			case "iotdevice-1/add/diag/log": return ADD_DIAG_LOG;
			case "iotdevice-1/clear/diag/log": return CLEAR_DIAG_LOG;
			case "iotdevice-1/notify": return NOTIFY;
			case "iotdevice-1/response": return RESPONSE;
			
		}
		return null;
	}
}
	

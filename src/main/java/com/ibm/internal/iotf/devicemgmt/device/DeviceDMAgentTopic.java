/**
 *****************************************************************************
 Copyright (c) 2016-17 IBM Corporation and other Contributors.
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
package com.ibm.internal.iotf.devicemgmt.device;

import com.ibm.internal.iotf.devicemgmt.DMAgentTopic;

/**
 * List of device topics where device sends the device management 
 * requests to the server
 * 
 */
public class DeviceDMAgentTopic implements DMAgentTopic {
	
	private static DeviceDMAgentTopic instance = new DeviceDMAgentTopic();
	

	enum Topic {
		MANAGE("iotdevice-1/mgmt/manage"),
		UNMANAGE("iotdevice-1/mgmt/unmanage"),
		UPDATE_LOCATION("iotdevice-1/device/update/location"),
		CREATE_DIAG_ERRCODES("iotdevice-1/add/diag/errorCodes"),
		CLEAR_DIAG_ERRCODES("iotdevice-1/clear/diag/errorCodes"),
		ADD_DIAG_LOG("iotdevice-1/add/diag/log"),
		CLEAR_DIAG_LOG("iotdevice-1/clear/diag/log"),
		NOTIFY("iotdevice-1/notify"),
		RESPONSE("iotdevice-1/response");
		
		private Topic(String name) {
			this.name = name;
		}
			
		private final String name;
		
		public String getName() {
			return name;
		}
	}

	public static DMAgentTopic getInstance() {
		return instance;
	}

	@Override
	public String getManageTopic() {
		return Topic.MANAGE.getName();
	}

	@Override
	public String getUpdateLocationTopic() {
		return Topic.UPDATE_LOCATION.getName();
	}

	@Override
	public String getClearDiagErrorCodesTopic() {
		return Topic.CLEAR_DIAG_ERRCODES.getName();
	}

	@Override
	public String getClearDiagLogsTopic() {
		return Topic.CLEAR_DIAG_LOG.getName();
	}

	@Override
	public String getAddErrorCodesTopic() {
		return Topic.CREATE_DIAG_ERRCODES.getName();
	}

	@Override
	public String getAddDiagLogTopic() {
		return Topic.ADD_DIAG_LOG.getName();	}

	@Override
	public String getUnmanageTopic() {
		return Topic.UNMANAGE.getName();
	}

	@Override
	public String getDMServerTopic() {
		return Topic.RESPONSE.getName();
	}

	@Override
	public String getNotifyTopic() {
		return Topic.NOTIFY.getName();
	}
}
	

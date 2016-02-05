/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
  Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.gateway.internal;

import com.ibm.iotf.devicemgmt.internal.DMAgentTopic;

/**
 * List of device topics where device sends the device management 
 * requests to the server
 * 
 */
public class GatewayDMAgentTopic implements DMAgentTopic {
	
	private static String STARTING = "iotdevice-1";
	private static String TYPE = "type";
	private static String ID = "id";
	
	private String deviceType;
	private String deviceId;
	private String topicStarter;
	
	public GatewayDMAgentTopic(String deviceType, String deviceId) {
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.topicStarter = STARTING + '/' + TYPE + '/' + this.deviceType + '/' + ID + '/' + this.deviceId + '/';
	}
	

	enum Topic {
		MANAGE("mgmt/manage"),
		UNMANAGE("mgmt/unmanage"),
		UPDATE_LOCATION("device/update/location"),
		CREATE_DIAG_ERRCODES("add/diag/errorCodes"),
		CLEAR_DIAG_ERRCODES("clear/diag/errorCodes"),
		ADD_DIAG_LOG("add/diag/log"),
		CLEAR_DIAG_LOG("clear/diag/log"),
		NOTIFY("notify"),
		RESPONSE("response");
			
		private Topic(String name) {
			this.name = name;
		}
			
		private final String name;
		
		public String getName() {
			return name;
		}
			
		
	}

	@Override
	public String getManageTopic() {
		return topicStarter + Topic.MANAGE.getName();
	}

	@Override
	public String getUpdateLocationTopic() {
		return topicStarter + Topic.UPDATE_LOCATION.getName();
	}

	@Override
	public String getClearDiagErrorCodesTopic() {
		return topicStarter + Topic.CLEAR_DIAG_ERRCODES.getName();
	}

	@Override
	public String getClearDiagLogsTopic() {
		return topicStarter + Topic.CLEAR_DIAG_LOG.getName();
	}

	@Override
	public String getAddErrorCodesTopic() {
		return topicStarter + Topic.CREATE_DIAG_ERRCODES.getName();
	}

	@Override
	public String getAddDiagLogTopic() {
		return topicStarter + Topic.ADD_DIAG_LOG.getName();	}

	@Override
	public String getUnmanageTopic() {
		return topicStarter + Topic.UNMANAGE.getName();
	}

	@Override
	public String getDMServerTopic() {
		return topicStarter + Topic.RESPONSE.getName();
	}

	@Override
	public String getNotifyTopic() {
		return topicStarter + Topic.NOTIFY.getName();
	}

}
	

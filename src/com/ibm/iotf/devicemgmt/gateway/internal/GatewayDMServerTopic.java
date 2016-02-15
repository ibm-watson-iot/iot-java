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

import com.ibm.iotf.devicemgmt.internal.DMServerTopic;

/**
 * List of Service topics where the IBM Watson IoT Platform server
 * initiates a device management request or responds to client request
 */
public class GatewayDMServerTopic implements DMServerTopic {
	
	private static String STARTING = "iotdm-1";
	private static String TYPE = "type";
	private static String ID = "id";
	
	private String deviceType;
	private String deviceId;
	private String topicStarter;
	
	public GatewayDMServerTopic(String deviceType, String deviceId) {
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.topicStarter = STARTING + '/' + TYPE + '/' + this.deviceType + '/' + ID + '/' + this.deviceId + '/';
	}

	enum ServerTopic {
		RESPONSE("response"),
		OBSERVE("observe"),
		CANCEL("cancel"),
		INITIATE_REBOOT("mgmt/initiate/device/reboot"),
		INITIATE_FACTORY_RESET("mgmt/initiate/device/factory_reset"),
		INITIATE_FIRMWARE_DOWNLOAD("mgmt/initiate/firmware/download"),
		INITIATE_FIRMWARE_UPDATE("mgmt/initiate/firmware/update"),
		DEVICE_UPDATE("device/update");
		
		private ServerTopic(String name) {
			this.name = name;
		}
		
		private final String name;
		
		/**
		 * @return the name of the topic
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param topic name in string
		 * @return the ServerTopic for the given string
		 */
		public static ServerTopic get(String topic) {
			switch(topic) {
			case "response": return RESPONSE;
			
			case ("observe"): return OBSERVE;
			
			case "cancel": return CANCEL;
			
			case "mgmt/initiate/device/reboot": return INITIATE_REBOOT;
			
			case "mgmt/initiate/device/factory_reset": return INITIATE_FACTORY_RESET;
			
			case "mgmt/initiate/firmware/download": return INITIATE_FIRMWARE_DOWNLOAD;
			
			case "mgmt/initiate/firmware/update": return INITIATE_FIRMWARE_UPDATE;
			
			case "device/update": return DEVICE_UPDATE;
			
			}
			return null;
		}

	}
	
	@Override
	public String getDMServerTopic() {
		return topicStarter + ServerTopic.RESPONSE.getName();
	}

	@Override
	public String getDeviceUpdateTopic() {
		return topicStarter + ServerTopic.DEVICE_UPDATE.getName();
	}

	@Override
	public String getObserveTopic() {

		return topicStarter + ServerTopic.OBSERVE.getName();
	}

	@Override
	public String getCancelTopic() {

		return topicStarter + ServerTopic.CANCEL.getName();
	}

	@Override
	public String getInitiateRebootTopic() {

		return topicStarter + ServerTopic.INITIATE_REBOOT.getName();
	}

	@Override
	public String getInitiateFactoryReset() {

		return topicStarter + ServerTopic.INITIATE_FACTORY_RESET.getName();
	}

	@Override
	public String getInitiateFirmwareDownload() {

		return topicStarter + ServerTopic.INITIATE_FIRMWARE_DOWNLOAD.getName();
	}

	@Override
	public String getInitiateFirmwareUpdate() {

		return topicStarter + ServerTopic.INITIATE_FIRMWARE_UPDATE.getName();
	}
}


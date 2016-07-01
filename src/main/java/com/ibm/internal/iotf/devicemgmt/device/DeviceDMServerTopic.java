/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
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

import java.util.HashMap;
import java.util.Map;

import com.ibm.internal.iotf.devicemgmt.DMServerTopic;

/**
 * List of Service topics where the IBM Watson IoT Platform server
 * initiates a device management request or responds to client request
 */
public class DeviceDMServerTopic implements DMServerTopic {
	
	private static DeviceDMServerTopic instance = new DeviceDMServerTopic();
	private static final Map<String, Object> SERVER_TOPIC = new HashMap<>();
	static {
		SERVER_TOPIC.put("iotdm-1/response", ServerTopic.RESPONSE);
		SERVER_TOPIC.put("iotdm-1/observe", ServerTopic.OBSERVE);
		SERVER_TOPIC.put("iotdm-1/cancel", ServerTopic.CANCEL);
		SERVER_TOPIC.put("iotdm-1/mgmt/initiate/device/reboot", ServerTopic.INITIATE_REBOOT);
		SERVER_TOPIC.put("iotdm-1/mgmt/initiate/device/factory_reset", ServerTopic.INITIATE_FACTORY_RESET);
		SERVER_TOPIC.put("iotdm-1/mgmt/initiate/firmware/download", ServerTopic.INITIATE_FIRMWARE_DOWNLOAD);
		SERVER_TOPIC.put("iotdm-1/mgmt/initiate/firmware/update", ServerTopic.INITIATE_FIRMWARE_UPDATE);
		SERVER_TOPIC.put("iotdm-1/device/update", ServerTopic.DEVICE_UPDATE);
	}
	

	enum ServerTopic {
		RESPONSE("iotdm-1/response"),
		OBSERVE("iotdm-1/observe"),
		CANCEL("iotdm-1/cancel"),
		INITIATE_REBOOT("iotdm-1/mgmt/initiate/device/reboot"),
		INITIATE_FACTORY_RESET("iotdm-1/mgmt/initiate/device/factory_reset"),
		INITIATE_FIRMWARE_DOWNLOAD("iotdm-1/mgmt/initiate/firmware/download"),
		INITIATE_FIRMWARE_UPDATE("iotdm-1/mgmt/initiate/firmware/update"),
		DEVICE_UPDATE("iotdm-1/device/update");
		
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
			return (ServerTopic)SERVER_TOPIC.get(topic);
		}

	}
	
	public static DMServerTopic getInstance() {
		return instance;
	}

	@Override
	public String getDMServerTopic() {
		return ServerTopic.RESPONSE.getName();
	}

	@Override
	public String getDeviceUpdateTopic() {
		return ServerTopic.DEVICE_UPDATE.getName();
	}

	@Override
	public String getObserveTopic() {

		return ServerTopic.OBSERVE.getName();
	}

	@Override
	public String getCancelTopic() {

		return ServerTopic.CANCEL.getName();
	}

	@Override
	public String getInitiateRebootTopic() {

		return ServerTopic.INITIATE_REBOOT.getName();
	}

	@Override
	public String getInitiateFactoryReset() {

		return ServerTopic.INITIATE_FACTORY_RESET.getName();
	}

	@Override
	public String getInitiateFirmwareDownload() {

		return ServerTopic.INITIATE_FIRMWARE_DOWNLOAD.getName();
	}

	@Override
	public String getInitiateFirmwareUpdate() {

		return ServerTopic.INITIATE_FIRMWARE_UPDATE.getName();
	}
}

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
package com.ibm.iotf.devicemgmt.device.internal;

	/**
	 * List of Service topics where the IBM IoT Foundation server
	 * initiates a device management request or responds to client request
	 */
	public enum ServerTopic {
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
			switch(topic) {
			case "iotdm-1/response": return RESPONSE;
			
			case ("iotdm-1/observe"): return OBSERVE;
			
			case "iotdm-1/cancel": return CANCEL;
			
			case "iotdm-1/mgmt/initiate/device/reboot": return INITIATE_REBOOT;
			
			case "iotdm-1/mgmt/initiate/device/factory_reset": return INITIATE_FACTORY_RESET;
			
			case "iotdm-1/mgmt/initiate/firmware/download": return INITIATE_FIRMWARE_DOWNLOAD;
			
			case "iotdm-1/mgmt/initiate/firmware/update": return INITIATE_FIRMWARE_UPDATE;
			
			case "iotdm-1/device/update": return DEVICE_UPDATE;
			
			}
			return null;
		}

	}


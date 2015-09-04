/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Modified to include Resource Model
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.client.device;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.client.device.Resource.Resource;
import com.ibm.iotf.devicemgmt.client.device.Resource.StringResource;
import com.ibm.iotf.util.LoggerUtility;

/**
 * <code>DeviceData</code> 
 *
 */
public class DeviceData {
	private static String CLASS_NAME = DeviceData.class.getName();
	
	private String typeId = null;
	private String deviceId = null;
	private DeviceInfo deviceInfo = null;
	private DeviceLocation deviceLocation = null;
	private DeviceDiagnostic deviceDiag = null;
	private DeviceMgmt mgmt = null;
	private JsonObject metadata = null;
	
	private DeviceAction deviceAction = null;
	
	private DeviceFirmwareHandler fwHandler = null;
	private DeviceActionHandler actionHandler = null;
	
	private Resource root = new StringResource(Resource.ROOT_RESOURCE_NAME, "");
	
	public DeviceData(Builder builder) {
		this.typeId = builder.typeId;
		this.deviceId = builder.deviceId;
		this.deviceInfo = builder.deviceInfo;
		this.deviceLocation = builder.deviceLocation;
		this.deviceDiag = builder.deviceDiag;
		this.metadata = builder.metadata;

		if(builder.deviceFirmware != null) {
			this.mgmt = new DeviceMgmt(builder.deviceFirmware);
		}
		
		if(this.mgmt != null) {
			root.add(mgmt);
		}
		
		if(this.deviceLocation != null) {
			root.add(deviceLocation);
		}
		
		if(this.deviceInfo != null) {
			root.add(deviceInfo);
		}
		
		if(this.deviceDiag != null) {
			root.add(this.deviceDiag);
		}
	}
	
	public DeviceDiagnostic getDeviceDiag() {
		return deviceDiag;
	}


	public JsonObject getMetadata() {
		return metadata;
	}


	public String getTypeId() {
		return typeId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}
	
	public DeviceLocation getDeviceLocation() {
		return deviceLocation;
	}
	
	public DeviceDiagnostic getDeviceDiagnostic() {
		return deviceDiag;
	}
	
	public DeviceFirmware getDeviceFirmware() {
		if(mgmt != null) {
			return mgmt.getDeviceFirmware();
		}
		return null;
	}
	
	public void addDeviceActionHandler(DeviceActionHandler actionHandler) throws Exception {
		final String METHOD = "addDeviceActionHandler";

		if(this.actionHandler != null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Action Handler is already set, "
					+ "so can not add the new Action handler !");
			
			throw new Exception("Action Handler is already set, "
					+ "so can not add the new Action handler !");
		}
		
		actionHandler.setDeviceData(this);
		getDeviceAction().addPropertyChangeListener(actionHandler);
		actionHandler.start();
		this.actionHandler = actionHandler;
		
	}

	public DeviceAction getDeviceAction() {
		if(this.deviceAction == null) {
			this.deviceAction = new DeviceAction();
		}
		
		return deviceAction;
	}


	public void addFirmwareHandler(DeviceFirmwareHandler fwHandler) throws Exception {
		final String METHOD = "addFirmwareHandler";
		if(getDeviceFirmware() == null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Firmware Object is not set, "
					+ "so can not add firmware handler !");
			
			throw new Exception("Firmware Object is not set, so can not add firmware handler !");
		}
		
		if(this.fwHandler != null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Firmware Handler is already set, "
					+ "so can not add the new firmware handler !");
			
			throw new Exception("Firmware Handler is already set, "
					+ "so can not add the new firmware handler !");
		}
		
		fwHandler.setDeviceData(this);
		getDeviceFirmware().addPropertyChangeListener(fwHandler);
		fwHandler.start();
		this.fwHandler = fwHandler;
	}
	
	public static class Builder {
		private String typeId = null;
		private String deviceId = null;
		private DeviceInfo deviceInfo = null;
		private DeviceLocation deviceLocation = null;
		private DeviceDiagnostic deviceDiag = null;
		private DeviceFirmware deviceFirmware = null;
		private JsonObject metadata = null;
		
		public Builder() {}
		
		public Builder typeId(String typeId) {
			this.typeId = typeId;
			return this;
		}
		
		public Builder deviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}
		
		public Builder deviceInfo(DeviceInfo deviceInfo) {
			this.deviceInfo = deviceInfo;
			return this;
		}
		
		public Builder deviceLocation(DeviceLocation deviceLocation) {
			this.deviceLocation = deviceLocation;
			return this;
		}
		
		public Builder deviceDiag(DeviceDiagnostic deviceDiag) {
			this.deviceDiag = deviceDiag;
			return this;
		}
		
		public Builder deviceFirmware(DeviceFirmware deviceFirmware) {
			this.deviceFirmware = deviceFirmware;
			return this;
		}
		
		public Builder metadata(JsonObject metadata) {
			this.metadata = metadata;
			return this;
		}
		
		public DeviceData build() {
			return new DeviceData(this);
		}
		
		
	}

	void terminateHandlers() {
		if(this.fwHandler != null) {
			fwHandler.terminate();
			fwHandler = null;
		}
		
		if(this.actionHandler != null) {
			actionHandler.terminate();
			actionHandler = null;
		}
		
	}

	void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	
	void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}


	public void setMetadata(JsonObject value) {
		this.metadata = value;
	}

	public Resource getResource(String name) {
		String[] token = name.split("\\.");
		Resource resource = this.root;
		for(int i = 0; i < token.length; i++) {
			resource = resource.getChild(token[i]);
		}
		return resource;
	}

}

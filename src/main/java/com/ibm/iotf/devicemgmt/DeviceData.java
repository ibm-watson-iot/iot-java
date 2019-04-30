/**
 *****************************************************************************
 Copyright (c) 2015-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt;

import com.google.gson.JsonObject;
import com.ibm.internal.iotf.devicemgmt.ConcreteCustomAction;
import com.ibm.internal.iotf.devicemgmt.ConcreteDeviceAction;
import com.ibm.internal.iotf.devicemgmt.DeviceMgmt;
import com.ibm.iotf.client.CustomAction;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.devicemgmt.resource.StringResource;

/**
 * <p><code>DeviceData</code> defines the device model.</p>
 * 
 * <p>The device model describes the metadata and management characteristics of a device. 
 * The device database in the Watson IoT Platform is the master source of 
 * device information. Applications and managed devices are able to send updates to 
 * the database such as a location or the progress of a firmware update.
 * Once these updates are received by the Watson IoT Platform, 
 * the device database is updated, making the information available to applications.</p>
 */
public class DeviceData {
	private static String CLASS_NAME = DeviceData.class.getName();
	
	private String typeId = null;
	private String deviceId = null;
	private DeviceInfo deviceInfo = null;
	private DeviceLocation deviceLocation = null;
	private DeviceMgmt mgmt = null;
	private DeviceMetadata metadata = null;
	private DeviceAction deviceAction = null;
	private CustomAction customAction = null;
	
	private Resource root = new StringResource(Resource.ROOT_RESOURCE_NAME, "");
	
	private DeviceData(Builder builder) {
		this.typeId = builder.typeId;
		this.deviceId = builder.deviceId;
		this.deviceInfo = builder.deviceInfo;
		this.deviceLocation = new DeviceLocation();
		this.metadata = builder.metadata;

		DeviceFirmware firmware = builder.deviceFirmware;
		if(builder.deviceFirmware != null) {
			this.mgmt = new DeviceMgmt(builder.deviceFirmware);
		} else {
			firmware = new DeviceFirmware.Builder().build();
			this.mgmt = new DeviceMgmt(firmware);
		}
		
		firmware.setDeviceId(deviceId);
		firmware.setTypeId(typeId);
		
		root.add(mgmt);
		
		if(this.deviceLocation != null) {
			root.add(deviceLocation);
		}
		
		// deviceinfo will be required to update the firmware name 
		// and version 
		if(this.deviceInfo == null) {
			this.deviceInfo = new DeviceInfo.Builder().build();
		}
		
		root.add(deviceInfo);
		
		if(this.metadata != null) {
			root.add(this.metadata);
		}
	}
	
	/**
	 * Returns the Device type
	 * @return returns the typeID
	 */
	public String getTypeId() {
		return typeId;
	}
	
	/**
	 * Returns the Device ID
	 * @return returns the device ID
	 */
	public String getDeviceId() {
		return deviceId;
	}
	
	/**
	 * Returns the DeviceInfo object
	 * @return DeviceInfo returns the deviceinfo object
	 */
	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}
	
	/**
	 * Return the DeviceLocation object
	 * @return DeviceLocation returns location of the device
	 */
	public DeviceLocation getDeviceLocation() {
		return deviceLocation;
	}

	/**
	 * Returns the DeviceFirmware object
	 * @return  DeviceFirmware returns the device firmware object
	 */
	public DeviceFirmware getDeviceFirmware() {
		if(mgmt != null) {
			return mgmt.getDeviceFirmware();
		}
		return null;
	}
	
	/**
	 * Returns the device action object
	 * @return DeviceAction object or null
	 */
	public DeviceAction getDeviceAction() {
		if(this.deviceAction == null) {
			this.deviceAction = new ConcreteDeviceAction(this.typeId, this.deviceId);
		}
		
		return deviceAction;
	}
	
	/**
	 * Returns the custom action object
	 * @return CustomAction object or null
	 */
	public CustomAction getCustomAction() {
		if(this.customAction == null) {
			this.customAction = new ConcreteCustomAction(typeId, deviceId);
		}
		return customAction;
	}

	/**
	 * A builder class that helps to construct the DeviceData object. 
	 *
	 */
	public static class Builder {
		private String typeId = null;
		private String deviceId = null;
		private DeviceInfo deviceInfo = null;
		private DeviceFirmware deviceFirmware = null;
		private DeviceMetadata metadata = null;
		
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
		
		public Builder deviceFirmware(DeviceFirmware deviceFirmware) {
			this.deviceFirmware = deviceFirmware;
			return this;
		}
		
		public Builder metadata(DeviceMetadata metadata) {
			this.metadata = metadata;
			return this;
		}
		
		public DeviceData build() {
			return new DeviceData(this);
		}
		
		
	}

	/**
	 * Set the Device-Type
	 * 
	 * @param typeId Device Type to be set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
		if(this.mgmt != null && this.mgmt.getDeviceFirmware() != null) {
			mgmt.getDeviceFirmware().setTypeId(typeId);
		}
	}
	
	/**
	 * Set Device-ID
	 * 
	 * @param deviceId DeviceID to be set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		if(this.mgmt != null && this.mgmt.getDeviceFirmware() != null) {
			mgmt.getDeviceFirmware().setDeviceId(deviceId);
		}
	}

	/**
	 * Returns the Resource for the given name
	 * 
	 * @param name - the name of the resource to be returned
	 * @return Resource or null
	 */
	public Resource getResource(String name) {
		String[] token = name.split("\\.");
		Resource resource = this.root;
		for(int i = 0; i < token.length; i++) {
			resource = resource.getChild(token[i]);
		}
		return resource;
	}

	/**
	 * Returns the metadata
	 * @return the metadata
	 */
	public DeviceMetadata getMetadata() {
		return this.metadata;
	}

	public void setLocation(DeviceLocation location) {
		this.deviceLocation = location;
		root.add(location);
	}

}

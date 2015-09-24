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
package com.ibm.iotf.devicemgmt.device;

import com.ibm.iotf.devicemgmt.device.internal.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.device.internal.DeviceMgmt;
import com.ibm.iotf.devicemgmt.device.resource.Resource;
import com.ibm.iotf.devicemgmt.device.resource.StringResource;
import com.ibm.iotf.util.LoggerUtility;

/**
 * <p><code>DeviceData</code> defines the device model.</p>
 * 
 * <p>The device model describes the metadata and management characteristics of a device. 
 * The device database in the Internet of Things Foundation is the master source of 
 * device information. Applications and managed devices are able to send updates to 
 * the database such as a location or the progress of a firmware update.
 * Once these updates are received by the Internet of Things Foundation, 
 * the device database is updated, making the information available to applications.</p>
 */
public class DeviceData {
	private static String CLASS_NAME = DeviceData.class.getName();
	
	private String typeId = null;
	private String deviceId = null;
	private DeviceInfo deviceInfo = null;
	private DeviceLocation deviceLocation = null;
	private DeviceDiagnostic deviceDiag = null;
	private DeviceMgmt mgmt = null;
	private DeviceMetadata metadata = null;
	private DeviceAction deviceAction = null;
	
	private DeviceFirmwareHandler fwHandler = null;
	private DeviceActionHandler actionHandler = null;
	
	private Resource root = new StringResource(Resource.ROOT_RESOURCE_NAME, "");
	
	private DeviceData(Builder builder) {
		this.typeId = builder.typeId;
		this.deviceId = builder.deviceId;
		this.deviceInfo = builder.deviceInfo;
		this.deviceLocation = builder.deviceLocation;
		this.metadata = builder.metadata;

		if(builder.deviceFirmware != null) {
			this.mgmt = new DeviceMgmt(builder.deviceFirmware);
		}
		
		if(builder.diagErr != null || builder.diagLog != null) {
			this.deviceDiag = new DeviceDiagnostic(builder.diagErr, builder.diagLog);
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
		
		if(this.metadata != null) {
			root.add(this.metadata);
		}
	}
	
	/**
	 * Returns the Device type
	 */
	public String getTypeId() {
		return typeId;
	}
	
	/**
	 * Returns the Device ID
	 */
	public String getDeviceId() {
		return deviceId;
	}
	
	/**
	 * Returns the DeviceInfo object
	 */
	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}
	
	/**
	 * Return the DeviceLocation object
	 */
	public DeviceLocation getDeviceLocation() {
		return deviceLocation;
	}

	/**
	 * Returns the DeviceFirmware object 
	 */
	public DeviceFirmware getDeviceFirmware() {
		if(mgmt != null) {
			return mgmt.getDeviceFirmware();
		}
		return null;
	}
	/**
	 * <p>Adds a device action handler which is of type <code>DeviceActionHandler</code></p>
	 * 
	 * <p>If a device supports device actions like reboot and factory reset,
	 * the abstract class <code>DeviceActionHandler</code>
	 * should be extended by the device code. The <code>handleReboot</code> and <code>handleFactoryReset</code>
	 * must be implemented to handle the actions.</p>
	 *  
	 * @param actionHandler DeviceActionHandler that handles the Reboot and Factory reset actions
	 * @throws Exception throws an exception if a handler is already added
	 */
	public void addDeviceActionHandler(DeviceActionHandler actionHandler) throws Exception {
		final String METHOD = "addDeviceActionHandler";

		if(this.actionHandler != null) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Action Handler is already set, "
					+ "so can not add the new Action handler !");
			
			throw new Exception("Action Handler is already set, "
					+ "so can not add the new Action handler !");
		}
		
		actionHandler.setDeviceData(this);
		getDeviceAction().addPropertyChangeListener(Resource.ChangeListenerType.INTERNAL, actionHandler);
		actionHandler.start();
		this.actionHandler = actionHandler;
		
	}

	/**
	 * Returns the device action object
	 * @return DeviceAction object or null
	 */
	public DeviceAction getDeviceAction() {
		if(this.deviceAction == null) {
			this.deviceAction = new DeviceAction();
		}
		
		return deviceAction;
	}

	/**
	 * <p>Adds a firmware handler that is of type <code>DeviceFirmwareHandler</code></p>
	 * 
	 * <p>If a device supports firmware update, the abstract class 
	 * <code>DeviceFirmwareHandler</code> should be extended by the device code.
	 * The <code>downloadFirmware</code> and <code>updateFirmware</code>
	 * must be implemented to handle.</p>
	 * 
	 * @param fwHandler DeviceFirmwareHandler that handles the Firmware actions
	 * @throws Exception throws an exception if a handler is already added
	 *
	 */
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
		getDeviceFirmware().addPropertyChangeListener(Resource.ChangeListenerType.INTERNAL, fwHandler);
		fwHandler.start();
		this.fwHandler = fwHandler;
	}
	
	/**
	 * A builder class that helps to construct the DeviceData object. 
	 *
	 */
	public static class Builder {
		private String typeId = null;
		private String deviceId = null;
		private DeviceInfo deviceInfo = null;
		private DeviceLocation deviceLocation = null;
		private DiagnosticErrorCode diagErr = null;
		private DiagnosticLog diagLog = null;
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
		
		public Builder deviceLocation(DeviceLocation deviceLocation) {
			this.deviceLocation = deviceLocation;
			return this;
		}
		
		public Builder deviceErrorCode(DiagnosticErrorCode diagErr) {
			this.diagErr = diagErr;
			return this;
		}
		
		public Builder deviceLog(DiagnosticLog diagLog) {
			this.diagLog = diagLog;
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

	/**
	 * Set the Device-Type
	 * 
	 * @param typeId Device Type to be set
	 */
	void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	
	/**
	 * Set Device-ID
	 * 
	 * @param deviceId DeviceID to be set
	 */
	void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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

	/**
	 * Returns the DiagnosticErrorCode object
	 * @return returns DiagnosticErrorCode or null
	 */
	public DiagnosticErrorCode getDiagnosticErrorCode() {
		if(this.deviceDiag != null) {
			return this.deviceDiag.getErrorCode();
		}
		return null;
	}

	/**
	 * Returns the DiagnosticLog object
	 * @return returns DiagnosticLog or null
	 */
	public DiagnosticLog getDiagnosticLog() {
		if(this.deviceDiag != null) {
			return this.deviceDiag.getLog();
		}
		return null;
	}

}

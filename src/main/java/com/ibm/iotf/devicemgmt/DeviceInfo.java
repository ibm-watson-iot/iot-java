/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
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
package com.ibm.iotf.devicemgmt;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.devicemgmt.resource.StringResource;

/**
 * Device Info as specified in Watson IoT Platform Device Model, The following attributes provide the
 * information about the device,
 * <ul class="simple">
 *   <li>serialNumber
 *   <li>manufacturer
 *   <li>model
 *   <li>deviceClass
 *   <li>description
 *   <li>fwVersion
 *   <li>hwVersion
 *   <li>descriptiveLocation
 *</ul>
 */
public class DeviceInfo extends Resource {
	
	private static final String SERIAL_NUMBER = "serialNumber";
	private static final String MANUFACTURER = "manufacturer";
	private static final String MODEL = "model";
	private static final String DEVICE_CLASS = "deviceClass";
	private static final String DESCRIPTION = "description";
	private static final String FIRMWARE_VERSION = "fwVersion";
	private static final String HARDWARE_VERSION = "hwVersion";
	private static final String DESCRIPTIVE_LOCATION = "descriptiveLocation";

	private StringResource serialNumber; 
	private StringResource manufacturer;
	private StringResource model;
	private StringResource deviceClass;
	private StringResource description;
	private StringResource fwVersion;
	private StringResource hwVersion;
	private StringResource descriptiveLocation;
	
	public static final String RESOURCE_NAME = "deviceInfo";
	/**
	 * Constructs a new <code>DeviceInfo</code> with the specified fields.
	 * @param serialNumber
	 * @param manufacturer
	 * @param model
	 * @param deviceClass
	 * @param description
	 * @param fwVersion
	 * @param hwVersion
	 * @param descriptiveLocation
	 */
	private DeviceInfo(Builder builder) {
		super(RESOURCE_NAME);

		this.setSerialNumber(builder.serialNumber);
		this.setManufacturer(builder.manufacturer);
		this.setModel(builder.model);
		this.setDeviceClass(builder.deviceClass);
		this.setDescription(builder.description);
		this.setFwVersion(builder.fwVersion);
		this.setHwVersion(builder.hwVersion);
		this.setDescriptiveLocation(builder.descriptiveLocation);
	}
	
	/**
	 * Update the device with new values
	 * @param deviceInfo The device info to be updated
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int update(DeviceInfo deviceInfo) {
		return update(deviceInfo.toJsonObject(), true);
	}
	
	/**
	 * Update the device with new values
	 * @param deviceInfo - JsonObject containing the new values
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int update(JsonElement deviceInfo) {
		return update(deviceInfo, true);
	}
	
	/**
	 * Update the device with new values
	 * @param deviceInfo - JsonObject containing the new values
	 * @param fireEvent - whether to fire an update or not
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int update(JsonElement deviceInfo, boolean fireEvent) {
		JsonObject json = (JsonObject) deviceInfo;
		Iterator <Map.Entry<String,JsonElement>>iter = json.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, JsonElement> e = iter.next();
			Resource child = this.getChild(e.getKey());
			if (child != null) {
				child.update(e.getValue(), fireEvent);
			} else {
				switch(e.getKey()) {
					case SERIAL_NUMBER:
						this.setSerialNumber(e.getValue().getAsString());
						break;
						
					case MANUFACTURER:
						this.setManufacturer(e.getValue().getAsString());
						break;
						
					case DEVICE_CLASS:
						this.setDeviceClass(e.getValue().getAsString());
						break;
				
					case FIRMWARE_VERSION:
						this.setFwVersion(e.getValue().getAsString());
						break;
						
					case HARDWARE_VERSION:
						this.setHwVersion(e.getValue().getAsString());
						break;
						
					case MODEL:	
						this.setModel(e.getValue().getAsString());
						break;
					
					case DESCRIPTION:
						this.setDescription(e.getValue().getAsString());
						break;
						
					case DESCRIPTIVE_LOCATION:	
						this.setDescriptiveLocation(e.getValue().getAsString());
						break;
				}
			}
		}
		fireEvent(fireEvent);
		return this.getRC();
	}
	
	public String getSerialNumber() {
		if(this.serialNumber == null) {
			return "";
		}
		return serialNumber.getValue();
	}

	public void setSerialNumber(String serialNumber) {
		this.setSerialNumber(serialNumber, true);
	}
	
	private void setSerialNumber(String serialNumber, boolean fireEvent) {
		if(null == serialNumber) {
			return;
		}
		if(this.serialNumber == null) {
			this.serialNumber = new StringResource(SERIAL_NUMBER, serialNumber);
			this.add(this.serialNumber);
		}
		this.serialNumber.setValue(serialNumber);
		
		fireEvent(fireEvent);
	}

	public String getManufacturer() {
		if(this.manufacturer == null) {
			return "";
		}
		return manufacturer.getValue();
	}

	public void setManufacturer(String manufacturer) {
		this.setManufacturer(manufacturer, true);
	}
	
	private void setManufacturer(String manufacturer, boolean fireEvent) {
		if(null == manufacturer) {
			return;
		}
		if(this.manufacturer == null) {
			this.manufacturer = new StringResource(MANUFACTURER, manufacturer);
			this.add(this.manufacturer);
		}
		this.manufacturer.setValue(manufacturer);
		
		fireEvent(fireEvent);
	}

	public String getModel() {
		if(this.model == null) {
			return "";
		}
		return model.getValue();
	}

	public void setModel(String model) {
		this.setModel(model, true);
	}
	
	private void setModel(String model, boolean fireEvent) {
		if(null == model) return;
		
		if(this.model == null) {
			this.model= new StringResource(MODEL, model);
			this.add(this.model);
		}
		this.model.setValue(model);
		fireEvent(fireEvent);
	}

	public String getDeviceClass() {
		if(this.deviceClass == null) {
			return "";
		}
		return deviceClass.getValue();
	}

	public void setDeviceClass(String deviceClass) {
		this.setDeviceClass(deviceClass, true);
	}
	
	private void setDeviceClass(String deviceClass, boolean fireEvent) {
		if(null == deviceClass) return;
		if(this.deviceClass == null) {
			this.deviceClass= new StringResource(DEVICE_CLASS, deviceClass);
			this.add(this.deviceClass);
		}
		this.deviceClass.setValue(deviceClass);
		fireEvent(fireEvent);
	}

	public String getDescription() {
		if(this.description == null) {
			return "";
		}
		return description.getValue();
	}

	
	public void setDescription(String description) {
		this.setDescription(description, true);
	}
	
	private void setDescription(String description, boolean fireEvent) {
		
		if(null == description) return;
		
		if(this.description == null) {
			this.description= new StringResource(DESCRIPTION, description);
			this.add(this.description);
		}
		this.description.setValue(description);
		fireEvent(fireEvent);
	}

	public String getFwVersion() {
		if(this.fwVersion == null) {
			return "";
		}
		return fwVersion.getValue();
	}

	public void setFwVersion(String fwVersion) {
		this.setFwVersion(fwVersion, true);
	}
	
	private void setFwVersion(String fwVersion, boolean fireEvent) {
		
		if(null == fwVersion) return;
		
		if(this.fwVersion == null) {
			this.fwVersion = new StringResource(FIRMWARE_VERSION, fwVersion);
			this.add(this.fwVersion);
		}
		this.fwVersion.setValue(fwVersion);
		fireEvent(fireEvent);
	}

	public String getHwVersion() {
		if(this.hwVersion == null) {
			return "";
		}
		return hwVersion.getValue();
	}

	
	public void setHwVersion(String hwVersion) {
		this.setHwVersion(hwVersion, true);
	}
	
	private void setHwVersion(String hwVersion, boolean fireEvent) {
		
		if(null == hwVersion) return;
		
		if(this.hwVersion == null) {
			this.hwVersion= new StringResource(HARDWARE_VERSION, hwVersion);
			this.add(this.hwVersion);
		}
		this.hwVersion.setValue(hwVersion);
		
		fireEvent(fireEvent);
	}

	public String getDescriptiveLocation() {
		if(this.descriptiveLocation == null) {
			return "";
		}
		return descriptiveLocation.getValue();
	}

	public void setDescriptiveLocation(String descriptiveLocation) {
		this.setDescriptiveLocation(descriptiveLocation, true);
	}
	
	private void setDescriptiveLocation(String descriptiveLocation, boolean fireEvent) {
		
		if(descriptiveLocation == null) return;
		
		if(this.descriptiveLocation == null) {
			this.descriptiveLocation= new StringResource(DESCRIPTIVE_LOCATION, descriptiveLocation);
			this.add(this.descriptiveLocation);
		}
		this.descriptiveLocation.setValue(descriptiveLocation);
		fireEvent(fireEvent);
	}

	/**
	 * @return <code>JsonObject</code> of the <code>DeviceInfo</code>
	 */
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		if (this.serialNumber!= null) {
			json.add(serialNumber.getResourceName(), new JsonPrimitive(serialNumber.getValue()));
		} 
		
		if (this.manufacturer!= null) {
			json.add(manufacturer.getResourceName(), new JsonPrimitive(manufacturer.getValue()));
		} 
		
		if (this.model!= null) {
			json.add(model.getResourceName(), new JsonPrimitive(model.getValue()));
		} 
		
		if (this.deviceClass!= null) {
			json.add(deviceClass.getResourceName(), new JsonPrimitive(deviceClass.getValue()));
		} 
		
		if (this.description!= null) {
			json.add(description.getResourceName(), new JsonPrimitive(description.getValue()));
		}
		
		if (this.fwVersion!= null) {
			json.add(fwVersion.getResourceName(), new JsonPrimitive(fwVersion.getValue()));
		}
		
		if (this.hwVersion!= null) {
			json.add(hwVersion.getResourceName(), new JsonPrimitive(hwVersion.getValue()));
		} 
		
		if (this.descriptiveLocation!= null) {
			json.add(descriptiveLocation.getResourceName(), new JsonPrimitive(descriptiveLocation.getValue()));
		}
		
		return json;
	}
	

	/**
	 * @return JSON string of the <code>DeviceInfo</code>
	 */
	public String toString() {
		return toJsonObject().toString();
	}
	
	/**
	 * A builder class that helps to create a Device Info object
	 *
	 */
	public static class Builder {
		private String serialNumber; 
		private String manufacturer; 
		private String model; 
		private String deviceClass;
		private String description; 
		private String fwVersion; 
		private String hwVersion; 
		private String descriptiveLocation;
		
		public Builder() {}
		
		public Builder serialNumber(String serialNumber) {
			this.serialNumber = serialNumber;
			return this;
		}
		
		public Builder manufacturer(String manufacturer) {
			this.manufacturer = manufacturer;
			return this;
		}
		
		public Builder model(String model) {
			this.model = model;
			return this;
		} 
		
		public Builder deviceClass(String deviceClass) {
			this.deviceClass = deviceClass;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		} 
		
		public Builder fwVersion(String fwVersion) {
			this.fwVersion = fwVersion;
			return this;
		} 
		
		public Builder hwVersion(String hwVersion) {
			this.hwVersion = hwVersion;
			return this;
		} 
		
		public Builder descriptiveLocation(String descriptiveLocation) {
			this.descriptiveLocation = descriptiveLocation;
			return this;
		}
		
		public DeviceInfo build() {
			DeviceInfo info = new DeviceInfo(this);
			return info;
		}
	}


}

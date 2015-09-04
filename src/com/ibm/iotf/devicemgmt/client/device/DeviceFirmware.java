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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.client.device.Resource.NumberResource;
import com.ibm.iotf.devicemgmt.client.device.Resource.Resource;
import com.ibm.iotf.devicemgmt.client.device.Resource.StringResource;

/**
 * Device Firmware as specified in IoTF Device Model.
 * <li>mgmt.firmware.version
 * <li>mgmt.firmware.name
 * <li>mgmt.firmware.url
 * <li>mgmt.firmware.verifier
 * <li>mgmt.firmware.state
 * <li>mgmt.firmware.updateStatus
 * <li>mgmt.firmware.updatedDateTime
 * 
 * 
 */
public class DeviceFirmware extends Resource {
	
	public enum FirmwareState {
		IDLE(0), DOWNLOADING(1), DOWNLOADED(2);
		
		private final int state;
		
		private FirmwareState(int state) {
			this.state = state;
		}
		
		public int getState() {
			return state;
		}
	}
	
	public enum FirmwareUpdateStatus {
		SUCCESS(0),
		IN_PROGRESS(1),
		OUT_OF_MEMORY(2),
		CONNECTION_LOST(3),
		VERIFICATION_FAILED(4),
		UNSUPPORTED_IMAGE(5),
		INVALID_URI(6);
		
		private final int status;
		
		private FirmwareUpdateStatus(int status) {
			this.status = status;
		}
		public int getStatus() {
			return status;
		}
	}
	
	public static final String FIRMWARE_DOWNLOAD_START = "FirmwareDownloadStart";
	public static final String FIRMWARE_UPDATE_START = "FirmwareUpdateStart";
	
	private static final String VERSION = "version";
	private static final String NAME = "name";
	private static final String URL = "uri";
	private static final String VERIFIER = "verifier";
	private static final String STATE = "state";
	private static final String UPDATE_STATUS = "updateStatus";
	private static final String UPDATEDATETIME = "updatedDateTime";
	

	public static final String RESOURCE_NAME = "firmware";
	
	private StringResource version; 
	private StringResource name; 
	private StringResource url; 
	private StringResource verifier; 
	private NumberResource state;
	private NumberResource updateStatus; 
	
	private DeviceFirmware(Builder builder) {
		super(RESOURCE_NAME);
		this.setVersion(builder.version, false);
		this.setUrl(builder.url, false);
		this.setVerifier(builder.verifier, false);
		this.setState(builder.state, false);
		this.setName(builder.name, false);
		this.setUpdateStatus(builder.updateStatus, false);
	}
	
	public void update(DeviceFirmware firmware) {
		update(firmware.toJsonObject(), true);
	}
	
	public int update(JsonElement firmware, boolean fireEvent) {
		JsonObject json = (JsonObject) firmware;
		Iterator <Map.Entry<String,JsonElement>>iter = json.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, JsonElement> e = iter.next();
			Resource child = this.getChild(e.getKey());
			if (child != null) {
				child.update(e.getValue(), fireEvent);
			} else {
				switch(e.getKey()) {
				case VERSION: 
					this.setVersion(e.getValue().getAsString(), fireEvent);
					break;
				case NAME:
					this.setName(e.getValue().getAsString(), fireEvent);
					break;
					
				case URL:
					this.setUrl(e.getValue().getAsString(), fireEvent);
					break;
					
				case VERIFIER:
					this.setVerifier(e.getValue().getAsString(), fireEvent);
					break;
				}
			}
		}
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
		return this.getRC();
	}
	
	public String getVersion() {
		if (this.version != null) {
			return version.getValue();
		} else {
			return null;
		}
	}
	
	public void setVersion(String version) {
		this.setVersion(version, true);
	}
	
	private void setVersion(String version, boolean fireEvent) {
		
		if(version == null) return;
		
		if (this.version == null) {
			this.version = new StringResource(VERSION, version);
			this.add(this.version);
		} else {
			this.version.setValue(version);
		}
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
	}
	
	public String getUrl() {
		if (this.url != null) {
			return url.getValue();
		} else {
			return null;
		}
	}
	
	public void setUrl(String url) {
		this.setUrl(url, true);
	}
	
	private void setUrl(String url, boolean fireEvent) {
		
		if(url == null) return;
		
		if (this.url == null) {
			this.url = new StringResource(URL, url);
			this.add(this.url);
		} else {
			this.url.setValue(url);
		}
		
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
	}
	
	public String getVerifier() {
		if (this.verifier != null) {
			return verifier.getValue();
		} else {
			return null;
		}
	}
	
	public void setVerifier(String verifier) {
		setVerifier(verifier, true);
	}
	
	private void setVerifier(String verifier, boolean fireEvent) {
		
		if(verifier == null) return;
		
		if (this.verifier == null) {
			this.verifier = new StringResource(VERIFIER, verifier);
			this.add(this.verifier);
		} else {
			this.verifier.setValue(verifier);
		}
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
	}

	public int getState() {
		return this.state.getValue().intValue();
	}
	
	public void setState(FirmwareState state) {
		setState(state, true);
	}
	
	private void setState(FirmwareState state, boolean fireEvent) {
		if (this.state == null) {
			if(state != null) {
				this.state = new NumberResource(STATE, state.getState());
			} else {
				this.state = new NumberResource(STATE, FirmwareState.IDLE.getState());
			}
			this.add(this.state);
		} else {
			this.state.setValue(state.getState());
		}
		
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
	}
	
	public String getName() {
		if(this.name != null) {
			return this.name.getValue();
		} else {
			return null;
		}
	}
	
	public void setName(String name) {
		this.setName(name, true);
	}
	
	private void setName(String name, boolean fireEvent) {
		
		if(name == null) return;
		
		if (this.name == null) {
			this.name = new StringResource(NAME, name);
			this.add(this.name);
		} else {
			this.name.setValue(name);
		}
		
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
	}
	
	public int getUpdateStatus() {
		return this.updateStatus.getValue().intValue();
	}
	
	public void setUpdateStatus(FirmwareUpdateStatus updateStatus) {
		this.setUpdateStatus(updateStatus, true);
	}
	
	private void setUpdateStatus(FirmwareUpdateStatus updateStatus, boolean fireEvent) {
		
		if (this.updateStatus == null) {
			if(updateStatus != null) {
				this.updateStatus = new NumberResource(UPDATE_STATUS, updateStatus.getStatus());
			} else {
				this.updateStatus = new NumberResource(UPDATE_STATUS, FirmwareUpdateStatus.SUCCESS.getStatus());
			}
			this.add(this.updateStatus);
		} else {
			this.updateStatus.setValue(updateStatus.getStatus());
		}
		
		if(fireEvent)
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
	}
	
	public void fireEvent(String event) {
		pcs.firePropertyChange(event, null, this);
	}
	
	/**
	 * @return <code>JsonObject</code> of the <code>DeviceInfo</code>
	 */
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		if(name != null) {
			json.addProperty(this.name.getResourceName(), name.getValue());
		}
		if(version != null) {
			json.addProperty(this.version.getResourceName(), version.getValue());
		}
		
		if(url != null) {
			json.addProperty(this.url.getResourceName(), url.getValue());
		}
		
		if(verifier != null) {
			json.addProperty(this.verifier.getResourceName(), verifier.getValue());
		}
		
		if(state != null) {
			json.addProperty(this.state.getResourceName(), state.getValue());
		}
		
		if(this.updateStatus != null) {
			json.addProperty(this.updateStatus.getResourceName(), updateStatus.getValue());
		}
		
		return json;
	}
	
	/**
	 * @return JSON string of the <code>DeviceInfo</code>
	 */
	public String toString() {
		return toJsonObject().toString();
	}
	
	public static class Builder {
		private String version; 
		private String name; 
		private String url; 
		private String verifier; 
		private FirmwareState state;
		private FirmwareUpdateStatus updateStatus; 
		
		public Builder() {
			
		}
		
		public Builder version(String version) {
			this.version = version;
			return this;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder url(String url) {
			this.url = url;
			return this;
		}
		
		public Builder verifier(String verifier) {
			this.verifier = verifier;
			return this;
		}
		
		public Builder state(FirmwareState state) {
			this.state = state;
			return this;
		}
		
		public Builder updateStatus(FirmwareUpdateStatus updateStatus) {
			this.updateStatus = updateStatus;
			return this;
		}
		
		//Return the constructed DeviceFirmware object
        public DeviceFirmware build() {
        	DeviceFirmware firmware =  new DeviceFirmware(this);
            return firmware;
        }
		
	}
}

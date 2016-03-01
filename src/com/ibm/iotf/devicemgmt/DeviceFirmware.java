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
import com.ibm.iotf.devicemgmt.resource.NumberResource;
import com.ibm.iotf.devicemgmt.resource.Resource;
import com.ibm.iotf.devicemgmt.resource.StringResource;

/**
 * Device Firmware as specified in Watson IoT Platform Device Model. 
 * The following attributes are present as part of the Device Firmware,
 * <ul class="simple">
 * <li>mgmt.firmware.version
 * <li>mgmt.firmware.name
 * <li>mgmt.firmware.url
 * <li>mgmt.firmware.verifier
 * <li>mgmt.firmware.state
 * <li>mgmt.firmware.updateStatus
 * <li>mgmt.firmware.updatedDateTime
 * </ul>
 * 
 */
public class DeviceFirmware extends Resource {
	/**
	 * <p>The firmware update process is separated into two distinct actions, Downloading Firmware, and Updating Firmware.
	 * The status of each of these actions is stored in a separate attribute on the device. 
	 * The <code class="docutils literal"><span class="pre">mgmt.firmware.state</span></code>
	 * attribute describes the status of the firmware download. The possible values for 
	 * <code class="docutils literal"><span class="pre">mgmt.firmware.state</span></code> are:</p>
	 * <table border="1" class="docutils">
	 * 	<colgroup>
	 * 	<col width="12%" />
	 * 	<col width="11%" />
	 * 	<col width="77%" />
	 * </colgroup>
	 * <thead valign="bottom">
	 * 	<tr class="row-odd"><th class="head">Value</th>
	 * 	<th class="head">State</th>
	 * 	<th class="head">Meaning</th>
	 * </tr>
	 * </thead>
	 * <tbody valign="top">
	 * <tr class="row-even"><td>0</td>
	 * <td>Idle</td>
	 * <td>The device is currently not in the process of downloading firmware</td>
	 * </tr>
	 * <tr class="row-odd"><td>1</td>
	 * <td>Downloading</td>
	 * <td>The device is currently downloading firmware</td>
	 * </tr>
	 * <tr class="row-even"><td>2</td>
	 * <td>Downloaded</td>
	 * <td>The device has successfully downloaded a firmware update and it is ready to install</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *  
	 */
	public enum FirmwareState {
		IDLE(0), DOWNLOADING(1), DOWNLOADED(2);
		
		private final int state;
		
		private FirmwareState(int state) {
			this.state = state;
		}
		
		/**
		 * Returns the current Firmware download State
		 * @return
		 */
		public int getState() {
			return state;
		}
	}
	
	/**
	 * <p>The <code class="docutils literal"><span class="pre">mgmt.firmware.updateStatus</span></code> 
	 * attribute describes the status of firmware update. The possible values for <code class="docutils literal">
	 * <span class="pre">mgmt.firmware.status</span></code> are:</p>
	 * <table border="1" class="docutils">
	 * <colgroup>
	 * <col width="13%" />
	 * <col width="20%" />
	 * <col width="67%" />
	 * </colgroup>
	 * <thead valign="bottom">
	 * <tr class="row-odd"><th class="head">Value</th>
	 * <th class="head">State</th>
	 * <th class="head">Meaning</th>
	 * </tr>
	 * </thead>
	 * <tbody valign="top">
	 * <tr class="row-even"><td>0</td>
	 * <td>Success</td>
	 * <td>The firmware has been successfully updated</td>
	 * </tr>
	 * <tr class="row-odd"><td>1</td>
	 * <td>In Progress</td>
	 * <td>The firmware update has been initiated but is not yet complete</td>
	 * </tr>
	 * <tr class="row-even"><td>2</td>
	 * <td>Out of Memory</td>
	 * <td>An out of memory condition has been detected during the operation.</td>
	 * </tr>
	 * <tr class="row-odd"><td>3</td>
	 * <td>Connection Lost</td>
	 * <td>The connection was lost during the firmware download</td>
	 * </tr>
	 * <tr class="row-even"><td>4</td>
	 * <td>Verification Failed</td>
	 * <td>The firmware did not pass verification</td>
	 *</tr>
	 *<tr class="row-odd"><td>5</td>
	 *<td>Unsupported Image</td>
	 *<td>The downloaded firmware image is not supported by the device</td>
	 *</tr>
	 *<tr class="row-even"><td>6</td>
	 *<td>Invalid URI</td>
	 *<td>The device could not download the firmware from the provided URI</td>
	 *</tr>
	 *</tbody>
	 *</table>
	 */
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
		
		/**
		 * Returns the current Firmware Update Status
		 * @return
		 */
		public int getStatus() {
			return status;
		}
	}
	
	private static final String VERSION = "version";
	private static final String NAME = "name";
	private static final String URL = "uri";
	private static final String VERIFIER = "verifier";
	private static final String STATE = "state";
	private static final String UPDATE_STATUS = "updateStatus";
	
	public static final String RESOURCE_NAME = "firmware";
	
	private StringResource version; 
	private StringResource name; 
	private StringResource url; 
	private StringResource verifier; 
	private NumberResource state;
	private NumberResource updateStatus;
	private String deviceId;
	private String typeId; 
	
	private DeviceFirmware(Builder builder) {
		super(RESOURCE_NAME);
		this.setVersion(builder.version, false);
		this.setUrl(builder.url, false);
		this.setVerifier(builder.verifier, false);
		this.setState(builder.state, false);
		this.setName(builder.name, false);
		this.setUpdateStatus(builder.updateStatus, false);
	}
	
	/**
	 * Update the Firmware object with new values
	 * 
	 * @param firmware
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int update(DeviceFirmware firmware) {
		return update(firmware.toJsonObject(), true);
	}
	
	/**
	 * Update the Firmware object with new values
	 * 
	 * @param firmware
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */

	public int update(JsonElement firmware) {
		return update(firmware, true);
	}
	
	/**
	 * Update the Firmware object with new values
	 * 
	 * @param firmware
	 * @param fireEvent - whether to fire an update event of not
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	
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
		fireEvent(fireEvent);
		return this.getRC();
	}
	
	/**
	 * returns the version of the firmware
	 * @return
	 */
	public String getVersion() {
		if (this.version != null) {
			return version.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the firmware with the given version 
	 * @param version version to be updated
	 */
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
		fireEvent(fireEvent);
	}
	
	/**
	 * Returns the firmware URL
	 */
	public String getUrl() {
		if (this.url != null) {
			return url.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the firmware URL
	 */
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
		
		fireEvent(fireEvent);
	}
	
	/**
	 * Returns the firmware Verifier
	 */
	public String getVerifier() {
		if (this.verifier != null) {
			return verifier.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the firmware verifier
	 */
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
		fireEvent(fireEvent);
	}

	/**
	 * Returns the firmware state
	 */
	public int getState() {
		return this.state.getValue().intValue();
	}
	
	/**
	 * Set the firmware state
	 */
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
		
		fireEvent(fireEvent);
	}
	
	/**
	 * Returns the name of the firmware
	 */
	public String getName() {
		if(this.name != null) {
			return this.name.getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the name of the firmware
	 */
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
		
		fireEvent(fireEvent);
	}
	
	/**
	 * Returns the Firmware Update status
	 */
	public int getUpdateStatus() {
		return this.updateStatus.getValue().intValue();
	}
	
	/**
	 * Set the Firmware Update status
	 */
	public void setUpdateStatus(FirmwareUpdateStatus updateStatus) {
		this.setUpdateStatus(updateStatus, true);
		
		/**
		 * When the firmware update is success, set the FWVersion
		 * of the Device accordingly. Also, set the verifier to null
		 * as the verification is done.
		 */
		if(updateStatus == FirmwareUpdateStatus.SUCCESS) {
			if(null != version && !("".equals(version))) {
				Resource mgmt = this.getParent();
				Resource root = mgmt.getParent();
				Resource deviceInfo = root.getChild(DeviceInfo.RESOURCE_NAME);
				if(deviceInfo != null) {
					((DeviceInfo) deviceInfo).setFwVersion(this.getVersion());
				}
			}
			this.setVerifier(null, false);
		}
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
		
		fireEvent(fireEvent);
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
	
	/**
	 * A Builder class that helps to build the Device Firmware object
	 */
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
	
	void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getTypeId() {
		return typeId;
	}
}

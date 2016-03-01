/**
 *****************************************************************************
 * Copyright (c) 2016 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Patrizia Gufler1 - Initial Contribution
 * Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 */
package com.ibm.iotf.sample.devicemgmt.gateway.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.client.gateway.GatewayClient;
import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceFirmware;
import com.ibm.iotf.devicemgmt.LogSeverity;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareState;
import com.ibm.iotf.devicemgmt.DeviceFirmware.FirmwareUpdateStatus;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.gateway.DeviceInterface;
import com.ibm.iotf.sample.devicemgmt.gateway.GatewayFirmwareHandlerSample;

public abstract class Device implements DeviceInterface, Runnable {
	private String deviceId;
	protected DeviceType deviceType;
	private boolean managable = false;
	private boolean firmwareAction = false;
	private boolean deviceAction = false;
	private boolean managed;
	private int eventUpdateInterval;
	private List<ErrorCode> errorCode = new ArrayList<ErrorCode>();;
	private List<DiagLog> diagLog = new ArrayList<DiagLog>();

	protected GatewayClient gwClient;
	private String downloadedFirmwareName;

	public Device(String deviceId, GatewayClient gwClient, int updateInterval) {
		this.deviceId = deviceId;
		this.gwClient = gwClient;
		this.eventUpdateInterval = updateInterval;
	}
	
	/**
	 * Let us use the WIoTP client Id as the key to identify the device
	 * @return
	 */
	public String getKey() {
		return new StringBuilder("d:").
				append(gwClient.getOrgId()).
				append(':').
				append(this.deviceType.getDeviceType()).
				append(':').
				append(this.deviceId).toString();
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getDeviceType() {
		return deviceType.getDeviceType();
	}

	public boolean isManagable() {
		return this.managable;
	}

	public void setManagable(boolean firmwareAction, boolean deviceAction) {
		this.managable = firmwareAction || deviceAction;
		this.setFirmwareAction(firmwareAction);
		this.setDeviceAction(deviceAction);
	}

	public boolean isFirmwareAction() {
		return firmwareAction;
	}

	private void setFirmwareAction(boolean firmwareAction) {
		this.firmwareAction = firmwareAction;
	}

	public boolean isDeviceAction() {
		return deviceAction;
	}

	private void setDeviceAction(boolean deviceAction) {
		this.deviceAction = deviceAction;
	}

	public boolean isManaged() {
		return this.managed;
	}
	
	public void setManaged(boolean managed) {
		this.managed = managed;
		if(this.managed == true) {
			ManagedGateway gateway = (ManagedGateway)this.gwClient;
			for (DiagLog diagLog : this.diagLog) {
				gateway.addDeviceLog(getDeviceType().toString(), getDeviceId(),
						diagLog.getMessage(), diagLog.getTimeStamp(), diagLog.getSeverity());
			}
			
			for (ErrorCode errorCode : this.errorCode) {
				gateway.addDeviceErrorCode(getDeviceType().toString(), getDeviceId(), errorCode.getErrorCode());
			}
			
			this.diagLog.clear();
			this.errorCode.clear();
		}
	}

	public void setErrorCode(int errorCode) {
		// Check if the device is managed
		if(this.isManaged()) {
			ManagedGateway gateway = (ManagedGateway)this.gwClient;
			int rc = gateway.addDeviceErrorCode(this.deviceType.getDeviceType(), this.deviceId, errorCode);
			if(rc != 200) {
				System.err.println("Error in adding errorCode :: "+errorCode);
			}
		} else {
			// If not managed already, add it in ArrayList and send it later
			this.errorCode.add(new ErrorCode(errorCode));
		}
		
	}

	public void sendCommand(String cmd) {
		
	}

	public void setFirmwareName(String downloadedFirmwareName) {
		this.downloadedFirmwareName = downloadedFirmwareName;
	}
	
	/**
	 * A sample method to handle the Light's reboot request from the DM server.
	 * 
	 */
	@Override
	public void reboot(DeviceAction action) {
		ManagedGateway gateway = (ManagedGateway) gwClient;
		
		setLog(LogSeverity.informational, "reboot started..", null, new Date());
		try {
			// Pretend to reboot the device
			Thread.sleep(1000 * 2);
		} catch(InterruptedException ie) {}
		
		setLog(LogSeverity.informational, "reboot ended..", null, new Date());

		// We must send a manage request inorder to complete the reboot request successfully
		try {
			
			gateway.sendDeviceManageRequest(deviceType.getDeviceType(), this.deviceId, 0, this.firmwareAction, this.deviceAction);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		System.out.println("Executed restart command status (true)");
	}

	/**
	 * <p>A sample firmware update method that pretends to install the firmware to the device.
	 * The real implementation can follow a similar technique to push the firmware to Oven or other devices
	 * that accept the firmware</p>
	 * 
	 * Also, this method adds a diagnostic log containing the progress to the IoT Platform.
	 * 
	 */
	public void updateFirmware(DeviceFirmware deviceFirmware) {
		
		String message = "Firmware Update Event start";
		// Inform the server about the status through Diaglog if needed
		setLog(LogSeverity.informational, message, null, new Date());
		
		System.out.println("<--("+this.deviceId+") Progress ::");
		
		for(int i = 1; i < 21; i++) {
			try {
				Thread.sleep(200);
			} catch(Exception e) {
				
			}
			// Inform the server about the progress through Diaglog if needed
			setLog(LogSeverity.informational, "progress " + (i * 5), null, new Date());
			
			System.out.print("  "+ (i * 5) + "%");
		}

		// Inform the server about the status through Diaglog if needed
		message = "Firmware Update Event End";
		setLog(LogSeverity.informational, message, null, new Date());
		
		System.out.println("Firmware Update successfull !!");
		deviceFirmware.setUpdateStatus(FirmwareUpdateStatus.SUCCESS);
		deviceFirmware.setState(FirmwareState.IDLE);
		
		/**
		 * Delete the temporary firmware file
		 */
		GatewayFirmwareHandlerSample.deleteFile(downloadedFirmwareName);
		
		this.downloadedFirmwareName = null;
	}

	public void toggleDisplay() {
		
	}
	
	private static class ErrorCode {
		private int errorCode;

		ErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}

		public int getErrorCode() {
			return this.errorCode;
		}
	}

	private static class DiagLog {
		private LogSeverity severity;
		private String message;
		private String data;
		private Date timestamp;

		DiagLog(LogSeverity severity, String message, String data, Date timestamp) {
			this.setSeverity(severity);
			this.setMessage(message);
			this.setData(data);
			this.setTimestamp(timestamp);
		}

		public LogSeverity getSeverity() {
			return severity;
		}

		private void setSeverity(LogSeverity severity) {
			this.severity = severity;
		}

		public String getMessage() {
			return message;
		}

		private void setMessage(String message) {
			this.message = message;
		}

		public String getData() {
			return this.data;
		}

		private void setData(String data) {
			this.data = data;
		}

		public Date getTimeStamp() {
			return this.timestamp;
		}

		private void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
	}

	public void setLog(LogSeverity severity, String message, String data, Date date) {
		
		// Check if the device is managed
		if(this.isManaged()) {
			ManagedGateway gateway = (ManagedGateway)this.gwClient;
			int rc = gateway.addDeviceLog(this.deviceType.getDeviceType(), this.deviceId, message, date, severity);
			if(rc != 200) {
				System.err.println("Error in adding log :: "+message);
			}
		} else {
			// If not managed already, add it in ArrayList and send it later
			this.diagLog.add(new DiagLog(severity, message, data, date));
		}
	}
	
	public GatewayClient getGwClient() {
		return gwClient;
	}
	
	public int getEventUpdateInterval() {
		return eventUpdateInterval;
	}


	enum DeviceType {
		OVEN("Oven"), ELEVATOR("Elevator"), TEMPERATURE("Temperature"), LIGHT("Light"), SWITCH("Switch");

		private String deviceType;

		DeviceType(String deviceType) {
			this.deviceType = deviceType;
		}

		public String getDeviceType() {
			return this.deviceType;
		}
	}

}


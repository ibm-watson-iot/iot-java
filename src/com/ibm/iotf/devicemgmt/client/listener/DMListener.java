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
package com.ibm.iotf.devicemgmt.client.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import com.ibm.iotf.devicemgmt.client.device.DeviceLocation;
import com.ibm.iotf.devicemgmt.client.device.ManagedClient;
import com.ibm.iotf.devicemgmt.client.device.DeviceDiagnostic;
import com.ibm.iotf.devicemgmt.client.device.DeviceTopic;
import com.ibm.iotf.util.LoggerUtility;

public class DMListener implements PropertyChangeListener {
	
	private static Map<ManagedClient, DMListener> dmListeners = new HashMap<ManagedClient, DMListener>();
	private static final String CLASS_NAME = DMListener.class.getName();
	
	private ManagedClient dmClient;
	
	// List of Notifiers
	private LocationChangeNotifier locationNotifier;
	private DiagnosticErrorCodeChangeNotifier errorCodeNotifier;
	private DiagnosticLogChangeNotifier logNotifier;
	
	public DMListener(ManagedClient client) {
		this.dmClient = client;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String METHOD = "propertyChange"; 
		switch(event.getPropertyName()) {
			case DeviceDiagnostic.LOG_CHANGE_EVENT:
				this.logNotifier.handleEvent(event);
				break;
								
			case DeviceDiagnostic.ERRORCODE_CHANGE_EVENT:
				this.errorCodeNotifier.handleEvent(event);
				break;
								
			case DeviceDiagnostic.ERRORCODE_CLEAR_EVENT:
				this.errorCodeNotifier.clearEvent(event);
				break;
								
			case DeviceDiagnostic.LOG_CLEAR_EVENT:
				this.logNotifier.clearEvent(event);
				break;
								
			case DeviceLocation.RESOURCE_NAME:
				this.locationNotifier.handleEvent(event);
				break;
		}
	}
	
	
	public static void start(ManagedClient dmClient) {
		final String METHOD = "start";
		LoggerUtility.fine(CLASS_NAME, METHOD, "MQTT Connected(" + dmClient.isConnected() + ")");
		
		DMListener dmListener = dmListeners.get(dmClient);
		if(dmListener == null) {
			dmListener = new DMListener(dmClient);
			dmListeners.put(dmClient, dmListener);
			
		}
		dmListener.createNotifiers();
	}
	
	private void createNotifiers() {
		final String METHOD = "createNotifiers";
		if (dmClient.getDeviceData().getDeviceLocation() != null) {
			if (locationNotifier == null) {
				locationNotifier = new LocationChangeNotifier(this.dmClient);
				locationNotifier.setNotifyTopic(DeviceTopic.UPDATE_LOCATION);
				dmClient.getDeviceData().getDeviceLocation().addPropertyChangeListener(this);
			}
		} else {
			LoggerUtility.info(CLASS_NAME, METHOD,  "The device does not support location notification.");
		}
		
		DeviceDiagnostic diagnostic = dmClient.getDeviceData().getDeviceDiagnostic(); 
		if (diagnostic != null) {			
			if(diagnostic.getErrorCode() != null && this.errorCodeNotifier == null) {
				this.errorCodeNotifier = new DiagnosticErrorCodeChangeNotifier(dmClient);
				errorCodeNotifier.setNotifyTopic(DeviceTopic.CREATE_DIAG_ERRCODES);
				diagnostic.getErrorCode().addPropertyChangeListener(this);
			}
		
			if(diagnostic.getLog() != null && this.logNotifier == null) {
				logNotifier = new DiagnosticLogChangeNotifier(dmClient);
				logNotifier.setNotifyTopic(DeviceTopic.ADD_DIAG_LOG);
				diagnostic.getLog().addPropertyChangeListener(this);
			}
		} else {
			LoggerUtility.info(CLASS_NAME, METHOD,  "The device does not support Diagnostic notification.");
		}

	}
	
	public static void stop(ManagedClient dmClient) {
	
		DMListener dmListener = dmListeners.remove(dmClient);
		
		if(dmListener.locationNotifier != null) {
			dmClient.getDeviceData().getDeviceLocation().removePropertyChangeListener(dmListener);
		}
			
		if(dmListener.errorCodeNotifier != null) {
			dmClient.getDeviceData().getDeviceDiagnostic().getErrorCode().removePropertyChangeListener(dmListener);
		}
			
		if(dmListener.logNotifier != null) {
			dmClient.getDeviceData().getDeviceDiagnostic().getLog().removePropertyChangeListener(dmListener);
		}
	}
	
}

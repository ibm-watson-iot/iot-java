package com.ibm.iotf.test.common;

import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.StatusCallback;

public class TestStatusCallback implements StatusCallback {
	
	private ApplicationStatus appStatus = null;
	private DeviceStatus devStatus = null;

	@Override
	public void processApplicationStatus(ApplicationStatus status) {
		this.appStatus = status;
	}

	@Override
	public void processDeviceStatus(DeviceStatus status) {
		this.devStatus = status;
	}
	
	public ApplicationStatus getAppStatus() { return this.appStatus; }
	public DeviceStatus getDeviceStatus() { return this.devStatus; }
	

	public void clear() {
		appStatus = null;
		devStatus = null;
	}
}

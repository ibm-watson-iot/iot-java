package com.ibm.wiotp.sdk.test.util.callbacks;

import com.ibm.wiotp.sdk.app.callbacks.StatusCallback;
import com.ibm.wiotp.sdk.app.messages.ApplicationStatus;
import com.ibm.wiotp.sdk.app.messages.DeviceStatus;

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

package com.ibm.iotf.client.app;


public interface StatusCallback {

	public void processApplicationStatus(ApplicationStatus status);
	
	public void processDeviceStatus(DeviceStatus status);
}

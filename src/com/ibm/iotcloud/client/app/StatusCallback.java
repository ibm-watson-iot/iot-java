package com.ibm.iotcloud.client.app;


public interface StatusCallback {

	public void processApplicationStatus(ApplicationStatus status);
	
	public void processDeviceStatus(DeviceStatus status);
}

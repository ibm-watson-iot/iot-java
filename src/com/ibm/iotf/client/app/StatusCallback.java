package com.ibm.iotf.client.app;


/**
 * This interface holds the callbacks for processing status
 *
 */
public interface StatusCallback {

	
	/**
	 * This method processes the application status
	 * @param status
	 * 			an object of ApplicationStatus
	 */
	public void processApplicationStatus(ApplicationStatus status);
	
	/**
	 * This method processes device status
	 * @param status
	 * 			an object of DeviceStatus
	 */
	public void processDeviceStatus(DeviceStatus status);
}

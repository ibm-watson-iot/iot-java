/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
  Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.sample.devicemgmt.gateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.sample.client.gateway.DeviceInterface;

/**
 * This sample Gateway action handler demonstrates how one can reboot the Gateway and the 
 * attached Arduino Uno device. 
 * 
 */
public class GatewayActionHandlerSample extends DeviceActionHandler {
	
	private Map<String, DeviceInterface> deviceMap = new HashMap<String, DeviceInterface>();
	private String gatewayDeviceId;
	
	public void addDeviceInterface(String deviceId, DeviceInterface device) {
		deviceMap.put(deviceId, device);
	}

	public GatewayActionHandlerSample() {
	}

	/**
	 * If reboot attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	@Override
	public void handleReboot(DeviceAction action) {
		
		System.out.println(" --> Reboot action requested for device " + action.getDeviceId());
		DeviceInterface device = this.deviceMap.get(action.getDeviceId());
		if(device != null) {
			device.reboot(action);
		} else if(action.getDeviceId().equals(gatewayDeviceId)) {
			ProcessBuilder processBuilder = null;
			Process p = null;
			
			String osname = System.getProperty("os.name");
			
			if(osname.startsWith("Windows")) {
				processBuilder = new ProcessBuilder("shutdown", "-r");
			} else {
				processBuilder = new ProcessBuilder("sudo", "shutdown", "-r", "now");
			}
	
			processBuilder.redirectErrorStream(true);
			processBuilder.inheritIO();
			
			
			boolean status = false;
			try {
				p = processBuilder.start();
				// wait for say 2 minutes before giving it up
				status = waitForCompletion(p, 2);
				System.out.println("Executed restart command "+status);
			} catch (IOException e) {
				action.setMessage(e.getMessage());
			} catch (InterruptedException e) {
				action.setMessage(e.getMessage());
			}
			
			System.out.println("Executed restart command status ("+status+")");
			if(status == false) {
				action.setStatus(DeviceAction.Status.FAILED);
			}
		} else {
			System.err.println("Device "+action.getDeviceId() +" not found");
			action.setStatus(DeviceAction.Status.FAILED);
			return;
		}
	}

	/**
	 * If factory reset attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the factory reset is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	@Override
	public void handleFactoryReset(DeviceAction action) {
		
		System.out.println(" --> factory reset requested for device " + action.getDeviceId());
		/**
		 * This sample doesn't support factory reset, so respond accordingly
		 */
		action.setStatus(DeviceAction.Status.UNSUPPORTED);
		System.out.println("<-- factory reset not supported");
	}
	
	/**
	 * Since JDK7 doesn't take any timeout parameter, we provide an workaround
	 * that wakes up every second and checks for the completion status of the process.
	 * @param process
	 * @param minutes
	 * @return
	 * @throws InterruptedException 
	 */
	private static boolean waitForCompletion(Process process, int minutes) throws InterruptedException {
		long timeToWait = (60 * minutes);
		
		int exitValue = -1;
		for(int i = 0; i < timeToWait; i++) {
			try {
				exitValue = process.exitValue();
			} catch(IllegalThreadStateException  e) {
				// Process is still running
			}
			if(exitValue == 0) {
				return true;
			}
			Thread.sleep(1000);
		}
		// Destroy the process forcibly
		try {
			process.destroy();
		} catch(Exception e) {}
	
		return false;
	}
	
	public void setGatewayDeviceId(String gwDeviceId) {
		this.gatewayDeviceId = gwDeviceId;
	}

}

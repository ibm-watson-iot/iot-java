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
 Sathiskumar Palaniappan - Modified to include Threadpool to support multiple 
                           downloads/updates at the same time (for attached devices)
 *****************************************************************************
 *
 */
package com.ibm.iotf.sample.devicemgmt.gateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.ibm.iotf.devicemgmt.DeviceAction;
import com.ibm.iotf.devicemgmt.DeviceAction.Status;
import com.ibm.iotf.devicemgmt.DeviceActionHandler;
import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;
import com.ibm.iotf.sample.client.gateway.DeviceInterface;
/**
 * This sample Gateway action handler demonstrates how one can reboot the Gateway and the 
 * attached devices. 
 * 
 */
public class GatewayActionHandlerSample extends DeviceActionHandler {
	
	private Map<String, DeviceInterface> deviceMap = new HashMap<String, DeviceInterface>();
	private ManagedGateway gateway;
	
	public void addDeviceInterface(String clientId, DeviceInterface device) {
		deviceMap.put(clientId, device);
	}
	
	/**
	 * If reboot attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the reboot is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	private static class RebootTask implements Runnable {
		
		private DeviceAction deviceAction;
		private GatewayActionHandlerSample handler;

		public RebootTask(DeviceAction deviceAction, GatewayActionHandlerSample handler) {
			this.deviceAction = deviceAction;
			this.handler = handler;
		}

		@Override
		public void run() {
			System.out.println(" --> Reboot action requested for device " + deviceAction.getDeviceId());
			DeviceInterface device = handler.getDevice(handler.getKey(deviceAction));
			if(device != null) {
				device.reboot(deviceAction);
			} else if(handler.isGateway(deviceAction)) {
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
				String msg = null;
				try {
					p = processBuilder.start();
					// wait for say 2 minutes before giving it up
					status = waitForCompletion(p, 2);
					System.out.println("Executed restart command "+status);
				} catch (IOException e) {
					msg = e.getMessage();
				} catch (InterruptedException e) {
					msg = e.getMessage();
				}
				
				System.out.println("Executed restart command status ("+status+")");
				if(status == false) {
					deviceAction.setStatus(DeviceAction.Status.FAILED, msg);
				}
			} else {
				System.err.println("Device "+deviceAction.getDeviceId() +" not found");
				deviceAction.setStatus(DeviceAction.Status.FAILED, "Device Not found");
				return;
			}
		}
		
	}
	
	/**
	 * If factory reset attempt fails, set status to FAILED and the "message" 
	 * field should be set accordingly, if the factory reset is not supported, 
	 * set status to NOTSUPPORTED and optionally set "message" accordingly
	 */
	private static class FactoryResetTask implements Runnable {
		
		private DeviceAction deviceAction;
		private GatewayActionHandlerSample handler;

		public FactoryResetTask(DeviceAction deviceAction, GatewayActionHandlerSample handler) {
			this.deviceAction = deviceAction;
			this.handler = handler;
		}

		@Override
		public void run() {
			System.out.println(" --> factory reset requested for device " + deviceAction.getDeviceId());
			/**
			 * This sample doesn't support factory reset, so respond accordingly
			 */
			deviceAction.setStatus(DeviceAction.Status.UNSUPPORTED);
			System.out.println("<-- factory reset not supported");
		}
	}
	
	private ExecutorService threadPoolExecutor = null;
	
	public DeviceInterface getDevice(String key) {
		return this.deviceMap.get(key);
	}

	public boolean isGateway(DeviceAction deviceAction) {
		if(deviceAction != null && deviceAction.getTypeId().equals(this.gateway.getGWDeviceType()) 
				&& deviceAction.getDeviceId().equals(this.gateway.getGWDeviceId())) {
			return true;
		}
		return false;
	}

	/**
	 * Since JDK7 doesn't take any timeout parameter, we provide an workaround
	 * that wakes up every second and checks for the completion status of the process.
	 * @param process
	 * @param minutes
	 * @return
	 * @throws InterruptedException 
	 */
	public static boolean waitForCompletion(Process process, int minutes) throws InterruptedException {
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

	public void setGateway(ManagedGateway gwClient) {
		this.gateway = gwClient;
	}

	@Override
	public void handleReboot(DeviceAction action) {
		// set the support before handing over to the pool
		action.setStatus(Status.ACCEPTED);
		RebootTask task = new RebootTask(action, this);
		threadPoolExecutor.execute(task);
		
	}

	@Override
	public void handleFactoryReset(DeviceAction action) {
		/*FactoryResetTask task = new FactoryResetTask(action, this);
		threadPoolExecutor.execute(task);*/
		
		// As the sample doesn't support factory Rest, it just sends unsupported message now
		action.setStatus(Status.UNSUPPORTED);
		// Optionally set a message
		action.setMessage("Not supported at the moment");
	}

	public void setExecutor(ExecutorService threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}
	
	/**
	 * Let us use the WIoTP client Id as the key to identify the device
	 * @return
	 */
	private String getKey(DeviceAction deviceAction) {
		return new StringBuilder("d:").
				append(this.gateway.getOrgId()).
				append(':').
				append(deviceAction.getTypeId()).
				append(':').
				append(deviceAction.getDeviceId()).toString();
	}


}

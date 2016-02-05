package com.ibm.iotf.sample.devicemgmt.gateway.task;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.devicemgmt.gateway.ManagedGateway;

/**
 * 
 * Timer task that sends the manage command before the lifetime
 * expires, otherwise the Gateway or device will become dormant and can't 
 * participate in device management actions
 * 
 *  i.e, if you create the task instance without the deviceType and Id, then its considered 
 *  for Gateway.
 *
 */
public class ManageTask implements Runnable {
	private long lifetime;
	private ManagedGateway managedGateway;
	private String deviceType;
	private String deviceId;
	
	public ManageTask(ManagedGateway managedGateway, String deviceType, String deviceId, int lifetime) {
		this.lifetime = lifetime;
		this.managedGateway = managedGateway;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
	}
	
	public ManageTask(ManagedGateway managedGateway, long lifetime) {
		this.lifetime = lifetime;
		this.managedGateway = managedGateway;
	}
	
	@Override
	public void run() {
		try {
			boolean status = false;
			
			if(deviceId == null) {
				status = managedGateway.sendGatewayManageRequest(lifetime, false, true);
				System.out.println("Resent the manage request for Gateway at time "+new Date() +
						" status("+status+")");
			} else {
				status = managedGateway.sendDeviceManageRequest(this.deviceType, this.deviceId, lifetime, false, true);
				System.out.println("Resent the manage request for device "+ deviceId +" at time "+new Date() +
						" status("+status+")");
			}
			
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
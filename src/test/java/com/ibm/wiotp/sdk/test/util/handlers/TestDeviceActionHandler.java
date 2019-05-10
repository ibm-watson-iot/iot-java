package com.ibm.wiotp.sdk.test.util.handlers;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.wiotp.sdk.device.ManagedDevice;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.DeviceActionHandler;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction.Status;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestDeviceActionHandler extends DeviceActionHandler {
	
	private static final String CLASS_NAME = TestDeviceActionHandler.class.getName();
	private ManagedDevice dmClient = null;
	private boolean reboot = false;
	private boolean factoryReset = false;
	
	public TestDeviceActionHandler(ManagedDevice dmClient) {
		this.dmClient = dmClient;
	}
	
	public boolean rebooted() { return reboot; }
	public boolean factoryReset() { return factoryReset; }
	
	@Override
	public void handleReboot(DeviceAction action) {
		final String METHOD = "handleReboot";
		action.setStatus(Status.ACCEPTED);
		LoggerUtility.info(CLASS_NAME, METHOD, "reboot initiated.");
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					boolean status = dmClient.sendManageRequest(0,  true, true);
					LoggerUtility.info(CLASS_NAME, METHOD, "sent a manage request : " + status);
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				reboot = true;
			}
		}.start();
	}

	@Override
	public void handleFactoryReset(DeviceAction action) {
		final String METHOD = "handleFactoryReset";
		LoggerUtility.info(CLASS_NAME, METHOD, "factory reset initiated.");
		action.setStatus(Status.ACCEPTED);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					boolean status = dmClient.sendManageRequest(0,  true, true);
					LoggerUtility.info(CLASS_NAME, METHOD, "sent a manage request : " + status);
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				factoryReset = true;
			}

		}.start();
	}

}

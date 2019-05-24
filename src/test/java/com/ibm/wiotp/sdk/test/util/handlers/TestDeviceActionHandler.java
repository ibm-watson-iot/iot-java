package com.ibm.wiotp.sdk.test.util.handlers;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wiotp.sdk.device.ManagedDevice;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction;
import com.ibm.wiotp.sdk.devicemgmt.DeviceActionHandler;
import com.ibm.wiotp.sdk.devicemgmt.DeviceAction.Status;

public class TestDeviceActionHandler extends DeviceActionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TestDeviceActionHandler.class);
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
		action.setStatus(Status.ACCEPTED);
		LOG.info("reboot initiated.");
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					boolean status = dmClient.sendManageRequest(0,  true, true);
					LOG.info("sent a manage request : " + status);
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
		LOG.info("factory reset initiated.");
		action.setStatus(Status.ACCEPTED);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
					boolean status = dmClient.sendManageRequest(0,  true, true);
					LOG.info("sent a manage request : " + status);
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

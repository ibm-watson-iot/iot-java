package com.ibm.iotcloud.samples.sigar;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.ibm.iotcloud.client.Device;

public class SigarIoTDevice implements Runnable {

	private boolean quit = false;
	private Sigar sigar = null;
	
	private final String deviceId;	
	private final String account;
	private final String username;
	private final String password;

	public SigarIoTDevice(String account, String username, String password) {
		sigar = new Sigar();
		
		this.account = account;
		this.username = username;
		this.password = password;
		
		// Try to obtain a(ny) MAC Address on this system
		NetInterfaceConfig config = null;
		String macAddress = null;
		try {
			config = sigar.getNetInterfaceConfig(null);
			macAddress = config.getHwaddr();
		} catch (SigarException e) {
			System.out.println("Problem getting MAC address: " + e.toString());
		}
		
		// If we couldn't find a MAC address then we can't assign a Device ID
		if (macAddress == null) {
			deviceId = null;
		} else {
			deviceId = macAddress.toLowerCase().replaceAll(":", "");
		}

	}

	public void quit() {
		this.quit = true;
	}
	
	public void run() {
		if (deviceId == null) {
			System.out.println("Unable to run without a device ID");
		} else {
			try {
				// Instantiate the device
				Device device;
				if (account != null) {
					device = new Device(deviceId, account, username, password);
				}
				else {
					device = new Device(deviceId);
				}
				
				// Send a dataset every 1 second, until we are told to quit 
				while (!quit) {
					device.send("sigar", SigarData.create(sigar));
					Thread.sleep(1000);
				}
				
				// Once told to stop, cleanly disconnect from the service
				device.disconnect();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getDeviceId() {
		return deviceId;
	}
}
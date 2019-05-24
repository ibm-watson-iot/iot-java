package com.ibm.wiotp.samples.oshi;

import java.util.Scanner;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.codecs.JsonCodec;
import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.device.config.DeviceConfig;
import com.ibm.wiotp.sdk.device.config.DeviceConfigIdentity;
import com.ibm.wiotp.sdk.device.config.DeviceConfigOptions;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;


public class OshiDevice implements Runnable {
	
	private boolean quit = false;
	private SystemInfo si = null;
	protected DeviceClient client;
	
	public OshiDevice(DeviceConfig config) throws Exception {
		this.si = new SystemInfo();
		this.client = new DeviceClient(config);
		this.client.registerCodec(new JsonCodec());
	}

    public JsonObject createOshiData() {
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        double loadAverage = processor.getSystemCpuLoad() * 100;
        
        GlobalMemory memory = hal.getMemory();
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();
        long usedMemory = totalMemory - availableMemory;
        double memoryUtilization = (usedMemory / (double) totalMemory) * 100;
        
        JsonObject json = new JsonObject();
        json.addProperty("memory",  memoryUtilization);
        json.addProperty("cpu",  loadAverage);
        return json;
    }

	public void quit() {
		this.quit = true;
	}
	
	public void run() {
		try {
			client.connect();
			// Send a dataset every 1 second, until we are told to quit
			while (!quit) {
				JsonObject data = createOshiData();
				client.publishEvent("oshi", data, 0);
				Thread.sleep(1000);
			}
			
			// Once told to stop, cleanly disconnect from the service
			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		DeviceConfig config = null;
		if (args[0].equals("--quickstart")) {
			SystemInfo si = new SystemInfo();
			String macAddress = si.getHardware().getNetworkIFs()[0].getMacaddr().replace(":", "");
			String alternateDeviceId = UUID.randomUUID().toString();
			DeviceConfigIdentity identity = new DeviceConfigIdentity("quickstart", "iotsigar", macAddress);
			DeviceConfigOptions options = new DeviceConfigOptions();
			options.mqtt.port = 1883;
			
			config = new DeviceConfig(identity, null, options);
		} 
		else {
			config = DeviceConfig.generateFromEnv();
		}
	    // Start the device thread
		OshiDevice d;

		d = new OshiDevice(config);
		Thread t1 = new Thread(d);
		t1.start();

		if (args[0].equals("--quickstart")) {
			System.out.println("Welcome to IBM Watson IoT Platform Quickstart, view a vizualization of live data from this device at the URL below:");
			System.out.println("https://quickstart.internetofthings.ibmcloud.com/#/device/" + config.identity.deviceId + "/sensor/");
			System.out.println("");
		}
		System.out.println("(Press <enter> to disconnect)");

		// Wait for <enter>
		Scanner sc = new Scanner(System.in);
		try {
			sc.nextLine();
		} catch (java.util.NoSuchElementException e) {
			// It's okay, it just means that you can't press <enter> to disconnect because we can't read from System.in
		}
		sc.close();
		System.out.println("Closed connection to IBM Watson IoT Platform");
		
		// Let the device thread know it can terminate
		d.quit();
	}
	
}
package com.ibm.wiotp.sdk.samples.sigar;

import java.io.FileInputStream;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.hyperic.sigar.Sigar;

import com.ibm.wiotp.sdk.device.DeviceClient;
import com.ibm.wiotp.sdk.device.config.DeviceConfig;


public class SigarIoTDevice implements Runnable {
	
	private boolean quit = false;
	private Sigar sigar = null;
	protected DeviceClient client;
	
	public SigarIoTDevice() throws Exception {
		this.sigar = new Sigar();
		DeviceConfig config = DeviceConfig.generateFromEnv(); 
		this.client = new DeviceClient(config);
	}



	public void quit() {
		this.quit = true;
	}
	
	public void run() {
		try {
			client.connect();
			System.out.println("Start publishing event every second...");
			// Send a dataset every 1 second, until we are told to quit
			while (!quit) {
				SigarData data = SigarData.create(sigar);
				System.out.println(data);
				client.publishEvent("sigar", data, 0);
				Thread.sleep(1000);
			}
			
			// Once told to stop, cleanly disconnect from the service
			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		// Load custom logging properties file
	    try {
			FileInputStream fis =  new FileInputStream("logging.properties");
			LogManager.getLogManager().readConfiguration(fis);
		} catch (SecurityException e) {}
	    
		
	    // Start the device thread
		SigarIoTDevice d;

		d = new SigarIoTDevice();
		Thread t1 = new Thread(d);
		t1.start();

		System.out.println("Connected successfully - Your device ID is " + d.client.getConfig().getDeviceId());
		System.out.println(" * Organization: " + d.client.getConfig().getOrgId());			
		
		System.out.println("");
		System.out.println("(Press <enter> to disconnect)");

		// Wait for <enter>
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		
		System.out.println("Closing connection to the IBM Internet of Things Cloud service");
		// Let the device thread know it can terminate
		d.quit();
	}
	
}
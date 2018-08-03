package com.ibm.iotf.samples.sigar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;


public class SigarIoTApp implements Runnable {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	
	private volatile boolean quit = false;
	protected ApplicationClient client;

	private String deviceType;
	private String deviceId;
	
	public SigarIoTApp(String filename) throws Exception {
		
		/**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(SigarIoTApp.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		this.client = new ApplicationClient(props);
		
		/**
		 * Get the Device Type and Device Id from which the application will listen for the events
		 */
		this.deviceType = trimedValue(props.getProperty("Device-Type"));
		this.deviceId = trimedValue(props.getProperty("Device-ID"));
	}
	
	public void quit() {
		this.quit = true;
	}
	
	public void run() {
		try {
			try {
				client.connect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			client.setEventCallback(new MyEventCallback());
			client.setStatusCallback(new MyStatusCallback());
			client.subscribeToDeviceStatus();
			client.subscribeToDeviceEvents(this.deviceType, this.deviceId);
			while (!quit) {
				Thread.sleep(1000);
			}
			
			System.out.println("Closing connection to the IBM Watson IoT Platform");
			// Once told to stop, cleanly disconnect from the service
			client.disconnect();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class MyEventCallback implements EventCallback {

		@Override
		public void processEvent(Event e) {
			System.out.println(e.getDeviceId() + "--" + e.getPayload());
		}
		
		@Override
		public void processCommand(Command cmd) {
			
			
		}
		
	}
	

	private class MyStatusCallback implements StatusCallback {

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println(status.getPayload());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			System.out.println(status.getPayload());
		}
	}
	
	public static void main(String[] args) throws Exception {
		createShutDownHook();
		// Load custom logging properties file
	    try {
			FileInputStream fis =  new FileInputStream("logging.properties");
			LogManager.getLogManager().readConfiguration(fis);
		} catch (SecurityException e) {
		} catch (IOException e) {
		}
	    
	    // Start the application thread
		SigarIoTApp a = new SigarIoTApp(PROPERTIES_FILE_NAME);
		
		
		Thread t1 = new Thread(a);
		t1.start();

		System.out.println(" * Organization: " + a.client.getOrgId());			
		System.out.println("Connected successfully - Your App ID is " + a.client.getAppId());
		System.out.println("");
		System.out.println("(Press <enter> to disconnect)");

		// Wait for <enter>
		Scanner sc = new Scanner(System.in);
		try {
			sc.nextLine();
			sc.close();
		} catch(Exception e) {}
		
		System.out.println("Closing connection to the IBM Watson IoT Platform");
		// Let the App thread know it can terminate
		a.quit();
	}
	
	private static void createShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println();
				System.out.println("Thanks for using the application");
				System.out.println("Exiting...");
			}
		}));

	}

	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}

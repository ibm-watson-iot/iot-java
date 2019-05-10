package com.ibm.wiotp.sdk.samples.sigar;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.ApplicationClient;
import com.ibm.wiotp.sdk.app.messages.ApplicationStatus;
import com.ibm.wiotp.sdk.app.messages.Command;
import com.ibm.wiotp.sdk.app.messages.DeviceStatus;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.codecs.JsonCodec;
import com.ibm.wiotp.sdk.app.callbacks.CommandCallback;
import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.callbacks.StatusCallback;
import com.ibm.wiotp.sdk.app.config.ApplicationConfig;


public class SigarIoTApp implements Runnable {
	
	private volatile boolean quit = false;
	protected ApplicationClient client;

	public SigarIoTApp() throws Exception {
		ApplicationConfig config = ApplicationConfig.generateFromEnv();
		this.client = new ApplicationClient(config);
	}
	
	public void quit() {
		this.quit = true;
	}
	
	public void run() {
		try {
			client.connect();
			
			// Register callbacks
			client.registerCommandCallback(new MyCommandCallback());
			client.registerEventCallback(new MyEventCallback());
			client.setStatusCallback(new MyStatusCallback());
			client.registerCodec(new JsonCodec());
			
			// Create subscriptions for json events and commands only because we only have a json codec registered
			client.subscribeToDeviceCommands("+", "+", "+", "json");
			client.subscribeToDeviceEvents("+", "+", "+", "json");
			client.subscribeToDeviceStatus("+", "+");
			
			while (!quit) {
				Thread.sleep(1000);
			}
			System.out.println("Closing connection to the IBM Watson IoT Platform");
			
			// Once told to stop, cleanly disconnect from WIoTP
			client.disconnect();
		} catch (InterruptedException | KeyManagementException | NoSuchAlgorithmException | MqttException e) {
			e.printStackTrace();
		}
	}
	
	private class MyEventCallback implements EventCallback<JsonObject> {
		@Override
		public void processEvent(Event<JsonObject> evt) {
			System.out.println(evt.toString());
		}

		@Override
		public Class<JsonObject> getMessageClass() {
			return JsonObject.class;
		}
	}

	private class MyCommandCallback implements CommandCallback<JsonObject> {
		@Override
		public void processCommand(Command<JsonObject> cmd) {
			System.out.println(cmd.toString());			
		}
		
		@Override
		public Class<JsonObject> getMessageClass() {
			return JsonObject.class;
		}
	}

	private class MyStatusCallback implements StatusCallback {

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println(status.toString());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			System.out.println(status.toString());
		}
	}
	
	public static void main(String[] args) throws Exception {
		createShutDownHook();
		// Load custom logging properties file
	    try {
			FileInputStream fis =  new FileInputStream("logging.properties");
			LogManager.getLogManager().readConfiguration(fis);
		} catch (SecurityException | IOException e) {
		}
	    
	    // Start the application thread
		SigarIoTApp a = new SigarIoTApp();
		
		Thread t1 = new Thread(a);
		t1.start();

		System.out.println(" * Organization: " + a.client.getConfig().getOrgId());			
		// TODO: Add this to the interface
		// System.out.println("Connected successfully - Your App ID is " + a.client.getConfig().getAppId());
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
				System.out.println("Exiting...");
			}
		}));

	}
}

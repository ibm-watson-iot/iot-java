package com.ibm.iotf.sample.dme.device;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.DeviceData;
import com.ibm.iotf.devicemgmt.device.ManagedDevice;
import com.ibm.iotf.sample.dme.SystemObject;

/**
 * This is a simple example to showcase the Device Management Extension(DME) for Device.
 * 
 * The example publishes the event for a device for every one second, and registers
 * a custom action handler that will listen for the custom action - the custom action changes
 * the event publish interval time.
 *
 */
public class SampleDMEDevice {
	
	private final static String PROPERTIES_FILE_NAME = "/DMDeviceSample.properties";
	private ManagedDevice dmClient;
	private MyCustomActionHandler callBack;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting sample DM agent...");
		
		SampleDMEDevice sample = new SampleDMEDevice();
		try {
			sample.createManagedClient(PROPERTIES_FILE_NAME);
			sample.connect();
			sample.addDMEHandler();
			sample.publishEvents();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.err.flush();
		} finally {
			sample.terminate();
		}
		
		System.out.println(" Exiting...");
	}
	
	/**
	 * Publish the event based on the interval set by the application.
	 * The interval to be changed using the custom DM action.
	 */
	private void publishEvents() {
		SystemObject obj = new SystemObject();
		while(true) {
			try {
				//Generate a JSON object of the event to be published
				JsonObject event = new JsonObject();
				event.addProperty("name", SystemObject.getName());
				event.addProperty("cpu",  obj.getProcessCpuLoad());
				event.addProperty("mem",  obj.getMemoryUsed());
				
				this.dmClient.publishEvent("load", event);
				System.out.println(event + "  "+new Date());
				Thread.sleep(callBack.getPublishInterval());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	private void addDMEHandler() throws Exception {
		this.callBack = new MyCustomActionHandler();
		this.dmClient.addCustomActionHandler(callBack);
		Thread t = new Thread(callBack);
		t.start();
	}

	/** This method builds the device objects required to create the
	 * ManagedClient
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	private void createManagedClient(String propertiesFile) throws Exception {
		/**
		 * Load device properties
		 */
		Properties deviceProps = new Properties();
		try {
			deviceProps.load(SampleDMEDevice.class.getResourceAsStream(propertiesFile));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}
		
		DeviceData deviceData = new DeviceData.Builder().build();
		
		// Options to connect to Watson IoT Platform
		Properties options = new Properties();
		
		options.setProperty("Organization-ID", trimedValue(deviceProps.getProperty("Organization-ID")));
		options.setProperty("Device-Type", trimedValue(deviceProps.getProperty("Device-Type")));
		options.setProperty("Device-ID", trimedValue(deviceProps.getProperty("Device-ID")));
		options.setProperty("Authentication-Method", trimedValue(deviceProps.getProperty("Authentication-Method")));
		options.setProperty("Authentication-Token", trimedValue(deviceProps.getProperty("Authentication-Token")));
				
		dmClient = new ManagedDevice(options, deviceData);
		
	}
	
	/**
	 * This method connects the device to the Watson IoT Platform
	 */
	private void connect() throws Exception {		
		dmClient.connect();
		dmClient.sendManageRequest(0, false, false, "example-dme-actions-v1");
	}
	
	private void terminate() throws Exception {
		if(this.dmClient != null) {
			dmClient.disconnect();
			System.out.println("Bye !!");
			System.exit(-1);
		}
	}
		
	private String trimedValue(String value) {
		if(value == null || value == "") {
			return "";
		} else {
			return value.trim();
		}
	}
}


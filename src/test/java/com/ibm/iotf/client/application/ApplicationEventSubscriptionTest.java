package com.ibm.iotf.client.application;

import java.io.IOException;
import java.util.Properties;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;
import com.ibm.iotf.client.device.DeviceClient;

import junit.framework.TestCase;

/**
 * This test verifies that the event & device connectivity status are successfully received by the
 * application.
 *
 */
public class ApplicationEventSubscriptionTest extends TestCase{
	
	private final static String DEVICE_PROPERTIES_FILE = "/device.properties";
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	
	/**
	 * This method publishes a device event such that the application will receive the same
	 * and verifies that the event is same.
	 */
	private void deviceEventPublish() {
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(ApplicationEventSubscriptionTest.class.getResourceAsStream(DEVICE_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(props);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
			
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  90);
		event.addProperty("mem",  70);
					
		myClient.publishEvent("blink", event);
		myClient.disconnect();
	}
	
	@Test
	public void testSubscribe(){
		/**
		 * Load device properties
		 */
		Properties props = new Properties();
		try {
			props.load(ApplicationEventSubscriptionTest.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
			
		ApplicationClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new ApplicationClient(props);
			myClient.connect();
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not udpated, just ignore;
			return;
		}
		
		/**
		 * Get the Device Type and Device Id on behalf the application will publish the event
		 */
		String deviceType = trimedValue(props.getProperty("Device-Type"));
		String deviceId = trimedValue(props.getProperty("Device-ID"));
		
		// Add event callback
		MyEventCallback eventbk = new MyEventCallback();
		myClient.setEventCallback(eventbk);
		
		// Add status callback
		MyStatusCallback statusbk = new MyStatusCallback();
		myClient.setStatusCallback(statusbk);
		
		// Subscribe to device events and device connectivity status
		myClient.subscribeToDeviceStatus();
		myClient.subscribeToDeviceEvents(deviceType, deviceId);
		
		this.deviceEventPublish();
		
		int count = 0;
		// wait for sometime before checking
		while(eventbk.eventReceived == false && count++ <= 5) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		myClient.disconnect();
		assertTrue("Device Event is not received by application", eventbk.eventReceived);
		assertTrue("Device connectivity status is not received by application", statusbk.statusReceived);
	}

	private static class MyEventCallback implements EventCallback {
		private boolean eventReceived = false;

		@Override
		public void processEvent(Event e) {
			eventReceived = true;
			System.out.println("Event " + e.getPayload());
		}

		@Override
		public void processCommand(Command cmd) {
			System.out.println("Command " + cmd.getPayload());			
		}		
	}
	
	private static class MyStatusCallback implements StatusCallback {
		private boolean statusReceived = false;

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println("Application Status = " + status.getPayload());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			statusReceived = true;
			System.out.println("Device Status = " + status.getPayload());
		}
	}

	private static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

}

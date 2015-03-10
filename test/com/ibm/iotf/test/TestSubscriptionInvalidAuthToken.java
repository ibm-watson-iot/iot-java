package com.ibm.iotf.client.device.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;




import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.ApplicationStatus;
import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.DeviceStatus;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.app.StatusCallback;

import junit.framework.TestCase;

public class TestSubscriptionInvalidAuthToken extends TestCase{
	
	@Test
	public void testSubscribe(){
		try{
		Properties opt = new Properties();
		ApplicationClient client = null;
		
		
		
			
		File file = new File("test/test.properties");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();

		
		

		opt.put("auth-key", properties.getProperty("authKey"));
		opt.put("auth-token", "authToken");
		opt.put("id", properties.getProperty("id"));
		opt.put("auth-method", properties.getProperty("auth-method"));
	
	
		client = new ApplicationClient(opt);
		client.connect();
		
		
		client.setEventCallback(new MyEventCallback());
		client.setStatusCallback(new MyStatusCallback());
		client.subscribeToDeviceStatus();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		}

	private class MyEventCallback implements EventCallback {

		@Override
		public void processEvent(Event e) {
			System.out.println("Event " + e.getPayload());
		}

		@Override
		public void processCommand(Command cmd) {
			System.out.println("Command " + cmd.getPayload());			
		}


		
	}
	
	private class MyStatusCallback implements StatusCallback {

		@Override
		public void processApplicationStatus(ApplicationStatus status) {
			System.out.println("Application Status = " + status.getPayload());
		}

		@Override
		public void processDeviceStatus(DeviceStatus status) {
			System.out.println("Device Status = " + status.getPayload());
		}
	}



}

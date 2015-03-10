package com.ibm.iotf.client.test;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Properties;


import com.ibm.iotf.client.api.DeviceFactory;
import com.ibm.iotf.client.api.HistoricalEvent;

import junit.framework.TestCase;

public class TestHistoricalEvents extends TestCase {
	
	public void testHistoricalEvent(){
	
		try{
			Properties opt = new Properties();
		
			
			File file = new File("test/test.properties");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			
			
			
			
			opt.put("authKey",  properties.getProperty("authKey"));
			opt.put("authToken", properties.getProperty("authToken"));
		DeviceFactory factory = new DeviceFactory(opt);
		
		HistoricalEvent[] event = factory.getHistoricalEvents(properties.getProperty("deviceType"), properties.getProperty("deviceId"));
		
		
		
			assertEquals("HistoricalEvents received", event.length >0, factory.getHistoricalEvents(properties.getProperty("deviceType"), properties.getProperty("deviceId")));
			assertEquals("No HistoricalEvents received", event.length <=0, factory.getHistoricalEvents(properties.getProperty("deviceType"), properties.getProperty("deviceId")));
			
			
		
		
			
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
		}



}

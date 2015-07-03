package com.ibm.iotf.sample.client;

import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;

public class SimpleDevice {

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.setProperty("org", "uguhsp");
		properties.setProperty("type", "iotsample-arduino");
		properties.setProperty("auth-method", "token");
		properties.setProperty("auth-token", "somepassword");
		properties.setProperty("id", "00aabbccde03");
		
		DeviceClient myClient = null;
		try {
			myClient = new DeviceClient(properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myClient.connect();
		
		JsonObject event = new JsonObject();
		event.addProperty("name", "foo");
		event.addProperty("cpu",  60);
		event.addProperty("mem",  50);
		
		myClient.publishEvent("status", event, 2);
		myClient.disconnect();
	}
}

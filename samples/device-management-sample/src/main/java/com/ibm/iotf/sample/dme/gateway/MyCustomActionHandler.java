package com.ibm.iotf.sample.dme.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.CustomAction;
import com.ibm.iotf.client.CustomAction.Status;
import com.ibm.iotf.devicemgmt.CustomActionHandler;

public class MyCustomActionHandler extends CustomActionHandler implements Runnable {

	// A queue to hold & process the commands for smooth handling of MQTT messages
	private BlockingQueue<CustomAction> queue = new LinkedBlockingQueue<CustomAction>();
	private Map<String, Long> intervalMap = new HashMap<String, Long>();

	@Override
	public void run() {
		while(true) {
			CustomAction action = null;
			try {
				action = queue.take();
				System.out.println(" "+action.getActionId()+ " "+action.getPayload());
				JsonArray fields = action.getPayload().get("d").getAsJsonObject().get("fields").getAsJsonArray();
				for(JsonElement field : fields) {
					JsonObject fieldObj = field.getAsJsonObject();
					if("PublishInterval".equals(fieldObj.get("field").getAsString())) {
						long val = fieldObj.get("value").getAsLong();
						String key = action.getTypeId() + ":" + action.getDeviceId();
						long publishInterval = val * 1000;
						intervalMap.put(key, publishInterval);
						System.out.println("Updated the publish interval to "+val);
					}
				}
				action.setStatus(Status.OK);
				
			} catch (InterruptedException e) {}
		}
	}

	public long getPublishInterval(String deviceType, String deviceId) {
		String key = deviceType + ":" + deviceId;
		Long val = intervalMap.get(key);
		if(val == null) {
			return 1000; // default is 1 second
		} else {
			return val.longValue();
		}
	}

	@Override
	public void handleCustomAction(CustomAction action) {
		try {
			queue.put(action);
			} catch (InterruptedException e) {
		}	
		
	}
}
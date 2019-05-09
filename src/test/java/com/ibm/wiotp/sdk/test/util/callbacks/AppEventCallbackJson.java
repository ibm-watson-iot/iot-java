package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class AppEventCallbackJson implements EventCallback<JsonObject> {

	Event<JsonObject> event = null;
	ArrayList<Event<JsonObject>> allEvents = new ArrayList<Event<JsonObject>>();
	
	private static final String CLASS_NAME = DeviceCommandCallbackJson.class.getName();
	
	@Override
	public void processEvent(Event<JsonObject> evt) {
		event = evt;
		allEvents.add(event);
		LoggerUtility.info(CLASS_NAME, "processEvent", "Received event, name = "+evt.getEventId() + ", format = " + evt.getFormat() + ", Payload = "+evt.getData().toString());
	}

	public void clear() {
		event = null;
		allEvents = null;
	}
	
	public Event<JsonObject> getEvent() { return event; }
	public ArrayList<Event<JsonObject>> getAllEvents() { return allEvents; }

	@Override
	public Class<JsonObject> getMessageClass() {
		return JsonObject.class;
	}
}

package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class AppEventCallbackUtf8 implements EventCallback<String> {

	Event<String> event = null;
	ArrayList<Event<String>> allEvents = new ArrayList<Event<String>>();
	
	private static final String CLASS_NAME = DeviceCommandCallbackJson.class.getName();
	
	@Override
	public void processEvent(Event<String> evt) {
		event = evt;
		allEvents.add(event);
		LoggerUtility.info(CLASS_NAME, "processEvent", "Received event, name = "+evt.getEventId() + ", format = " + evt.getFormat() + ", Payload = "+evt.getData());
	}

	public void clear() {
		event = null;
		allEvents = null;
	}
	
	public Event<String> getEvent() { return event; }
	public ArrayList<Event<String>> getAllEvents() { return allEvents; }

	@Override
	public Class<String> getMessageClass() {
		return String.class;
	}
}

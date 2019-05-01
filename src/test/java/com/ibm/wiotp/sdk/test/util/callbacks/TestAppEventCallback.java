package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.messages.Event;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestAppEventCallback implements EventCallback {

	Event event = null;
	ArrayList<Event> allEvents = null;
	
	private static final String CLASS_NAME = TestDeviceCommandCallback.class.getName();
	
	@Override
	public void processEvent(Event evt) {
		event = evt;
		if (allEvents == null) {
			allEvents = new ArrayList<Event>();
		}
		allEvents.add(event);
		LoggerUtility.info(CLASS_NAME, "processEvent", "Received event, name = "+evt.getEventId() + ", format = " + evt.getFormat() + ", Payload = "+evt.getPayload());
	}

	public void clear() {
		event = null;
		allEvents = null;
	}
	
	public Event getEvent() { return event; }
	public ArrayList<Event> getAllEvents() { return allEvents; }
}

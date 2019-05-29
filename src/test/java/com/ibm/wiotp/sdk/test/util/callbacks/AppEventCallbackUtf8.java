package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.messages.Event;

public class AppEventCallbackUtf8 implements EventCallback<String> {
	private static final Logger LOG = LoggerFactory.getLogger(AppEventCallbackUtf8.class);

	Event<String> event = null;
	ArrayList<Event<String>> allEvents = new ArrayList<Event<String>>();
	
	@Override
	public void processEvent(Event<String> evt) {
		event = evt;
		allEvents.add(event);
		LOG.info("Received event, name = "+evt.getEventId() + ", format = " + evt.getFormat() + ", Payload = "+evt.getData());
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

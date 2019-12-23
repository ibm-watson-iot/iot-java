package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.callbacks.EventCallback;
import com.ibm.wiotp.sdk.app.messages.Event;

public class AppEventCallbackJson implements EventCallback<JsonObject> {
	private static final Logger LOG = LoggerFactory.getLogger(AppEventCallbackJson.class);

	Event<JsonObject> event = null;
	ArrayList<Event<JsonObject>> allEvents = new ArrayList<Event<JsonObject>>();

	@Override
	public void processEvent(Event<JsonObject> evt) {
		event = evt;
		allEvents.add(event);
		LOG.info("Received event, name = " + evt.getEventId() + ", format = " + evt.getFormat() + ", Payload = "
				+ evt.getData().toString());
	}

	public void clear() {
		event = null;
		allEvents = null;
	}

	public Event<JsonObject> getEvent() {
		return event;
	}

	public ArrayList<Event<JsonObject>> getAllEvents() {
		return allEvents;
	}

	@Override
	public Class<JsonObject> getMessageClass() {
		return JsonObject.class;
	}
}

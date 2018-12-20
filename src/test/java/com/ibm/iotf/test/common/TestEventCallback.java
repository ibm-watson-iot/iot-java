package com.ibm.iotf.test.common;

import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;

public class TestEventCallback implements EventCallback {

	Event event = null;
	Command command = null;
	
	@Override
	public void processEvent(Event evt) {
		this.event = evt;
	}

	@Override
	public void processCommand(Command cmd) {
		this.command = cmd;
	}
	
	public void clear() {
		event = null;
		command = null;
	}
	
	public Event getEvent() { return this.event; }
	public Command getCommand() { return this.command; }
	

}

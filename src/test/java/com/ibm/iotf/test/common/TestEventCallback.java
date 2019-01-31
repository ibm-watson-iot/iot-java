package com.ibm.iotf.test.common;

import java.util.ArrayList;

import com.ibm.iotf.client.app.Command;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;

public class TestEventCallback implements EventCallback {

	Event event = null;
	ArrayList<Event> allEvents = null;
	Command command = null;
	ArrayList<Command> allCommands = null;
	
	@Override
	public void processEvent(Event evt) {
		event = evt;
		if (allEvents == null) {
			allEvents = new ArrayList<Event>();
		}
		allEvents.add(event);
	}

	@Override
	public void processCommand(Command cmd) {
		command = cmd;
		if (allCommands == null) {
			allCommands = new ArrayList<Command>();
		}
		allCommands.add(command);
	}
	
	public void clear() {
		event = null;
		command = null;
		allEvents = null;
		allCommands = null;
	}
	
	public Event getEvent() { return event; }
	public ArrayList<Event> getAllEvents() { return allEvents; }
	public Command getCommand() { return command; }
	public ArrayList<Command> getAllCommands() { return allCommands; }
	

}

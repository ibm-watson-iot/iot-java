package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.callbacks.CommandCallback;
import com.ibm.wiotp.sdk.app.messages.Command;

public class AppCommandCallbackJson implements CommandCallback<JsonObject> {
	private static final Logger LOG = LoggerFactory.getLogger(AppCommandCallbackJson.class);

	Command<JsonObject> command = null;
	ArrayList<Command<JsonObject>> allCommands = null;
	
	@Override
	public void processCommand(Command<JsonObject> cmd) {
		command = cmd;
		if (allCommands == null) {
			allCommands = new ArrayList<Command<JsonObject>>();
		}
		allCommands.add(command);
		LOG.info("Received command, name = "+cmd.getCommandId() + ", format = " + cmd.getFormat() + ", Payload = "+cmd.getData().toString());
	}

	public void clear() {
		command = null;
		allCommands = null;
	}
	
	public Command<JsonObject> getCommand() { return command; }
	public ArrayList<Command<JsonObject>> getAllCommands() { return allCommands; }

	@Override
	public Class<JsonObject> getMessageClass() {
		return JsonObject.class;
	}
}

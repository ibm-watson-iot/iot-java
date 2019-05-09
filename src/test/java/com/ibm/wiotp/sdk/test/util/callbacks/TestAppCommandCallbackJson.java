package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.app.callbacks.CommandCallback;
import com.ibm.wiotp.sdk.app.messages.Command;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestAppCommandCallbackJson implements CommandCallback<JsonObject> {
	Command<JsonObject> command = null;
	ArrayList<Command<JsonObject>> allCommands = null;
	
	private static final String CLASS_NAME = TestAppCommandCallbackJson.class.getName();
	
	@Override
	public void processCommand(Command<JsonObject> cmd) {
		command = cmd;
		if (allCommands == null) {
			allCommands = new ArrayList<Command<JsonObject>>();
		}
		allCommands.add(command);
		LoggerUtility.info(CLASS_NAME, "processCommand", "Received command, name = "+cmd.getCommandId() + ", format = " + cmd.getFormat() + ", Payload = "+cmd.getData().toString());
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

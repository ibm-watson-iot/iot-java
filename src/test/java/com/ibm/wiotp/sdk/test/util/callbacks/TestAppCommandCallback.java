package com.ibm.wiotp.sdk.test.util.callbacks;

import java.util.ArrayList;

import com.ibm.wiotp.sdk.app.callbacks.CommandCallback;
import com.ibm.wiotp.sdk.app.messages.Command;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestAppCommandCallback implements CommandCallback {
	Command command = null;
	ArrayList<Command> allCommands = null;
	
	private static final String CLASS_NAME = TestAppCommandCallback.class.getName();
	
	@Override
	public void processCommand(Command cmd) {
		command = cmd;
		if (allCommands == null) {
			allCommands = new ArrayList<Command>();
		}
		allCommands.add(command);
		LoggerUtility.info(CLASS_NAME, "processCommand", "Received command, name = "+cmd.getCommandId() + ", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload());
	}

	public void clear() {
		command = null;
		allCommands = null;
	}
	
	public Command getCommand() { return command; }
	public ArrayList<Command> getAllCommands() { return allCommands; }
}

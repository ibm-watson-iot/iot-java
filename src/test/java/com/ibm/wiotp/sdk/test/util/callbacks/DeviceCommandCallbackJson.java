package com.ibm.wiotp.sdk.test.util.callbacks;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.device.Command;
import com.ibm.wiotp.sdk.device.CommandCallback;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class DeviceCommandCallbackJson implements CommandCallback<JsonObject> {
	private boolean commandReceived = false;
	private Command<JsonObject> command = null;
	private static final String CLASS_NAME = DeviceCommandCallbackJson.class.getName();

	@Override
	public void processCommand(Command<JsonObject> cmd) {
		final String METHOD = "processCommand";
		commandReceived = true;
		command = cmd;
		LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommandId() + ", format = " + cmd.getFormat() + ", Payload = "+cmd.getData().toString());
		
	}
	
	public void clear() {
		commandReceived = false;
		command = null;
	}
	
	public boolean commandReceived() { return this.commandReceived; }

	public Command<JsonObject> getCommand() { return this.command; }

	@Override
	public Class<JsonObject> getMessageClass() {
		return JsonObject.class;
	}
	
}

package com.ibm.wiotp.sdk.test.util.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.device.Command;
import com.ibm.wiotp.sdk.device.CommandCallback;

public class DeviceCommandCallbackJson implements CommandCallback<JsonObject> {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceCommandCallbackJson.class);
	private boolean commandReceived = false;
	private Command<JsonObject> command = null;

	@Override
	public void processCommand(Command<JsonObject> cmd) {
		commandReceived = true;
		command = cmd;
		LOG.info("Received command, name = " + cmd.getCommandId() + ", format = " + cmd.getFormat() + ", Payload = "
				+ cmd.getData().toString());
	}

	public void clear() {
		commandReceived = false;
		command = null;
	}

	public boolean commandReceived() {
		return this.commandReceived;
	}

	public Command<JsonObject> getCommand() {
		return this.command;
	}

	@Override
	public Class<JsonObject> getMessageClass() {
		return JsonObject.class;
	}

}

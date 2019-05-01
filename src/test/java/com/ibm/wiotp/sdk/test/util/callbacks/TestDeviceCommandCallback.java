package com.ibm.wiotp.sdk.test.util.callbacks;

import com.ibm.wiotp.sdk.device.Command;
import com.ibm.wiotp.sdk.device.CommandCallback;
import com.ibm.wiotp.sdk.util.LoggerUtility;

public class TestDeviceCommandCallback implements CommandCallback {
	private boolean commandReceived = false;
	private Command command = null;
	private static final String CLASS_NAME = TestDeviceCommandCallback.class.getName();

	@Override
	public void processCommand(Command cmd) {
		final String METHOD = "processCommand";
		commandReceived = true;
		command = cmd;
		LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommandId() + ", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload());
		
	}
	
	public void clear() {
		commandReceived = false;
		command = null;
	}
	
	public boolean commandReceived() { return this.commandReceived; }

	public Command getCommand() { return this.command; }
	
}

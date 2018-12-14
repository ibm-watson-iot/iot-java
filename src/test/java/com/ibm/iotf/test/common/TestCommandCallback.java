package com.ibm.iotf.test.common;

import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.util.LoggerUtility;

public class TestCommandCallback implements CommandCallback {
	private boolean commandReceived = false;
	private static final String CLASS_NAME = TestCommandCallback.class.getName();

	@Override
	public void processCommand(Command cmd) {
		final String METHOD = "processCommand";
		commandReceived = true;
		LoggerUtility.info(CLASS_NAME, METHOD, "Received command, name = "+cmd.getCommand() +
				", format = " + cmd.getFormat() + ", Payload = "+cmd.getPayload() + ", time = "+cmd.getTimestamp());
		
	}
	
	public void clear() {
		commandReceived = false;
	}
	
	public boolean commandReceived() { return this.commandReceived; }

}

package com.ibm.iotf.client.app;


public interface CommandCallback {

	/**
	 * 
	 * process the command received
	 * 
	 * @param cmd
	 *               Application subscribes to this command on the behalf of a device
	 */	

	public void processCommand(Command cmd);
}

package com.ibm.iotf.client.device;

/**
 * Interface to provide callback methods for command subscription  <br>
 * This can be used by devices to subscribe to commands
 * 
 */
public interface CommandCallback {

	
	/**
	 * process the command received
	 * 
	 * @param cmd
	 *               The command upon receiving an action is triggered in a device 
	 */	
	public void processCommand(Command cmd);
}

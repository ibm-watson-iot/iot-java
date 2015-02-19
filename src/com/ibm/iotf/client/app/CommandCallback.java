package com.ibm.iotf.client.app;


/**
 * @deprecated This would be eventually replaced by com.ibm.iotf.client.device.CommandCallback <br>
 * Currently retained for command subscription by application
 * Interface to provide callback methods for command subscription <br>
 * This can be used by applications to subscribe to commands
 * 
 */

@Deprecated
public interface CommandCallback {

	/**
	 * @deprecated This would eventually be replaced by com.ibm.iotf.client.device.CommandCallback.processCommand() <br>
	 * 
	 * process the command received
	 * 
	 * @param cmd
	 *               Application subscribes to this command on the behalf of a device
	 */	

	public void processCommand(Command cmd);
}

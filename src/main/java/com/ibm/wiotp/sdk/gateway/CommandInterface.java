package com.ibm.wiotp.sdk.gateway;

import com.ibm.wiotp.sdk.MessageInterface;

public interface CommandInterface<T> extends MessageInterface<T> {
	public String getTypeId();
	public String getDeviceId();
	public String getCommandId();
	
}

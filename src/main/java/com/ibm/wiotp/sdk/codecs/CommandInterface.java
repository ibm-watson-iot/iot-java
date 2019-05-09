package com.ibm.wiotp.sdk.codecs;

public interface CommandInterface<T> extends MessageInterface<T> {
	public String getTypeId();
	public String getDeviceId();
	public String getCommandId();
	
}

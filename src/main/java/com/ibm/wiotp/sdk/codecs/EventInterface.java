package com.ibm.wiotp.sdk.codecs;

public interface EventInterface<T> extends MessageInterface<T> {
	public String getTypeId();
	public String getDeviceId();
	public String getEventId();
	
}

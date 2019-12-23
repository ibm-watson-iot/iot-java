package com.ibm.wiotp.sdk.app.messages;

import com.ibm.wiotp.sdk.MessageInterface;

public interface EventInterface<T> extends MessageInterface<T> {
	public String getTypeId();

	public String getDeviceId();

	public String getEventId();

}

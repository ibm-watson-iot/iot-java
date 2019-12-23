package com.ibm.wiotp.sdk.app.messages;

import com.ibm.wiotp.sdk.MessageInterface;

public interface CommandInterface<T> extends MessageInterface<T> {
	public String getTypeId();

	public String getDeviceId();

	public String getCommandId();

}

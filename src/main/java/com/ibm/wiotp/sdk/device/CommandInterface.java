package com.ibm.wiotp.sdk.device;

import com.ibm.wiotp.sdk.MessageInterface;

public interface CommandInterface<T> extends MessageInterface<T> {
	public String getCommandId();

}

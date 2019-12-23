package com.ibm.wiotp.sdk;

import org.joda.time.DateTime;

public interface MessageInterface<T> {
	public T getData();

	public DateTime getTimestamp();
}

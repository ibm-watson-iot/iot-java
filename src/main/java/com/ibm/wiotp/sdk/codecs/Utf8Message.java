package com.ibm.wiotp.sdk.codecs;

import org.joda.time.DateTime;

public class Utf8Message implements MessageInterface<String>{
	
	private String data;
	private DateTime timestamp;
	
	public Utf8Message(String data, DateTime timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}
	
	@Override
	public String getData() {
		return data;
	}

	@Override
	public DateTime getTimestamp() {
		return timestamp;
	}
	

}

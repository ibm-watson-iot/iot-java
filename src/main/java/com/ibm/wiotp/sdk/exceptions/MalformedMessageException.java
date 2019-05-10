package com.ibm.wiotp.sdk.exceptions;

public class MalformedMessageException extends RuntimeException {

	private static final long serialVersionUID = 1122911093866778851L;
	
	private String format;
	
	public MalformedMessageException(String format) {
		super("Unable to decode the content of message");
		this.format = format;
	}
	
	public String getFormat() {
		return format;
	}
	
}

package com.ibm.wiotp.sdk.exceptions;

public class MissingMessageDecoderException extends RuntimeException {

	private static final long serialVersionUID = 1122911093866778851L;
	
	private String format;
	
	public MissingMessageDecoderException(String format) {
		super("There is no registered handler for decoding messages in format '" + format + "'");
		this.format = format;
	}
	
	public String getFormat() {
		return format;
	}
	
}

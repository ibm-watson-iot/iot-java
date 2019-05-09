package com.ibm.wiotp.sdk.exceptions;

public class MissingMessageEncoderException extends RuntimeException {

	private static final long serialVersionUID = 1122911093866778851L;
	
	private String className;
	
	public MissingMessageEncoderException(String className) {
		super("There is no registered handler for encoding objects of class '" + className + "'");
		this.className = className;
	}
	
	public String getFormat() {
		return className;
	}
	
}

/**
 *****************************************************************************
 Copyright (c) 2015-19 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 *
 */
package com.ibm.wiotp.sdk.device;

import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.MessageInterface;


/**
 * The objects of this class hold the command and the format of the command sent to a device <br>
 * This class has only accessors and no mutators and later on might be made final
 * 
 */

public class Command<T> implements MessageInterface<T>{

	private String commandId;
	private String format;
	private MessageInterface<T> message;

	/**
	 * Note that this class does not have a default constructor <br>

	 * This class has only accessors and no mutators and later on might be made final <br>
	 * @param command
	 * 			Actual command in the form of String passed
	 * @param format
	 * 			Format is a String which can contain values such as "json"
	 * @param message
	 * 			Object implementing MessageInterface
	 * @see <a href="http://www.eclipse.org/paho/files/javadoc/index.html">Paho Client Library</a> 
	 * 
	 */	
	public Command(String command, String format, MessageInterface<T> message) {
		this.commandId = command;
		this.format = format;
		this.message = message;
	}
	
	/**
	 * Returns the name of the command
	 * @return the name of the command 
	 */
	public String getCommandId() {
		return commandId;
	}
	
	public String getFormat() {
		return format.toString();
	}

	@Override
	public T getData() {
		return message.getData();
	}

	@Override
	public DateTime getTimestamp() {
		return message.getTimestamp();
	}
}

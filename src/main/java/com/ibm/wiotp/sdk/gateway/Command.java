/**
 *****************************************************************************
 * Copyright (c) 2016-19 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************
 */
package com.ibm.wiotp.sdk.gateway;

import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.MessageInterface;

/**
 * The Command class
 */
public class Command<T> implements CommandInterface<T> {

	private String typeId;
	private String deviceId;
	private String commandId;
	private String format;
	private MessageInterface<T> message;

	/**
	 * Note that this class does not have a default constructor
	 * 
	 * @param typeId    Object of String which denotes command type
	 * @param deviceId  Object of String which denotes command id
	 * @param commandId Object of String which denotes actual command type
	 * @param format    Object of String which denotes command format, say json
	 * @param message   Object implementing MessageInterface
	 * @see <a href="http://www.eclipse.org/paho/files/javadoc/index.html">Paho
	 *      Client Library</a>
	 * 
	 */
	public Command(String typeId, String deviceId, String commandId, String format, MessageInterface<T> message) {
		this.typeId = typeId;
		this.deviceId = deviceId;
		this.commandId = commandId;
		this.format = format;
		this.message = message;
	}

	public String getFormat() {
		return format;
	}

	@Override
	public DateTime getTimestamp() {
		return message.getTimestamp();
	}

	@Override
	public String getTypeId() {
		return typeId;
	}

	@Override
	public String getCommandId() {
		return commandId;
	}

	@Override
	public String getDeviceId() {
		return deviceId;
	}

	@Override
	public T getData() {
		return message.getData();
	}

}

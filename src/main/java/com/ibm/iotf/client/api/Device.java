/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Initial Contribution
 *****************************************************************************
 *
 */
package com.ibm.iotf.client.api;


/**
 * A bean class which represents a device registered (or to be registered) with the IBM Watson IoT Platform. <br>
 * This class has only accessors and mutators
 */
public class Device {
	private String uuid = null;
	private String type = null;
	private String id = null;
	private String metadata = null;
	
	private String password = null;
	private String registration = null;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}
	
	/**
	 * 
	 * Provides a human readable String representation of this device, including the id, type, metadata and registration.
	 */		
	public String toString() {
		return "Device : uuid = " + uuid + " id = " + id + " type = " + type + " metadata = " + metadata +
				" password = " + password + " registration = " + registration;
	}
}

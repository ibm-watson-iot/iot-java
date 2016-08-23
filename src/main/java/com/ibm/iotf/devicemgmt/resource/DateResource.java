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
package com.ibm.iotf.devicemgmt.resource;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * An internal class that represents a Date attribute of the
 * Device
 */
public class DateResource extends Resource<Date> {

	public DateResource(String name, Date value) {
		super(name, value);
	}
	
	/**
	 * Returns the value in Json Format
	 */
	@Override
	public JsonElement toJsonObject() {
		String utcTime = DateFormatUtils.formatUTC(getValue(), 
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
		return (JsonElement) new JsonPrimitive(utcTime);	
	}

	/**
	 * Updates the value of this resource with the given Json value
	 */
	@Override
	public int update(JsonElement json, boolean fireEvent) {
		DateTime dt = new DateTime(json.getAsString());
		super.setValue(new Date(dt.getMillis()), fireEvent);
		return this.getRC();
	}
	
	/**
	 * Updates the value of this resource with the given Json value
	 */
	@Override
	public int update(JsonElement json) {
		return update(json, true);
	}
}

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
package com.ibm.iotf.devicemgmt;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.resource.DateResource;
import com.ibm.iotf.devicemgmt.resource.NumberResource;
import com.ibm.iotf.devicemgmt.resource.Resource;

/**
 * A bean class which represents the location of a device.  When a property is changed, 
 * the IBM Watson IoT Platform will be notified.
 *
 */
public class DeviceLocation extends Resource {
	
	public DeviceLocation() {
		super(RESOURCE_NAME);
	}

	public static final String RESOURCE_NAME = "location";
	
	private NumberResource latitude;
	private NumberResource longitude;
	private NumberResource elevation;
	private DateResource measuredDateTime;
	private NumberResource accuracy;
	
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String ELEVATION = "elevation";
	private static final String MEASUREDDATETIME = "measuredDateTime";
	private static final String ACCURACY = "accuracy";
	
	public double getLatitude() {
		return this.latitude.getValue().doubleValue();
	}

	public double getLongitude() {
		return this.longitude.getValue().doubleValue();
	}

	public double getElevation() {
		if(this.elevation != null) {
			return this.elevation.getValue().doubleValue();
		}
		return 0;
	
	}

	public Date getMeasuredDateTime() {
		return this.measuredDateTime.getValue();
	}

	public double getAccuracy() {
		if(this.accuracy != null) {
			return this.accuracy.getValue().doubleValue();
		}
		return 0;
	}
	
	/**
	 * Return the JSON string of the <code>DeviceLocation</code> object.
	 */
	public String toString() {
		return toJsonObject().toString();
	}
	
	/**
	 * Updates each of the resources with the new value
	 * 
	 * @param fromLocation The location that needs to be updated
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int update(JsonElement fromLocation) {
		return update(fromLocation, true);
	}
	
	/**
	 * Updates each of the resources with the new value
	 * 
	 * @param fromLocation The location that needs to be updated
	 * @param fireEvent - boolean to indicate whether to fire the update event.
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)
	 */
	public int update(JsonElement fromLocation, boolean fireEvent) {
		JsonObject json = (JsonObject) fromLocation;
		Iterator <Map.Entry<String,JsonElement>>iter = json.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, JsonElement> e = iter.next();
			Resource child = this.getChild(e.getKey());
			if (child != null) {
				child.update(e.getValue(), fireEvent);
			} else {
				switch(e.getKey()) {
				case DeviceLocation.LATITUDE: 
					this.latitude = new NumberResource(LATITUDE, e.getValue().getAsNumber());
					this.add(this.latitude);
					break;
				case DeviceLocation.LONGITUDE:
					this.longitude = new NumberResource(LONGITUDE, e.getValue().getAsNumber());
					this.add(this.longitude);

					break;
					
				case DeviceLocation.ELEVATION:
					this.elevation = new NumberResource(ELEVATION, e.getValue().getAsNumber());
					this.add(this.elevation);
					break;
					
				case DeviceLocation.MEASUREDDATETIME:
					this.measuredDateTime = new DateResource(ACCURACY, new Date(e.getValue().getAsString()));
					this.add(this.measuredDateTime);
					break;
					
				case DeviceLocation.ACCURACY:
					this.accuracy = new NumberResource(ACCURACY, e.getValue().getAsNumber());
					this.add(this.accuracy);
					break;
				}
			}
		}
		fireEvent(true);
		return this.getRC();
	}
	
	/**
	 * Return the <code>JsonObject</code> representation of the <code>DeviceLocation</code> object.
	 * @return JsonObject object
	 */
	public JsonObject toJsonObject() {
		JsonObject json = new JsonObject();
		json.addProperty(this.latitude.getResourceName(), latitude.getValue());
		json.addProperty(this.longitude.getResourceName(), longitude.getValue());
		if(elevation != null) {
			json.addProperty(this.elevation.getResourceName(), elevation.getValue());
		}
		
		String utcTime = DateFormatUtils.formatUTC(measuredDateTime.getValue(), 
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
		
		json.addProperty(this.measuredDateTime.getResourceName(), utcTime);
		
		if(accuracy != null) {
			json.addProperty(this.accuracy.getResourceName(), accuracy.getValue());
		}
		return json;
	}
	
	
}

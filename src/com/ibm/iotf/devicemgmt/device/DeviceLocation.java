/**
 *****************************************************************************
 Copyright (c) 2015 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Mike Tran - Initial Contribution
 Sathiskumar Palaniappan - Modified to include Resource Model
 *****************************************************************************
 *
 */
package com.ibm.iotf.devicemgmt.device;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.devicemgmt.device.resource.DateResource;
import com.ibm.iotf.devicemgmt.device.resource.NumberResource;
import com.ibm.iotf.devicemgmt.device.resource.Resource;

/**
 * A bean class which represents the location of a device.  When a property is changed, 
 * the IBM Internet of Things Foundation will be notified.
 *
 */
public class DeviceLocation extends Resource {
	
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
	
	/**
	 * Create an <code>DeviceLocation</code> that can be used to communicate 
	 * with IBM IoT Foundation.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param elevation
	 * @param accuracy
	 * @param measuredDateTime  When the location information is retrieved
	 */
	private DeviceLocation(Builder builder) {
		super(RESOURCE_NAME);
		this.latitude = new NumberResource(LATITUDE, builder.latitude);
		this.longitude = new NumberResource(LONGITUDE, builder.longitude);
		
		this.add(this.latitude);
		this.add(this.longitude);
		
		if(this.elevation != null) {
			this.elevation = new NumberResource(ELEVATION, builder.elevation);
			this.add(this.elevation);
		}
		
		this.measuredDateTime = new DateResource(MEASUREDDATETIME, builder.measuredDateTime);
		this.add(this.measuredDateTime);

		if(builder.accuracy != null) {
			this.accuracy = new NumberResource(ACCURACY, builder.accuracy);
			this.add(this.accuracy);
		}
		
	}
	
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

	
	public DeviceLocation getDeviceLocation() {
		return this;
	}
	
	/**
	 * Update the location with information from the specified <code>DeviceLocation</code> object.
	 * 
	 * @param location <code>DeviceLocation</code> object.
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int update(DeviceLocation location) {
		return update(location.toJsonObject(), true);
	}
	
	/**
	 * Update the location.
	 * 
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int update(Double latitude, Double longitude, Double elevation) {
		return update(latitude, longitude, elevation, new Date());
	}

	/**
	 * Update the location
	 * 
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * @param measuredDateTime When the location information is retrieved
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int update(Double latitude, Double longitude, Double elevation, Date measuredDateTime) {
		return update(latitude, longitude, elevation, measuredDateTime, null);
	}
	
	/**
	 * Update the location
	 * 
	 * @param latitude	Latitude in decimal degrees using WGS84
	 * @param longitude Longitude in decimal degrees using WGS84
	 * @param elevation	Elevation in meters using WGS84
	 * @param measuredDateTime When the location information is retrieved
	 * @param accuracy	Accuracy of the position in meters
	 * 
	 * @return code indicating whether the update is successful or not 
	 *        (200 means success, otherwise unsuccessful)

	 */
	public int update(Double latitude, Double longitude, Double elevation, Date measuredDateTime, Double accuracy) {
		if(latitude != null) {
			this.latitude.setValue(latitude);
		}
		
		if(longitude != null) {
			this.longitude.setValue(longitude);
		}
		
		if(elevation != null) {
			if(this.elevation == null) {
				this.elevation = new NumberResource(ELEVATION, elevation);
				this.add(this.elevation);
			} else {
				this.elevation.setValue(elevation);
			}
		}
		
		if(measuredDateTime != null ){
			this.measuredDateTime.setValue(measuredDateTime);
		} else {
			this.measuredDateTime.setValue(new Date());
		}
		
		if(accuracy != null) {
			if(this.accuracy == null) {
				this.accuracy = new NumberResource(ACCURACY, accuracy);
				this.add(this.accuracy);
			} else {
				this.accuracy.setValue(accuracy);
			}
		}
		pcs.firePropertyChange(this.getCanonicalName(), null, this);
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
	
	/**
	 * Return the JSON string of the <code>DeviceLocation</code> object.
	 */
	public String toString() {
		return toJsonObject().toString();
	}
	
	public static class Builder {
		private Double latitude;
		private Double longitude;
		private Double elevation;
		private Date measuredDateTime;
		private Double accuracy;
		
		public Builder(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.measuredDateTime = new Date();
		}
		
		public Builder elevation(double elevation) {
			this.elevation = elevation;
			return this;
		}
		
		public Builder measuredDateTime(Date measuredDateTime) {
			this.measuredDateTime = measuredDateTime;
			return this;
		}
		
		public Builder accuracy(double accuracy) {
			this.accuracy = accuracy;
			return this;
		}
		
		public DeviceLocation build() {
			DeviceLocation location = new DeviceLocation(this);
			return location;
		}
	}

	/**
	 * Updates each of the resources with the new value
	 * 
	 * @param fromLocation
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
		if(fireEvent) {
			pcs.firePropertyChange(this.getCanonicalName(), null, this);
		}
		return this.getRC();
	}
	
}

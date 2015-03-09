package com.ibm.iotf.client.api;

/**
 * 
 * A bean which represents Historical event <br>
 * This class has accessors and mutators
 *
 */

public class HistoricalEvent {

	private String org_id = null;
	private String device_type = null;
	private String device_id = null;
	private String evt_type = null;
	
	private String timestamp = null;
	private String evt = null;
	
	public HistoricalEvent() {
		
	}

	public String getOrg_id() {
		return org_id;
	}
	
	public void setOrg_id(String org_id) {
		this.org_id = org_id;
	}
	
	public String getDevice_type() {
		return device_type;
	}
	
	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}
	
	public String getDevice_id() {
		return device_id;
	}
	
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	
	public String getEvt_type() {
		return evt_type;
	}
	
	public void setEvt_type(String evt_type) {
		this.evt_type = evt_type;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getEvt() {
		return evt;
	}
	
	public void setEvt(String evt) {
		this.evt = evt;
	}
	
	/**
	 * 
	 * Provides a human readable String representation of this HistoricalEvent, including the event, event type, org id, device type, device id and timestamp.
	 */		
	public String toString() {
		return "HistoricalEvent : evt = " + evt + " evt_type = " + evt_type + " org_id = " + org_id + " device_type = " + device_type + " device_id = " + device_id +
				" timestamp = " + timestamp;
	}	
}

package com.ibm.iotf.client.api;


/*
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

/*	
	public HistoricalEvent(JsonElement messageElement) {
		JsonObject responseJson = messageElement.getAsJsonObject();
		org_id = responseJson.get("org_id").toString();
		device_type = responseJson.get("device_type").toString();
		device_id = responseJson.get("device_id").toString();
		evt_type = responseJson.get("evt_type").toString();
		evt = responseJson.get("evt").toString();
		timestamp = responseJson.get("timestamp").toString();
	}
*/	
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
}

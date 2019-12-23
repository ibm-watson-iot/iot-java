package com.ibm.wiotp.sdk.codecs;

import org.joda.time.DateTime;

import com.google.gson.JsonObject;
import com.ibm.wiotp.sdk.MessageInterface;

public class JsonMessage implements MessageInterface<JsonObject> {

	private JsonObject data;
	private DateTime timestamp;

	public JsonMessage(JsonObject data, DateTime timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}

	@Override
	public JsonObject getData() {
		return data;
	}

	@Override
	public DateTime getTimestamp() {
		return timestamp;
	}

}

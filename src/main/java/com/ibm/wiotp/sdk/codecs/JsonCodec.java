package com.ibm.wiotp.sdk.codecs;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.ibm.wiotp.sdk.exceptions.MalformedMessageException;

public class JsonCodec implements MessageCodec<JsonObject> {
	private final static JsonParser JSON_PARSER = new JsonParser();

	@Override
	public byte[] encode(JsonObject data, DateTime timestamp) {
		if (data != null) {
			return data.toString().getBytes(Charset.forName("UTF-8"));
		} else {
			return new byte[0];
		}
	}

	@Override
	public JsonMessage decode(MqttMessage msg) throws MalformedMessageException {
		JsonObject data;

		if (msg.getPayload().length == 0) {
			data = null;
		} else {
			try {
				final String payloadInString = new String(msg.getPayload(), "UTF8");
				data = JSON_PARSER.parse(payloadInString).getAsJsonObject();
			} catch (JsonParseException | UnsupportedEncodingException e) {
				throw new MalformedMessageException("Unable to parse JSON: " + e.toString());
			}
		}

		return new JsonMessage(data, null);
	}

	@Override
	public Class<JsonObject> getMessageClass() {
		return JsonObject.class;
	}

	@Override
	public String getMessageFormat() {
		return "json";
	}

}

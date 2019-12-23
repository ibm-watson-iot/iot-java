package com.ibm.wiotp.sdk.codecs;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

import com.ibm.wiotp.sdk.exceptions.MalformedMessageException;

public class Utf8Codec implements MessageCodec<String> {

	@Override
	public byte[] encode(String data, DateTime timestamp) {
		if (data != null) {
			return data.getBytes(Charset.forName("UTF-8"));
		} else {
			return new byte[0];
		}
	}

	@Override
	public Utf8Message decode(MqttMessage msg) throws MalformedMessageException {
		String data;
		try {
			data = new String(msg.getPayload(), "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new MalformedMessageException("Unable to decode string as UTF-8: " + e.toString());
		}
		return new Utf8Message(data, null);
	}

	@Override
	public Class<String> getMessageClass() {
		return String.class;
	}

	@Override
	public String getMessageFormat() {
		return "utf8";
	}

}

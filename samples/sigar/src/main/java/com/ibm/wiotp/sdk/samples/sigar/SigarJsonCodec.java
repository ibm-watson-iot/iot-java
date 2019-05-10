package com.ibm.wiotp.sdk.samples.sigar;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.DateTime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.ibm.wiotp.sdk.MessageInterface;
import com.ibm.wiotp.sdk.codecs.MessageCodec;
import com.ibm.wiotp.sdk.exceptions.MalformedMessageException;

public class SigarJsonCodec implements MessageCodec<SigarData>{
	private final static JsonParser JSON_PARSER = new JsonParser();
	
	@Override
	public byte[] encode(SigarData data, DateTime timestamp) {
    	JsonObject json = new JsonObject();
    	json.addProperty("name", data.getName());
    	json.addProperty("disk", data.getDisk());
    	json.addProperty("mem", data.getMem());
    	json.addProperty("cpu", data.getCpu());
    	json.addProperty("timestamp", timestamp.toString());

		return json.toString().getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public MessageInterface<SigarData> decode(MqttMessage msg) throws MalformedMessageException {
		JsonObject json;
		
		if (msg.getPayload().length == 0) {
			return new SigarData();
		}
		
		try {
			final String payloadInString = new String(msg.getPayload(), "UTF8");
			json = JSON_PARSER.parse(payloadInString).getAsJsonObject();
			
	    	return new SigarData(
	    			json.get("name").getAsString(), 
	    			json.get("disk").getAsDouble(), 
	    			json.get("mem").getAsDouble(), 
	    			json.get("cpu").getAsDouble(), 
	    			DateTime.parse(json.get("timestamp").getAsString())
	    	);
		} catch (JsonParseException | UnsupportedEncodingException e) {
			throw new MalformedMessageException("Unable to parse JSON: " + e.toString());
		}
	}

	@Override
	public Class<SigarData> getMessageClass() {
		return SigarData.class;
	}

	@Override
	public String getMessageFormat() {
		return "json-sigar";
	}
	
	
}

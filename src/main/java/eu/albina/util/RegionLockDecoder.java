package eu.albina.util;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

import eu.albina.model.RegionLock;

public class RegionLockDecoder implements Decoder.Text<RegionLock> {

	@Override
	public RegionLock decode(String s) throws DecodeException {
		return new RegionLock(new JSONObject(s));
	}

	@Override
	public boolean willDecode(String s) {
		return (s != null);
	}

	@Override
	public void init(EndpointConfig endpointConfig) {
		// Custom initialization logic
	}

	@Override
	public void destroy() {
		// Close resources
	}
}
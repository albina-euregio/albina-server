package eu.albina.util;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

import eu.albina.model.BulletinUpdate;

public class BulletinUpdateDecoder implements Decoder.Text<BulletinUpdate> {

	@Override
	public BulletinUpdate decode(String s) throws DecodeException {
		return new BulletinUpdate(new JSONObject(s));
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
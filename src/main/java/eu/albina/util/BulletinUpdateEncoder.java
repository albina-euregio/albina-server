package eu.albina.util;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import eu.albina.model.BulletinUpdate;

public class BulletinUpdateEncoder implements Encoder.Text<BulletinUpdate> {

	@Override
	public String encode(BulletinUpdate lock) throws EncodeException {
		return lock.toJSON().toString();
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
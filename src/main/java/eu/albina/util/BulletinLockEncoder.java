package eu.albina.util;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import eu.albina.model.BulletinLock;

public class BulletinLockEncoder implements Encoder.Text<BulletinLock> {

	@Override
	public String encode(BulletinLock lock) throws EncodeException {
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
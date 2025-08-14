// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import eu.albina.model.RegionLock;

public class RegionLockEncoder implements Encoder.Text<RegionLock> {

	@Override
	public String encode(RegionLock lock) throws EncodeException {
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

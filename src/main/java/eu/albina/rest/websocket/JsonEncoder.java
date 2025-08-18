// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import eu.albina.util.JsonUtil;

public class JsonEncoder<T> implements Encoder.Text<T> {

	@Override
	public String encode(T object) throws EncodeException {
		return JsonUtil.writeValueUsingJackson(object);
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

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import eu.albina.util.JsonUtil;


public abstract class JsonDecoder<T> implements Decoder.Text<T> {

    private final Class<T> targetClass;

    public JsonDecoder(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T decode(String s) throws DecodeException {
        return JsonUtil.parseUsingJackson(s, targetClass);
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
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

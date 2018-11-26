package eu.albina.util;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import eu.albina.model.ChatMessage;

public class ChatMessageEncoder implements Encoder.Text<ChatMessage> {

	@Override
	public String encode(ChatMessage message) throws EncodeException {
		return message.toJSON().toString();
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
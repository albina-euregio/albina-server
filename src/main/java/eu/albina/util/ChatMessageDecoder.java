package eu.albina.util;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.json.JSONObject;

import eu.albina.model.ChatMessage;

public class ChatMessageDecoder implements Decoder.Text<ChatMessage> {

	@Override
	public ChatMessage decode(String s) throws DecodeException {
		return new ChatMessage(new JSONObject(s));
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
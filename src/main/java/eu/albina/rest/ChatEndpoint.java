package eu.albina.rest;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.ChatController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.ChatMessage;
import eu.albina.util.ChatMessageDecoder;
import eu.albina.util.ChatMessageEncoder;

@ServerEndpoint(value = "/chat/{username}", decoders = ChatMessageDecoder.class, encoders = ChatMessageEncoder.class)
public class ChatEndpoint {

	private static Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);

	private Session session;
	private static Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
	private static HashMap<String, String> users = new HashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {
		this.session = session;
		chatEndpoints.add(this);
		users.put(session.getId(), username);

		// ChatMessage message = new ChatMessage();
		// message.setDateTime(new DateTime());
		// message.setUsername(username);
		// message.setText("CONNECTED");

		logger.info("Client connected: " + username);

		// broadcast(message);
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException, EncodeException, AlbinaException {
		ChatMessage chatMessage = new ChatMessage(new JSONObject(message));
		ChatController.getInstance().saveChatMessage(chatMessage);
		broadcast(chatMessage);
	}

	@OnClose
	public void onClose(Session session) throws IOException, EncodeException {
		chatEndpoints.remove(this);

		// ChatMessage message = new ChatMessage();
		// message.setDateTime(new DateTime());
		// message.setUsername(users.get(session.getId()));
		// message.setText("DISCONNECTED");

		logger.info("Client disconnected: " + users.get(session.getId()));

		// broadcast(message);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.error("Chat error: " + throwable.getMessage());
	}

	private static void broadcast(ChatMessage message) throws IOException, EncodeException {
		chatEndpoints.forEach(endpoint -> {
			synchronized (endpoint) {
				try {
					endpoint.session.getBasicRemote().sendObject(message);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Collection<String> getActiveUsers() {
		return users.values();
	}
}
// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;

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

import com.github.openjson.JSONObject;
import eu.albina.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.ChatController;
import eu.albina.model.ChatMessage;

@ServerEndpoint(value = "/chat/{username}")
public class ChatEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);

	private Session session;
	private static final Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
	private static final HashMap<String, String> users = new HashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) {
		this.session = session;
		this.session.setMaxIdleTimeout(0); // never timeout due to inactivity
		chatEndpoints.add(this);
		users.put(session.getId(), username);

		// ChatMessage message = new ChatMessage();
		// message.setDateTime(new DateTime());
		// message.setUsername(username);
		// message.setText("CONNECTED");

        logger.info("Client connected: {}", username);

		// broadcast(message);
	}

	@OnMessage
	public void onMessage(Session session, String message) {
		ChatMessage chatMessage = JsonUtil.parseUsingJackson(message, ChatMessage.class);
		ChatController.getInstance().saveChatMessage(chatMessage);
		broadcast(chatMessage);
	}

	@OnClose
	public void onClose(Session session) {
		chatEndpoints.remove(this);

		// ChatMessage message = new ChatMessage();
		// message.setDateTime(new DateTime());
		// message.setUsername(users.get(session.getId()));
		// message.setText("DISCONNECTED");

        logger.info("Client disconnected: {}", users.get(session.getId()));

		// broadcast(message);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.debug("Chat error", throwable);
	}

	private static void broadcast(ChatMessage message) {
		chatEndpoints.forEach(endpoint -> {
			synchronized (endpoint) {
				try {
					endpoint.session.getBasicRemote().sendObject(message);
				} catch (IOException | EncodeException e) {
					logger.warn("Broadcasting error", e);
				}
			}
		});
	}

	public static Collection<String> getActiveUsers() {
		return users.values();
	}
}

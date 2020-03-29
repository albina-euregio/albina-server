/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.ChatController;
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
	public void onOpen(Session session, @PathParam("username") String username) {
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
	public void onMessage(Session session, String message) {
		ChatMessage chatMessage = new ChatMessage(new JSONObject(message));
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

		logger.info("Client disconnected: " + users.get(session.getId()));

		// broadcast(message);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.error("Chat error", throwable);
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

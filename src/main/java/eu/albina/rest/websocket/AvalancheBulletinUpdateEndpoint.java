// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micronaut.http.annotation.PathVariable;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnError;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.BulletinUpdate;

@ServerWebSocket(value = "/update/{username}")
public class AvalancheBulletinUpdateEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinUpdateEndpoint.class);

	private static final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(WebSocketSession session, @PathVariable String username) {
		sessions.put(session.getId(), session);
		logger.info("Client connected: {}", username);
	}


	@OnMessage
	public void onMessage(WebSocketSession session, BulletinUpdate bulletinUpdate) {
		// Micronaut automatically deserializes JSON into BulletinUpdate
		broadcast(bulletinUpdate);
	}

	@OnClose
	public void onClose(WebSocketSession session) {
		sessions.remove(session.getId());
		logger.info("Client disconnected: {}", session.getId());
	}

	@OnError
	public void onError(WebSocketSession session, Throwable throwable) {
		logger.warn("Bulletin update error", throwable);
	}

	public static void broadcast(BulletinUpdate update) {
		sessions.values().forEach(session -> {
			try {
				session.send(update);
			} catch (WebSocketSessionException e) {
				logger.warn("Session closed while broadcasting", e);
			}
		});
	}
}

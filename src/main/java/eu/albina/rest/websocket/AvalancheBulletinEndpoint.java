// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.BulletinLock;
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ServerWebSocket("/bulletin/{username}")
public class AvalancheBulletinEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinEndpoint.class);

	private static final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(WebSocketSession session, @PathVariable String username) {
		sessions.put(session.getId(), session);
		logger.info("Client connected: {}", username);
	}

	@OnMessage
	public void onMessage(WebSocketSession session, BulletinLock bulletinLock) throws AlbinaException {
		// set session id
		bulletinLock.setSessionId(session.getId());

		if (bulletinLock.getLock()) {
			AvalancheBulletinController.getInstance().lockBulletin(bulletinLock);
		} else {
			AvalancheBulletinController.getInstance().unlockBulletin(bulletinLock);
		}

		broadcast(bulletinLock);
	}

	@OnClose
	public void onClose(WebSocketSession session) {
		sessions.remove(session.getId());
		AvalancheBulletinController.getInstance().unlockBulletins(session.getId());
		logger.info("Client disconnected: {}", session.getId());
	}

	@OnError
	public void onError(WebSocketSession session, Throwable throwable) {
		logger.warn("Bulletin lock error", throwable);
	}

	public static void broadcast(BulletinLock lock) {
		sessions.values().forEach(session -> {
			try {
				session.send(lock);
			} catch (WebSocketSessionException e) {
				logger.warn("Session closed while broadcasting", e);
			}
		});
	}
}

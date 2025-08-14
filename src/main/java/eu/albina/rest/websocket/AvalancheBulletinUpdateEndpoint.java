// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.websocket;

import java.io.IOException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.BulletinUpdate;

@ServerEndpoint(value = "/update/{username}", decoders = BulletinUpdateDecoder.class, encoders = BulletinUpdateEncoder.class)
public class AvalancheBulletinUpdateEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinUpdateEndpoint.class);

	private Session session;
	private static final Set<AvalancheBulletinUpdateEndpoint> updateEndpoints = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) {
		this.session = session;
		this.session.setMaxIdleTimeout(0); // never timeout due to inactivity
		updateEndpoints.add(this);
        logger.info("Client connected: {}", username);
	}

	@OnMessage
	public void onMessage(Session session, String lock) {
		// BulletinUpdate bulletinUpdate = new BulletinUpdate(new JSONObject(lock));
		// broadcast(bulletinUpdate);
	}

	@OnClose
	public void onClose(Session session) {
		updateEndpoints.remove(this);
        logger.info("Client disconnected: {}", session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.debug("Bulletin update error", throwable);
	}

	public static void broadcast(BulletinUpdate lock) {
		updateEndpoints.forEach(endpoint -> {
			synchronized (endpoint) {
				try {
					endpoint.session.getBasicRemote().sendObject(lock);
				} catch (IOException | EncodeException e) {
					logger.warn("Broadcasting error", e);
				}
			}
		});
	}
}

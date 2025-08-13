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

import eu.albina.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.BulletinLock;

@ServerEndpoint(value = "/bulletin/{username}", decoders = AvalancheBulletinEndpoint.BulletinLockDecoder.class, encoders = AvalancheBulletinEndpoint.BulletinLockEncoder.class)
public class AvalancheBulletinEndpoint {

	public static class BulletinLockEncoder extends JsonEncoder<BulletinLock> {}
	public static class BulletinLockDecoder extends JsonDecoder<BulletinLock> {
		public BulletinLockDecoder() {
        	super(BulletinLock.class);
    	}	
	}

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinEndpoint.class);

	private Session session;
	private static final Set<AvalancheBulletinEndpoint> bulletinEndpoints = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) {
		this.session = session;
		this.session.setMaxIdleTimeout(0); // never timeout due to inactivity
		bulletinEndpoints.add(this);
        logger.info("Client connected: {}", username);
	}

	@OnMessage
	public void onMessage(Session session, String lock) throws AlbinaException {
		BulletinLock bulletinLock = JsonUtil.parseUsingJackson(lock, BulletinLock.class);
		bulletinLock.setSessionId(session.getId());
		if (bulletinLock.getLock())
			AvalancheBulletinController.getInstance().lockBulletin(bulletinLock);
		else
			AvalancheBulletinController.getInstance().unlockBulletin(bulletinLock);
		broadcast(bulletinLock);
	}

	@OnClose
	public void onClose(Session session) {
		bulletinEndpoints.remove(this);
		AvalancheBulletinController.getInstance().unlockBulletins(session.getId());
        logger.info("Client disconnected: {}", session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.debug("Bulletin lock error", throwable);
	}

	public static void broadcast(BulletinLock lock) {
		bulletinEndpoints.forEach(endpoint -> {
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

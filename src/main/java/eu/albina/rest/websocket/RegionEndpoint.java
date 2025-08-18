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

import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.RegionLock;

@ServerEndpoint(value = "/region/{username}")
public class RegionEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(RegionEndpoint.class);

	private Session session;
	private static final Set<RegionEndpoint> regionEndpoints = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) {
		this.session = session;
		this.session.setMaxIdleTimeout(0); // never timeout due to inactivity
		regionEndpoints.add(this);
        logger.info("Client connected: {}", username);
	}

	@OnMessage
	public void onMessage(Session session, String lock) throws AlbinaException {
		RegionLock regionLock = JsonUtil.parseUsingJackson(lock, RegionLock.class);
		regionLock.setSessionId(session.getId());
		if (regionLock.getLock())
			RegionController.getInstance().lockRegion(regionLock);
		else
			RegionController.getInstance().unlockRegion(regionLock);
		broadcast(regionLock);
	}

	@OnClose
	public void onClose(Session session) {
		regionEndpoints.remove(this);
		RegionController.getInstance().unlockRegions(session.getId());
        logger.info("Client disconnected: {}", session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.debug("Region lock error", throwable);
	}

	public static void broadcast(RegionLock lock) {
		regionEndpoints.forEach(endpoint -> {
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

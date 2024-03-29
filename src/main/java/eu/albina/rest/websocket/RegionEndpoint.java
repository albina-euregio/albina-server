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

import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.RegionLock;

@ServerEndpoint(value = "/region/{username}", decoders = RegionLockDecoder.class, encoders = RegionLockEncoder.class)
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
		RegionLock regionLock = new RegionLock(new JSONObject(lock));
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

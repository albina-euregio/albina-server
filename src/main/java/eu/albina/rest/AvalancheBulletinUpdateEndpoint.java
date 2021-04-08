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
import eu.albina.util.BulletinUpdateDecoder;
import eu.albina.util.BulletinUpdateEncoder;

@ServerEndpoint(value = "/update/{username}", decoders = BulletinUpdateDecoder.class, encoders = BulletinUpdateEncoder.class)
public class AvalancheBulletinUpdateEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinUpdateEndpoint.class);

	private Session session;
	private static final Set<AvalancheBulletinUpdateEndpoint> updateEndpoints = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) {
		this.session = session;
		updateEndpoints.add(this);
		logger.info("Client connected: " + username);
	}

	@OnMessage
	public void onMessage(Session session, String lock) {
		// BulletinUpdate bulletinUpdate = new BulletinUpdate(new JSONObject(lock));
		// broadcast(bulletinUpdate);
	}

	@OnClose
	public void onClose(Session session) {
		updateEndpoints.remove(this);
		logger.info("Client disconnected: " + session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.error("Bulletin update error", throwable);
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

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

import eu.albina.exception.AlbinaException;
import eu.albina.model.BulletinUpdate;
import eu.albina.util.BulletinUpdateDecoder;
import eu.albina.util.BulletinUpdateEncoder;

@ServerEndpoint(value = "/update/{username}", decoders = BulletinUpdateDecoder.class, encoders = BulletinUpdateEncoder.class)
public class AvalancheBulletinUpdateEndpoint {

	private static Logger logger = LoggerFactory.getLogger(AvalancheBulletinUpdateEndpoint.class);

	private Session session;
	private static Set<AvalancheBulletinUpdateEndpoint> updateEndpoints = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {
		this.session = session;
		updateEndpoints.add(this);
		logger.info("Client connected: " + username);
	}

	@OnMessage
	public void onMessage(Session session, String lock) throws IOException, EncodeException, AlbinaException {
		// BulletinUpdate bulletinUpdate = new BulletinUpdate(new JSONObject(lock));
		// broadcast(bulletinUpdate);
	}

	@OnClose
	public void onClose(Session session) throws IOException, EncodeException {
		updateEndpoints.remove(this);
		logger.info("Client disconnected: " + session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.error("Bulletin update error: " + throwable.getMessage());
	}

	public static void broadcast(BulletinUpdate lock) throws IOException, EncodeException {
		updateEndpoints.forEach(endpoint -> {
			synchronized (endpoint) {
				try {
					endpoint.session.getBasicRemote().sendObject(lock);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
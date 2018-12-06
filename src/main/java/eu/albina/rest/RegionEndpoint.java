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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.RegionLock;
import eu.albina.util.RegionLockDecoder;
import eu.albina.util.RegionLockEncoder;

@ServerEndpoint(value = "/region/{username}", decoders = RegionLockDecoder.class, encoders = RegionLockEncoder.class)
public class RegionEndpoint {

	private static Logger logger = LoggerFactory.getLogger(RegionEndpoint.class);

	private Session session;
	private static Set<RegionEndpoint> regionEndpoints = new CopyOnWriteArraySet<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {
		this.session = session;
		regionEndpoints.add(this);
		logger.info("Client connected: " + username);
	}

	@OnMessage
	public void onMessage(Session session, String lock) throws IOException, EncodeException, AlbinaException {
		RegionLock regionLock = new RegionLock(new JSONObject(lock));
		regionLock.setSessionId(session.getId());
		if (regionLock.getLock())
			RegionController.getInstance().lockRegion(regionLock);
		else
			RegionController.getInstance().unlockRegion(regionLock);
		broadcast(regionLock);
	}

	@OnClose
	public void onClose(Session session) throws IOException, EncodeException {
		regionEndpoints.remove(this);
		RegionController.getInstance().unlockRegions(session.getId());
		logger.info("Client disconnected: " + session.getId());
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// Do error handling here
		logger.error("Region lock error: " + throwable.getMessage());
	}

	public static void broadcast(RegionLock lock) throws IOException, EncodeException {
		regionEndpoints.forEach(endpoint -> {
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
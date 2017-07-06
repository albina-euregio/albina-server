package eu.albina.controller;

import java.util.UUID;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import eu.albina.model.BulletinLock;
import eu.albina.model.ChatMessage;
import eu.albina.model.RegionLock;
import eu.albina.model.enumerations.EventName;
import eu.albina.util.GlobalVariables;

/**
 * Controller for socket IO.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class SocketIOController {

	private static Logger logger = LoggerFactory.getLogger(SocketIOController.class);
	private static SocketIOController instance = null;

	private SocketIOServer socketIOServer = null;

	private SocketIOController() {
	}

	public static SocketIOController getInstance() {
		if (instance == null) {
			instance = new SocketIOController();
		}
		return instance;
	}

	public void startSocketIO() {
		if (socketIOServer == null) {
			Configuration configuration = new Configuration();
			// configuration.setHostname("localhost");
			// configuration.setHostname("127.0.0.1");
			configuration.setPort(9092);
			socketIOServer = new SocketIOServer(configuration);

			socketIOServer.addConnectListener(new ConnectListener() {
				@Override
				public void onConnect(SocketIOClient client) {
					logger.debug("[SocketIO] Client connected!");
				}
			});

			socketIOServer.addDisconnectListener(new DisconnectListener() {
				@Override
				public void onDisconnect(SocketIOClient client) {
					UUID sessionId = client.getSessionId();
					RegionController.getInstance().unlockRegions(sessionId);
					AvalancheBulletinController.getInstance().unlockBulletins(sessionId);
					logger.debug("[SocketIO] Client disconnected: " + sessionId.toString());
				}
			});

			socketIOServer.addEventListener(EventName.bulletinUpdate.toString(), String.class,
					new DataListener<String>() {
						@Override
						public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
							// TODO save bulletin update
							sendEvent(EventName.bulletinUpdate.toString(), data);
						}
					});

			socketIOServer.addEventListener(EventName.chatEvent.toString(), String.class, new DataListener<String>() {
				@Override
				public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
					ChatController.getInstance().saveChatMessage(new ChatMessage(new JSONObject(data)));
					sendEvent(EventName.chatEvent.toString(), data);
				}
			});

			socketIOServer.addEventListener(EventName.lockBulletin.toString(), String.class,
					new DataListener<String>() {
						@Override
						public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
							sendEvent(EventName.lockBulletin.toString(), data);
							JSONObject message = new JSONObject(data);
							DateTime dateTime = null;
							if (message.has("date") && message.has("bulletin")) {
								dateTime = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);
								BulletinLock lock = new BulletinLock(client.getSessionId(),
										message.getString("bulletin"), dateTime);
								AvalancheBulletinController.getInstance().lockBulletin(lock);
							} else
								logger.warn("[SocketIO] Bulletin could not be locked!");
						}
					});

			socketIOServer.addEventListener(EventName.unlockBulletin.toString(), String.class,
					new DataListener<String>() {
						@Override
						public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
							sendEvent(EventName.unlockBulletin.toString(), data);
							JSONObject message = new JSONObject(data);
							DateTime dateTime = null;
							if (message.has("date") && message.has("bulletin")) {
								dateTime = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);
								AvalancheBulletinController.getInstance().unlockBulletin(message.getString("bulletin"),
										dateTime);
							} else
								logger.warn("[SocketIO] Bulletin could not be unlocked!");
						}
					});

			socketIOServer.addEventListener(EventName.lockRegion.toString(), String.class, new DataListener<String>() {
				@Override
				public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
					sendEvent(EventName.lockRegion.toString(), data);
					JSONObject message = new JSONObject(data);
					DateTime date = null;
					if (message.has("date") && message.has("region")) {
						date = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);
						logger.info("[lock region] date: " + date.toString());
						RegionLock lock = new RegionLock(client.getSessionId(), message.getString("region"), date);
						RegionController.getInstance().lockRegion(lock);
					} else
						logger.warn("[SocketIO] Region could not be locked!");
				}
			});

			socketIOServer.addEventListener(EventName.unlockRegion.toString(), String.class,
					new DataListener<String>() {
						@Override
						public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
							sendEvent(EventName.unlockRegion.toString(), data);
							JSONObject message = new JSONObject(data);
							DateTime date = null;
							if (message.has("date") && message.has("region")) {
								date = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);
								logger.info("[unlock region] date: " + date.toString());
								RegionController.getInstance().unlockRegion(message.getString("region"), date);
							} else
								logger.warn("[SocketIO] Region could not be unlocked!");
						}
					});

			socketIOServer.start();
		} else
			logger.info("SocketIOServer already running!");
	}

	public void sendEvent(String eventName, String data) {
		if (socketIOServer != null)
			socketIOServer.getBroadcastOperations().sendEvent(eventName, data);
		else
			logger.warn("SocketIOServer not started!");
	}

	public void stopSocketIO() {
		socketIOServer.stop();
	}
}

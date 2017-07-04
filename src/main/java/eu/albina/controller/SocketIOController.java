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

	private static SocketIOServer socketIOServer = null;

	public static void start() {
		Configuration configuration = new Configuration();
		// configuration.setHostname("localhost");
		// configuration.setHostname("127.0.0.1");
		configuration.setPort(9092);
		socketIOServer = new SocketIOServer(configuration);

		socketIOServer.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				// TODO implement
				logger.debug("[SocketIO] Client connected!");
			}
		});

		socketIOServer.addDisconnectListener(new DisconnectListener() {
			@Override
			public void onDisconnect(SocketIOClient client) {
				UUID sessionId = client.getSessionId();
				RegionController.getInstance().unlockRegion(sessionId);
				logger.debug("[SocketIO] Client disconnected: " + sessionId.toString());
			}
		});

		socketIOServer.addEventListener(EventName.bulletinUpdate.toString(), String.class, new DataListener<String>() {
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

		socketIOServer.addEventListener(EventName.notification.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				// TODO save notification
				sendEvent(EventName.notification.toString(), data);
			}
		});

		socketIOServer.addEventListener(EventName.lockBulletin.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				JSONObject message = new JSONObject(data);
				DateTime dateTime = null;
				if (message.has("date") && message.has("bulletinId")) {
					dateTime = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);

					AvalancheBulletinController.getInstance().lockBulletin(dateTime, message.getString("bulletinId"));
					sendEvent(EventName.lockBulletin.toString(), data);
				} else {
					logger.debug("[SocketIO] Bulletin could not be locked!");
				}
			}
		});

		socketIOServer.addEventListener(EventName.unlockBulletin.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				JSONObject message = new JSONObject(data);
				DateTime dateTime = null;
				if (message.has("date") && message.has("bulletinId")) {
					dateTime = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);

					AvalancheBulletinController.getInstance().unlockBulletin(dateTime, message.getString("bulletinId"));
					sendEvent(EventName.unlockBulletin.toString(), data);
				} else {
					logger.debug("[SocketIO] Bulletin could not be unlocked!");
				}
			}
		});

		socketIOServer.addEventListener(EventName.lockRegion.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				JSONObject message = new JSONObject(data);
				DateTime dateTime = null;
				if (message.has("date") && message.has("region")) {
					dateTime = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);

					RegionLock lock = new RegionLock(client.getSessionId(), message.getString("region"), dateTime);
					RegionController.getInstance().lockRegion(lock);

					sendEvent(EventName.lockRegion.toString(), data);
				} else {
					logger.debug("[SocketIO] Region could not be locked!");
				}
			}
		});

		socketIOServer.addEventListener(EventName.unlockRegion.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				JSONObject message = new JSONObject(data);
				DateTime dateTime = null;
				if (message.has("date") && message.has("region")) {
					dateTime = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);

					RegionController.getInstance().unlockRegion(message.getString("region"), dateTime);
					sendEvent(EventName.unlockRegion.toString(), data);
				} else {
					logger.debug("[SocketIO] Region could not be unlocked!");
				}
			}
		});

		socketIOServer.start();
	}

	public static void sendEvent(String eventName, String data) {
		socketIOServer.getBroadcastOperations().sendEvent(eventName, data);
	}

	public static void stop() {
		socketIOServer.stop();
	}
}

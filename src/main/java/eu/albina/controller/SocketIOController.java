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

import eu.albina.exception.AlbinaException;
import eu.albina.model.BulletinLock;
import eu.albina.model.ChatMessage;
import eu.albina.model.RegionLock;
import eu.albina.model.User;
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
					String username = client.getHandshakeData().getSingleUrlParam("username");
					if (username != null && !username.isEmpty()) {
						try {
							User user = UserController.getInstance().getUser(username);
							ChatController.getInstance().addActiveUser(user);
							sendEvent(EventName.login.toString(), user.toJSON().toString());
						} catch (AlbinaException e) {
							logger.warn("Active user could not be added: " + e.getMessage());
						}
					}
					logger.debug("Client connected: " + username + " [" + client.getSessionId() + "]");
				}
			});

			socketIOServer.addDisconnectListener(new DisconnectListener() {
				@Override
				public void onDisconnect(SocketIOClient client) {
					UUID sessionId = client.getSessionId();
					RegionController.getInstance().unlockRegions(sessionId);
					AvalancheBulletinController.getInstance().unlockBulletins(sessionId);
					String username = client.getHandshakeData().getSingleUrlParam("username");
					if (username != null && !username.isEmpty()) {
						try {
							User user = UserController.getInstance().getUser(username);
							ChatController.getInstance().deleteActiveUser(user);
							sendEvent(EventName.logout.toString(), user.toJSON().toString());
						} catch (AlbinaException e) {
							logger.warn("Active user could not be deleted: " + e.getMessage());
						}
					}
					logger.debug("Client disconnected: " + username + " [" + sessionId.toString() + "]");
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
								try {
									AvalancheBulletinController.getInstance().lockBulletin(lock);
									ackRequest.sendAckData(true);
								} catch (AlbinaException ae) {
									ackRequest.sendAckData(false);
								}
							} else {
								logger.warn("Bulletin could not be locked!");
								ackRequest.sendAckData(false);
							}
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
								try {
									AvalancheBulletinController.getInstance()
											.unlockBulletin(message.getString("bulletin"), dateTime);
									ackRequest.sendAckData(true);
								} catch (AlbinaException ae) {
									ackRequest.sendAckData(false);
								}
							} else {
								logger.warn("Bulletin could not be unlocked!");
								ackRequest.sendAckData(false);
							}
						}
					});

			socketIOServer.addEventListener(EventName.lockRegion.toString(), String.class, new DataListener<String>() {
				@Override
				public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
					JSONObject message = new JSONObject(data);
					DateTime date = null;
					if (message.has("date") && message.has("region")) {
						date = DateTime.parse(message.getString("date"), GlobalVariables.parserDateTime);
						RegionLock lock = new RegionLock(client.getSessionId(), message.getString("region"), date);
						try {
							RegionController.getInstance().lockRegion(lock);
							sendEvent(EventName.lockRegion.toString(), data);
							ackRequest.sendAckData(true);
							logger.info("Region locked: " + message.getString("region") + ", " + date.toString());
						} catch (AlbinaException ae) {
							logger.warn("Region could not be locked: " + message.getString("region") + ", "
									+ date.toString());
							ackRequest.sendAckData(false);
						}
					} else {
						logger.warn("Region could not be locked!");
						ackRequest.sendAckData(false);
					}
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
								try {
									RegionController.getInstance().unlockRegion(message.getString("region"), date);
									ackRequest.sendAckData(true);
									logger.info(
											"Region unlocked: " + message.getString("region") + ", " + date.toString());
								} catch (AlbinaException ae) {
									logger.warn("Region could not be unlocked: " + message.getString("region") + ", "
											+ date.toString());
									ackRequest.sendAckData(false);
								}
							} else {
								logger.warn("Region could not be unlocked!");
								ackRequest.sendAckData(false);
							}
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
		if (socketIOServer != null)
			socketIOServer.stop();
	}
}

package eu.albina.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import eu.albina.controller.ChatController;
import eu.albina.model.ChatMessage;
import eu.albina.model.enumerations.EventName;
import eu.albina.util.HibernateUtil;

@WebListener
public class AlbinaServiceContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaServiceContextListener.class);

	private SocketIOServer socketIOServer = null;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		HibernateUtil.closeSessionFactory();
		socketIOServer.stop();
		System.out.println("ServletContextListener destroyed");
	}

	// Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		HibernateUtil.createSessionFactory();

		Enumeration<Driver> drivers = DriverManager.getDrivers();
		Driver d = null;
		while (drivers.hasMoreElements()) {
			try {
				d = drivers.nextElement();
				DriverManager.deregisterDriver(d);
				logger.warn(String.format("Driver %s deregistered", d));
			} catch (SQLException ex) {
				logger.warn(String.format("Error deregistering driver %s", d), ex);
			}
		}
		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (InterruptedException e) {
			logger.warn("SEVERE problem cleaning up: " + e.getMessage());
			e.printStackTrace();
		}

		startSocketIO();

		logger.debug("ServletContextListener started");
	}

	private void startSocketIO() {
		Configuration configuration = new Configuration();
		// configuration.setHostname("localhost");
		// configuration.setHostname("127.0.0.1");
		configuration.setPort(9092);
		socketIOServer = new SocketIOServer(configuration);

		socketIOServer.addEventListener(EventName.bulletinUpdate.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				// TODO save bulletin update
				socketIOServer.getBroadcastOperations().sendEvent(EventName.bulletinUpdate.toString(), data);
			}
		});

		socketIOServer.addEventListener(EventName.chatEvent.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				socketIOServer.getBroadcastOperations().sendEvent(EventName.chatEvent.toString(), data);
				ChatController.getInstance().saveChatMessage(new ChatMessage(new JSONObject(data)));
			}
		});

		socketIOServer.addEventListener(EventName.notification.toString(), String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) throws Exception {
				// TODO save notification
				socketIOServer.getBroadcastOperations().sendEvent(EventName.notification.toString(), data);
			}
		});

		socketIOServer.start();
	}
}
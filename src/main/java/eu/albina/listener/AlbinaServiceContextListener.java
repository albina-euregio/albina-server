package eu.albina.listener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import eu.albina.controller.SocketIOController;
import eu.albina.util.EmailUtil;
import eu.albina.util.HibernateUtil;

@WebListener
public class AlbinaServiceContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaServiceContextListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		HibernateUtil.getInstance().shutDown();
		SocketIOController.getInstance().stopSocketIO();
		logger.debug("ServletContextListener destroyed");
	}

	// Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		HibernateUtil.getInstance().setUp();

		try {
			EmailUtil.getInstance().createFreemarkerConfigurationInstance();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		SocketIOController.getInstance().startSocketIO();

		logger.debug("ServletContextListener started");
	}
}
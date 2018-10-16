package eu.albina.listener;

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

import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.SchedulerUtil;

@WebListener
public class AlbinaServiceContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaServiceContextListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		HibernateUtil.getInstance().shutDown();
		SchedulerUtil.getInstance().shutDown();
		logger.debug("ServletContextListener destroyed");
	}

	// Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		GlobalVariables.loadConfigProperties();

		HibernateUtil.getInstance().setUp();

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

		SchedulerUtil.getInstance().setUp();
		SchedulerUtil.getInstance().start();

		logger.debug("ServletContextListener started");
	}
}
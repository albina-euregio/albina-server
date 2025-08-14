// SPDX-License-Identifier: AGPL-3.0-or-later
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

import eu.albina.util.DBMigration;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.SchedulerUtil;

/**
 * A {@code ServletContextListener} used to initialize the application.
 *
 * @author Norbert Lanzanasto
 *
 */
@WebListener
public class AlbinaServiceContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaServiceContextListener.class);

	/**
	 * Starting point of the application. Contains all necessary steps that have to
	 * be performed when the servlet is initialized. This runs before the
	 * application is started.
	 *
	 * @param arg0
	 */
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

		SchedulerUtil.getInstance().setUp();
		SchedulerUtil.getInstance().start();

		DBMigration.executeMigration();
		DBMigration.createAutoConfiguration();
		logger.debug("ServletContextListener started");
	}

	/**
	 * Contains all necessary steps that have to be performed when the servlet is
	 * destroyed.
	 *
	 * @param arg0
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		HibernateUtil.getInstance().shutDown();
		SchedulerUtil.getInstance().shutDown();
		logger.debug("ServletContextListener destroyed");
	}
}

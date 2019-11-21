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
import io.sentry.Sentry;

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
		Sentry.init();

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
		AbandonedConnectionCleanupThread.checkedShutdown();

		SchedulerUtil.getInstance().setUp();
		SchedulerUtil.getInstance().start();

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

package eu.albina;

import eu.albina.util.DBMigration;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.SchedulerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import io.micronaut.runtime.Micronaut;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
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
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Micronaut.run(Application.class, args);
    }
}

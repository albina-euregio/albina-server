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
//		GlobalVariables.loadConfigProperties();
//
//		SchedulerUtil.getInstance().setUp();
//		SchedulerUtil.getInstance().start();
		// TODO use micronaut way of schedulers by creating a Startup Listener Bean?

		logger.debug("ServletContextListener started");
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		Micronaut.run(Application.class, args);
	}
}

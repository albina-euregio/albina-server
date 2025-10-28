package eu.albina;

import com.google.common.io.Resources;
import io.micronaut.runtime.Micronaut;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class Application {

	private static void initLogging() throws IOException {
		// Hibernate
		System.setProperty("org.jboss.logging.provider", "slf4j");

		// java.util.logging.Logger
		if (System.getProperty("java.util.logging.config.file") != null) {
			LogManager.getLogManager().readConfiguration();
			return;
		}
		try (InputStream inputStream = Resources.getResource("logging.properties").openStream()) {
			LogManager.getLogManager().readConfiguration(inputStream);
		}
	}

	public static void main(String[] args) throws IOException {
		initLogging();
		Micronaut.run(Application.class, args);
	}
}

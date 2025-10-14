package eu.albina;

import io.micronaut.runtime.Micronaut;

public class Application {

	public static void main(String[] args) {
		System.setProperty("org.jboss.logging.provider", "slf4j");
		Micronaut.run(Application.class, args);
	}
}

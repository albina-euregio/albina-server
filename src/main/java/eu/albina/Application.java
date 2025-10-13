package eu.albina;

import eu.albina.util.HibernateUtil;
import io.micronaut.runtime.Micronaut;

public class Application {

	public static void main(String[] args) {
		System.setProperty("org.jboss.logging.provider", "slf4j");
		HibernateUtil.getInstance().setUp();

		Micronaut.run(Application.class, args);
	}
}

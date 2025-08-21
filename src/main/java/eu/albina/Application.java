package eu.albina;

import eu.albina.util.DBMigration;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;
import eu.albina.util.SchedulerUtil;
import io.micronaut.runtime.Micronaut;

public class Application {

	public static void main(String[] args) {
		GlobalVariables.loadConfigProperties();

		HibernateUtil.getInstance().setUp();

		SchedulerUtil.getInstance().setUp();
		SchedulerUtil.getInstance().start();

		DBMigration.executeMigration();
		DBMigration.createAutoConfiguration();

		Micronaut.run(Application.class, args);
	}
}

package eu.albina.util;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class DBMigration {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBMigration.class);

	public static void executeMigration() {
		if (!"true".equals(System.getenv("ALBINA_DB_RUN_MIGRATION"))) {
			LOGGER.info("database migration skipped");
			return;
		}

		LOGGER.info("start database migration");

		try (Session session = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager().unwrap(Session.class)) {

			session.doWork(connection -> {
				try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bout);) {
					Map<String, Object> config = new HashMap<>();
					config.put("liquibase.hub.mode", "off");

					Scope.child(config, () -> {
						JdbcConnection jdbcConnection = new JdbcConnection(connection);
						Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
						Liquibase liquibase = new Liquibase("/db/changelog-main.xml", new ClassLoaderResourceAccessor(), database);
						liquibase.update(new Contexts());
					});
				} catch (Exception e) {
					throw new HibernateException("database migration failed", e);
				}
			});
		}

		LOGGER.info("database migration finished");
	}

}

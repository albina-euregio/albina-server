package eu.albina.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Database configuration from environment
 */
public final class DBEnvConfig {

	public static final int NO_MAX_POOL_SIZE = -1;

	private static final Logger LOGGER = LoggerFactory.getLogger(DBEnvConfig.class);

	private static final String ALBINA_DB_CONNECTION_USERNAME = "ALBINA_DB_CONNECTION_USERNAME";
	private static final String ALBINA_DB_CONNECTION_PASSWORD = "ALBINA_DB_CONNECTION_PASSWORD";
	private static final String ALBINA_DB_CONNECTIONPOOL_MAXSIZE = "ALBINA_DB_CONNECTIONPOOL_MAXSIZE";
	private static final String ALBINA_DB_CONNECTION_URL = "ALBINA_DB_CONNECTION_URL";

	private static final DBEnvConfig DB_ENV_CONFIG = initConfig(System.getenv());

	private final String dbUser;
	private final String dbpw;
	private final String connectionUrl;
	private final int maxPoolsSize;

	private DBEnvConfig(Properties env) {
		dbUser = env.getProperty(ALBINA_DB_CONNECTION_USERNAME);
		dbpw = env.getProperty(ALBINA_DB_CONNECTION_PASSWORD);
		connectionUrl = env.getProperty(ALBINA_DB_CONNECTION_URL);

		int poolsize = NO_MAX_POOL_SIZE;
		if(env.containsKey(ALBINA_DB_CONNECTIONPOOL_MAXSIZE)) {
			String pms = env.getProperty(ALBINA_DB_CONNECTIONPOOL_MAXSIZE);

			try {
				poolsize = Integer.parseInt(pms);
			} catch (NumberFormatException e) {
				LOGGER.error("parse db connection max pool size '{}' failed", pms, e);
			}
		}
		maxPoolsSize = poolsize;

		LOGGER.info("database configuration from environment parsed");
	}

	public String getDbUser() { return dbUser; }

	public String getDbPassword() { return dbpw; }

	public int getDbConnectionPoolMaxSize() { return maxPoolsSize; }

	public String getDbConnectionUrl() { return connectionUrl; }

	public static DBEnvConfig instance() { return DB_ENV_CONFIG; }

	static DBEnvConfig initConfig(Map<String, String> systemEnv) {
		Properties env = new Properties();

		String[] keys = {
			ALBINA_DB_CONNECTION_USERNAME,
			ALBINA_DB_CONNECTION_PASSWORD,
			ALBINA_DB_CONNECTIONPOOL_MAXSIZE,
			ALBINA_DB_CONNECTION_URL
		};

		for(String key : keys) {
			if(systemEnv.containsKey(key)) {
				env.setProperty(key, systemEnv.get(key));
			}
		}

		return new DBEnvConfig(env);
	}

}

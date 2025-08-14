// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final String dbUser;
	private final String dbPassword;
	private final String connectionUrl;
	private final int maxPoolsSize;

	private DBEnvConfig(Properties env) {
		dbUser = env.getProperty(ALBINA_DB_CONNECTION_USERNAME);
		dbPassword = env.getProperty(ALBINA_DB_CONNECTION_PASSWORD);
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

	public String getDbPassword() { return dbPassword; }

	public int getDbConnectionPoolMaxSize() { return maxPoolsSize; }

	public String getDbConnectionUrl() { return connectionUrl; }

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

	Map<String, String> asMap() {
		Map<String, String> properties = new HashMap<>();
		if (connectionUrl != null) {
			properties.put("hibernate.connection.url", connectionUrl);
		}
		if (dbUser != null) {
			properties.put("hibernate.connection.username", dbUser);
		}
		if (dbPassword != null) {
			properties.put("hibernate.connection.password", dbPassword);
		}
		if (DBEnvConfig.NO_MAX_POOL_SIZE != maxPoolsSize) {
			properties.put("hibernate.c3p0.max_size", Integer.toString(maxPoolsSize));
		}
		return properties;
	}

}

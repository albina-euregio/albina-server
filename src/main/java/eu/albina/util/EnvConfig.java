package eu.albina.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration from environment
 */
public final class EnvConfig {

	public static final int NO_MAX_POOL_SIZE = -1;

	private static final Logger LOGGER = LoggerFactory.getLogger(EnvConfig.class);

	private static final String ALBINA_SERVER_CONFIG_FILE = "ALBINA_SERVER_CONFIG_FILE";
	private static final String ALBINA_DB_CONNECTION_USERNAME = "ALBINA_DB_CONNECTION_USERNAME";
	private static final String ALBINA_DB_CONNECTION_PASSWORD = "ALBINA_DB_CONNECTION_PASSWORD";
	private static final String ALBINA_DB_CONNECTION_POOL_MAX_SIZE = "ALBINA_DB_CONNECTION_POOL_MAX_SIZE";
	private static final String ALBINA_DB_CONNECTION_URL = "ALBINA_DB_CONNECTION_URL";

	private static final EnvConfig ENV_CONFIG = initConfig(System.getenv());

	private final String dbUser;
	private final String dbpw;
	private final String connectionUrl;
	private final int maxPoolsSize;

	private EnvConfig(Properties env) {
		dbUser = env.getProperty(ALBINA_DB_CONNECTION_USERNAME);
		dbpw = env.getProperty(ALBINA_DB_CONNECTION_PASSWORD);
		connectionUrl = env.getProperty(ALBINA_DB_CONNECTION_URL);

		int poolsize = NO_MAX_POOL_SIZE;
		if(env.containsKey(ALBINA_DB_CONNECTION_POOL_MAX_SIZE)) {
			String pms = env.getProperty(ALBINA_DB_CONNECTION_POOL_MAX_SIZE);

			try {
				poolsize = Integer.parseInt(pms);
			} catch (NumberFormatException e) {
				LOGGER.error("parse db connection max pool size '{}' failed", pms, e);
			}
		}
		maxPoolsSize = poolsize;

		LOGGER.info("environment configuration created");
	}

	public String getDbUser() { return dbUser; }

	public String getDbPassword() { return dbpw; }

	public int getDbConnectionPoolMaxSize() { return maxPoolsSize; }

	public String getDbConnectionUrl() { return connectionUrl; }

	public static EnvConfig instance() { return ENV_CONFIG; }

	static EnvConfig initConfig(Map<String, String> systemEnv) {
		Properties env = new Properties();

		if(systemEnv.containsKey(ALBINA_SERVER_CONFIG_FILE)) {
			String filepath = systemEnv.get(ALBINA_SERVER_CONFIG_FILE);
			try(InputStream in = Files.newInputStream(Paths.get(filepath));
				InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				env.load(r);
			} catch (IOException e) {
				LOGGER.error("reading configuration file {} failed.", filepath, e );
			}
		}

		String[] keys = {
			ALBINA_DB_CONNECTION_USERNAME,
			ALBINA_DB_CONNECTION_PASSWORD,
			ALBINA_DB_CONNECTION_POOL_MAX_SIZE,
			ALBINA_DB_CONNECTION_URL
		};

		for(String key : keys) {
			if(systemEnv.containsKey(key)) {
				env.setProperty(key, systemEnv.get(key));
			}
		}

		return new EnvConfig(env);
	}

}

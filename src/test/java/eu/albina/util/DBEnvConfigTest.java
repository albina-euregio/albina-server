package eu.albina.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static eu.albina.util.DBEnvConfig.NO_MAX_POOL_SIZE;
import static org.junit.jupiter.api.Assertions.*;

class DBEnvConfigTest {

	private Map<String, String> systemEnv;

	@BeforeEach
	void setUp() {
		systemEnv = new HashMap<>();
	}

	@AfterEach
	void tearDown() {
		systemEnv = null;
	}

	@Test
	void testNoEnvironmentSetting() {
		DBEnvConfig ec = DBEnvConfig.initConfig(Collections.emptyMap());
		assertAll(() -> assertNull(ec.getDbUser()),
			() -> assertNull(ec.getDbPassword()),
			() -> assertEquals(NO_MAX_POOL_SIZE,ec.getDbConnectionPoolMaxSize()),
			() -> assertNull(ec.getDbConnectionUrl()));
	}

	@Test
	void testGetDbUserFromEnvironmentVariable() {
		String expectedUser = UUID.randomUUID().toString();
		systemEnv.put("ALBINA_DB_CONNECTION_USERNAME", expectedUser);
		DBEnvConfig ec = DBEnvConfig.initConfig(systemEnv);
		assertEquals(expectedUser, ec.getDbUser());
	}

	@Test
	void testGetDbPasswordFromEnvironmentVariable() {
		String expectedPw = UUID.randomUUID().toString();
		systemEnv.put("ALBINA_DB_CONNECTION_PASSWORD", expectedPw);
		DBEnvConfig ec = DBEnvConfig.initConfig(systemEnv);
		assertEquals(expectedPw, ec.getDbPassword());
	}
	@Test
	void testGetMaxPoolSizeFromEnvironmentVariable() {
		int expectedMaxPoolSize = 10;
		systemEnv.put("ALBINA_DB_CONNECTIONPOOL_MAXSIZE", Integer.toString(expectedMaxPoolSize));
		DBEnvConfig ec = DBEnvConfig.initConfig(systemEnv);
		assertEquals(expectedMaxPoolSize, ec.getDbConnectionPoolMaxSize());
	}

	@Test
	void testGetConnectionStringFromEnvironmentVariable() {
		String expectedConnectionUrl = UUID.randomUUID().toString();
		systemEnv.put("ALBINA_DB_CONNECTION_URL", expectedConnectionUrl);
		DBEnvConfig ec = DBEnvConfig.initConfig(systemEnv);
		assertEquals(expectedConnectionUrl, ec.getDbConnectionUrl());
	}

}

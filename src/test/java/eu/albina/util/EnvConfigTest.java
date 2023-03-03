package eu.albina.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static eu.albina.util.EnvConfig.NO_MAX_POOL_SIZE;
import static org.junit.jupiter.api.Assertions.*;

class EnvConfigTest {

	private Map<String, String> systemEnv;

	@BeforeEach
	void setUp() {
		Map<String, String> se = new HashMap<>();
		se.put("ALBINA_SERVER_CONFIG_FILE", "src/test/resources/env/albina-server.env");
		systemEnv = se;
	}

	@AfterEach
	void tearDown() {
		systemEnv = null;
	}

	@Test
	void testNoEnvironmentSetting() {
		EnvConfig ec = EnvConfig.initConfig(Collections.emptyMap());
		assertAll(() -> assertNull(ec.getDbUser()),
			() -> assertNull(ec.getDbPassword()),
			() -> assertEquals(NO_MAX_POOL_SIZE,ec.getDbConnectionPoolMaxSize()),
			() -> assertNull(ec.getDbConnectionUrl()));
	}

	@Test
	void testConfigurationFromFile() {
		EnvConfig ec = EnvConfig.initConfig(systemEnv);
		assertAll(() -> assertEquals("connectionusername", ec.getDbUser()),
			() -> assertEquals("connectionpassword", ec.getDbPassword()),
			() -> assertEquals(30, ec.getDbConnectionPoolMaxSize()),
			() -> assertEquals("connectionurl", ec.getDbConnectionUrl()));
	}

	@Test
	void testGetDbUserFromEnvironmentVariable() {
		String expectedUser = UUID.randomUUID().toString();
		systemEnv.put("ALBINA_DB_CONNECTION_USERNAME", expectedUser);
		EnvConfig ec = EnvConfig.initConfig(systemEnv);
		assertEquals(expectedUser, ec.getDbUser());
	}

	@Test
	void testGetDbPasswordFromEnvironmentVariable() {
		String expectedPw = UUID.randomUUID().toString();
		systemEnv.put("ALBINA_DB_CONNECTION_PASSWORD", expectedPw);
		EnvConfig ec = EnvConfig.initConfig(systemEnv);
		assertEquals(expectedPw, ec.getDbPassword());
	}
	@Test
	void testGetMaxPoolSizeFromEnvironmentVariable() {
		int expectedMaxPoolSize = 10;
		systemEnv.put("ALBINA_DB_CONNECTION_POOL_MAX_SIZE", Integer.toString(expectedMaxPoolSize));
		EnvConfig ec = EnvConfig.initConfig(systemEnv);
		assertEquals(expectedMaxPoolSize, ec.getDbConnectionPoolMaxSize());
	}

	@Test
	void testGetConnectionStringFromEnvironmentVariable() {
		String expectedConnectionUrl = UUID.randomUUID().toString();
		systemEnv.put("ALBINA_DB_CONNECTION_URL", expectedConnectionUrl);
		EnvConfig ec = EnvConfig.initConfig(systemEnv);
		assertEquals(expectedConnectionUrl, ec.getDbConnectionUrl());
	}

}

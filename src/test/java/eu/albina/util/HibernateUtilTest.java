package eu.albina.util;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl;
import org.junit.jupiter.api.Test;

import javax.persistence.Persistence;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class HibernateUtilTest {

	@Test
	void createSchema() throws Exception {
		Path path = Paths.get("src/main/resources/sql/00-create.sql");
		Files.deleteIfExists(path);
		Map<String, Object> properties = new HashMap<>();
		properties.put("hibernate.dialect", MySQL8Dialect.class.getName());
		properties.put("hibernate.connection.provider_class", UserSuppliedConnectionProviderImpl.class.getName());
		properties.put("javax.persistence.schema-generation.scripts.action", "create");
		properties.put("javax.persistence.schema-generation.scripts.create-target", path.toString());
		Persistence.generateSchema("eu.albina", properties);
	}
}

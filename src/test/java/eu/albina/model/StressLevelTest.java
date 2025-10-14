// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class StressLevelTest {

	@Inject
	ObjectMapper objectMapper;

	@Test
	void testRandomization() throws Exception {
		StressLevel l1 = new StressLevel();
		l1.setUser(new User("secret1@example.com"));
		l1.setStressLevel(45);
		StressLevel l2 = new StressLevel();
		l2.setUser(new User("secret2@example.com"));
		l2.setStressLevel(45);
		Map<UUID, List<StressLevel>> x = StressLevel.randomizeUsers(List.of(l1, l2));
		assertFalse(objectMapper.writeValueAsString(x).contains("@example.com"),
			"Found unexpected '@'. Randomization of users emails does not work.");
	}

}

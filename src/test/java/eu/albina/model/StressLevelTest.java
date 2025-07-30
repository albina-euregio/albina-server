package eu.albina.model;

import eu.albina.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

class StressLevelTest {

	@Test
	void testRandomization() throws Exception {
		StressLevel l1 = new StressLevel();
		l1.setUser(new User("secret1@example.com"));
		l1.setStressLevel(45);
		StressLevel l2 = new StressLevel();
		l2.setUser(new User("secret2@example.com"));
		l2.setStressLevel(45);
		Map<UUID, List<StressLevel>> x = StressLevel.randomizeUsers(List.of(l1, l2));
		assertFalse(JsonUtil.ALBINA_OBJECT_MAPPER.writeValueAsString(x).contains("@example.com"),
			"Found unexpected '@'. Randomization of users emails does not work.");
	}

}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import eu.albina.util.JsonUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class IncidentViewTest {

	@Inject
	ObjectMapper objectMapper;

	private Incident sampleIncident() throws IOException {
		Incident incident = new Incident();
		incident.setId("incident-1");
		incident.setData(objectMapper.readValue("{\"secret\":\"internal\"}", Object.class));
		incident.setPublishedAt(java.time.Instant.parse("2026-01-02T03:04:05Z"));
		incident.setPublicData(objectMapper.readValue("{\"summary\":\"public\"}", Object.class));
		incident.prePersist(); // populate createdAt/updatedAt
		return incident;
	}

	@Test
	public void publicViewExposesOnlyIdPublishedAtPublicData() throws IOException {
		String json = objectMapper.cloneWithViewClass(JsonUtil.Views.Public.class)
			.writeValueAsString(sampleIncident());
		assertTrue(json.contains("\"id\""), json);
		assertTrue(json.contains("\"publishedAt\""), json);
		assertTrue(json.contains("\"publicData\""), json);
		// internal-only fields must not leak
		assertFalse(json.contains("\"data\""), json);
		assertFalse(json.contains("\"createdAt\""), json);
		assertFalse(json.contains("\"updatedAt\""), json);
		assertFalse(json.contains("internal"), json);
	}

	@Test
	public void internalViewExposesData() throws IOException {
		String json = objectMapper.cloneWithViewClass(JsonUtil.Views.Internal.class)
			.writeValueAsString(sampleIncident());
		assertTrue(json.contains("\"data\""), json);
		assertTrue(json.contains("\"publishedAt\""), json);
		assertTrue(json.contains("\"publicData\""), json);
		// internal payload is exposed in the internal view
		assertTrue(json.contains("\"data\""), json);
		assertTrue(json.contains("\"createdAt\""), json);
		assertTrue(json.contains("\"updatedAt\""), json);
		assertTrue(json.contains("internal"), json);
	}
}

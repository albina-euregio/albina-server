// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class IncidentServiceTest {

	@Inject
	ObjectMapper objectMapper;

	private static final UUID PUBLIC_ATTACHMENT = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVATE_ATTACHMENT = UUID.fromString("22222222-2222-2222-2222-222222222222");

	/** Public data as published by the client: attachments plus unrelated fields which must be ignored. */
	private static final String PUBLIC_DATA = """
		{
			"id": "44444444-4444-4444-4444-444444444444",
			"dateTime": "2026-01-02T03:04:05Z",
			"incidentLede": {"de": "Lawinenunfall"},
			"attachments": [
				{"id": "11111111-1111-1111-1111-111111111111", "public": true,
					"fileName": "avalanche.jpg", "mediaType": "image/jpeg",
					"attachmentCategory": "Avalanche", "attachmentTags": ["overview"],
					"dateAdded": "2026-01-02T03:04:05Z"},
				{"id": "22222222-2222-2222-2222-222222222222", "public": false,
					"fileName": "victim.jpg", "mediaType": "image/jpeg"},
				{"id": "33333333-3333-3333-3333-333333333333", "fileName": "unflagged.jpg"}
			]
		}
		""";

	@Test
	public void parsesAttachmentsAndIgnoresOtherFields() throws IOException {
		IncidentService.PublicAttachments publicData =
			objectMapper.readValue(PUBLIC_DATA, IncidentService.PublicAttachments.class);
		assertEquals(3, publicData.attachments().size());
		assertEquals(PUBLIC_ATTACHMENT, publicData.attachments().get(0).id());
		assertEquals(Boolean.TRUE, publicData.attachments().get(0).isPublic());
		assertEquals(PRIVATE_ATTACHMENT, publicData.attachments().get(1).id());
		assertEquals(Boolean.FALSE, publicData.attachments().get(1).isPublic());
		// an attachment without the public flag must not be treated as public
		assertNull(publicData.attachments().get(2).isPublic());
	}

	@Test
	public void parsesPublicDataWithoutAttachments() throws IOException {
		IncidentService.PublicAttachments publicData =
			objectMapper.readValue("{\"incidentLede\": {\"de\": \"Lawinenunfall\"}}",
				IncidentService.PublicAttachments.class);
		assertNull(publicData.attachments());
	}

	/** The endpoint serializes the stored public data before parsing it, so both directions must work. */
	@Test
	public void roundTripsThroughSerialization() throws IOException {
		Object publicData = objectMapper.readValue(PUBLIC_DATA, Object.class);
		IncidentService.PublicAttachments parsed = objectMapper.readValue(
			objectMapper.writeValueAsBytes(publicData), IncidentService.PublicAttachments.class);
		assertTrue(parsed.attachments().stream()
			.anyMatch(a -> PUBLIC_ATTACHMENT.equals(a.id()) && Boolean.TRUE.equals(a.isPublic())));
	}
}

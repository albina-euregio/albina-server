// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record IncidentAttachment(
	UUID uuid,
	Instant dateAdded,
	byte[] file,
	String fileName,
	String mediaType,
	Instant dateCreated,
	String credit,
	String caption,
	String altText,
	@JsonProperty("public") Boolean isPublic,
	String attachmentCategory,
	List<String> attachmentTags) {

	public IncidentAttachment(UUID uuid, Instant dateAdded, byte[] file, String fileName, String mediaType) {
		this(uuid, dateAdded, file, fileName, mediaType, null, "", "", "", null, null, List.of());
	}
}

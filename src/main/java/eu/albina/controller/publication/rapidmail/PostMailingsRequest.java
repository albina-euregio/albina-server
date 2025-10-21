// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.rapidmail;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
record PostMailingsRequest(
	@JsonProperty("destinations") List<Destination> destinations,
	@JsonProperty("file") File file,
	@JsonProperty("from_email") String fromEmail,
	@JsonProperty("from_name") String fromName,
	@JsonProperty("host") String host,
	@JsonProperty("send_at") String sendAt,
	@JsonProperty("status") String status,
	@JsonProperty("subject") String subject
) {
	@Serdeable
	record Destination(
		@JsonProperty("action") String action,
		@JsonProperty("id") Integer id,
		@JsonProperty("type") String type
	) {
	}

	@Serdeable
	record File(
		@JsonProperty("description") String description,
		@JsonProperty("type") String type,
		@JsonProperty("content") String content
	) {
	}
}

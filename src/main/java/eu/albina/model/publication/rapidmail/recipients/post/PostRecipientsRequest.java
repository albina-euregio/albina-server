// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipients.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record PostRecipientsRequest(
	@JsonProperty("email") String email,
	@JsonProperty("recipientlist_id") Integer recipientlistId
) {
}

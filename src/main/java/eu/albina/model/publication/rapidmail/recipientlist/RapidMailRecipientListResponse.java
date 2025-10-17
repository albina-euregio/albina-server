// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipientlist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
public record RapidMailRecipientListResponse(
	@JsonProperty("_embedded") Embedded embedded,
	@JsonProperty("page") int page,
	@JsonProperty("total_items") int totalItems,
	@JsonProperty("page_count") int pageCount,
	@JsonProperty("page_size") int pageSize) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Serdeable
	public record Embedded(
		@JsonProperty("recipientlists") List<Item> recipientlists) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Serdeable
	public record Item(
		@JsonProperty("created") String created,
		@JsonProperty("default") String jsonMemberDefault,
		@JsonProperty("description") String description,
		@JsonProperty("id") Integer id,
		@JsonProperty("name") String name,
		@JsonProperty("recipient_subscribe_email") String recipientSubscribeEmail,
		@JsonProperty("subscribe_confirmation_email_body") String subscribeConfirmationEmailBody,
		@JsonProperty("subscribe_confirmation_email_body_html") String subscribeConfirmationEmailBodyHtml,
		@JsonProperty("subscribe_confirmation_email_from") String subscribeConfirmationEmailFrom,
		@JsonProperty("subscribe_confirmation_email_from_name") String subscribeConfirmationEmailFromName,
		@JsonProperty("subscribe_confirmation_email_subject") String subscribeConfirmationEmailSubject,
		@JsonProperty("subscribe_confirmation_welcome_email_body") String subscribeConfirmationWelcomeEmailBody,
		@JsonProperty("subscribe_confirmation_welcome_email_body_html") String subscribeConfirmationWelcomeEmailBodyHtml,
		@JsonProperty("subscribe_confirmation_welcome_email_from") String subscribeConfirmationWelcomeEmailFrom,
		@JsonProperty("subscribe_confirmation_welcome_email_from_name") String subscribeConfirmationWelcomeEmailFromName,
		@JsonProperty("subscribe_confirmation_welcome_email_subject") String subscribeConfirmationWelcomeEmailSubject,
		@JsonProperty("subscribe_form_field_key") String subscribeFormFieldKey,
		@JsonProperty("subscribe_form_url") String subscribeFormUrl,
		@JsonProperty("unsubscribe_blacklist") String unsubscribeBlacklist,
		@JsonProperty("unsubscribe_confirmation_email_body") String unsubscribeConfirmationEmailBody,
		@JsonProperty("unsubscribe_confirmation_email_body_html") String unsubscribeConfirmationEmailBodyHtml,
		@JsonProperty("unsubscribe_confirmation_email_from") String unsubscribeConfirmationEmailFrom,
		@JsonProperty("unsubscribe_confirmation_email_from_name") String unsubscribeConfirmationEmailFromName,
		@JsonProperty("unsubscribe_confirmation_email_subject") String unsubscribeConfirmationEmailSubject,
		@JsonProperty("unsubscribe_confirmation_goodbye_email_body") String unsubscribeConfirmationGoodbyeEmailBody,
		@JsonProperty("unsubscribe_confirmation_goodbye_email_body_html") String unsubscribeConfirmationGoodbyeEmailBodyHtml,
		@JsonProperty("unsubscribe_confirmation_goodbye_email_from") String unsubscribeConfirmationGoodbyeEmailFrom,
		@JsonProperty("unsubscribe_confirmation_goodbye_email_from_name") String unsubscribeConfirmationGoodbyeEmailFromName,
		@JsonProperty("unsubscribe_confirmation_goodbye_email_subject") String unsubscribeConfirmationGoodbyeEmailSubject,
		@JsonProperty("unsubscribe_form_url") String unsubscribeFormUrl,
		@JsonProperty("updated") String updated
	) {
	}
}

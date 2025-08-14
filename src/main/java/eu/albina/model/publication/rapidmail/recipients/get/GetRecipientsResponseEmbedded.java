// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.publication.rapidmail.recipients.get;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRecipientsResponseEmbedded {

	@JsonProperty("recipients")
	private List<GetRecipientsResponseItem> recipients;

	public void setRecipients(List<GetRecipientsResponseItem> recipients) {
		this.recipients = recipients;
	}

	public List<GetRecipientsResponseItem> getRecipients() {
		return recipients;
	}

	@Override
	public String toString() {
		return "Embedded{" + "recipients = '" + recipients + '\'' + "}";
	}
}

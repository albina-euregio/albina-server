package ch.rasc.webpush;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubscriptionKeys(
	@JsonProperty("p256dh") String p256dh,
	@JsonProperty("auth") String auth) {
}

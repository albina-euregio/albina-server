// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.common.base.MoreObjects;

import eu.albina.model.enumerations.LanguageCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "SUBSCRIBE_DATE")
	private ZonedDateTime subscribeDate = ZonedDateTime.now();

	@Column(name = "AUTH", length = 191)
	private String auth;

	@Column(name = "P256DH", length = 191)
	private String p256dh;

	@Column(name = "ENDPOINT", columnDefinition = "longtext")
	private String endpoint;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE", length = 191)
	private LanguageCode language;

	@Column(name = "REGION_ID", length = 191)
	private String region;

	@Column(name = "FAILED_COUNT")
	private int failedCount;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Instant getSubscribeDate() {
		return subscribeDate.toInstant();
	}

	public void setSubscribeDate(Instant subscribeDate) {
		this.subscribeDate = ZonedDateTime.ofInstant(subscribeDate, ZoneId.of("UTC"));
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getP256dh() {
		return p256dh;
	}

	public void setP256dh(String p256dh) {
		this.p256dh = p256dh;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public LanguageCode getLanguage() {
		return language;
	}

	public void setLanguage(LanguageCode language) {
		this.language = language;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("subscribeDate", subscribeDate)
			.add("auth", auth)
			.add("p256dh", p256dh)
			.add("endpoint", endpoint)
			.add("language", language)
			.add("region", region)
			.add("failedCount", failedCount)
			.toString();
	}
}

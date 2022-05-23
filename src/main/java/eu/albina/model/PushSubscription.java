/*******************************************************************************
 * Copyright (C) 2021 albina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

import eu.albina.model.enumerations.LanguageCode;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "SUBSCRIBE_DATE")
	private ZonedDateTime subscribeDate = ZonedDateTime.now();

	@Column(name = "AUTH")
	private String auth;

	@Column(name = "P256DH")
	private String p256dh;

	@Column(name = "ENDPOINT", length = 1023)
	private String endpoint;

	@Enumerated(EnumType.STRING)
	@Column(name = "LANGUAGE_CODE")
	private LanguageCode language;

	@Column(name = "REGION_ID")
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

	public static List<PushSubscription> getTestSubscriptions(LanguageCode language) {
		List<PushSubscription> subscriptions = new ArrayList<PushSubscription>();
		PushSubscription subscription = new PushSubscription();
		subscription.setAuth("rObvUclXOeAWwjfjn9J7Aw");
		subscription.setEndpoint("https://fcm.googleapis.com/fcm/send/cRdhlR3U36w:APA91bGjFKRktO65yjf3PGB1VZAoLLkRo2OgwahMG5u17GtWy82BJavc4Hfuo-zuxHT0SWEjODNEF07PSD6DFgtWgtc3DhZf2LKGvsKK2B7p29Q4pv6Kd_-9uaoxTD5JGX1RzDSFQJNg");
		subscription.setFailedCount(0);
		subscription.setId(24L);
		subscription.setLanguage(language);
		subscription.setP256dh("BH9E3G1nCGH1RP3WrW7c0j93OUeY4yswq_Tbw0KOpMViVPgkgzT-S-PSc1SCOooO72PjiYCQE50cXCSueDELzmE");
		subscription.setRegion("AT-07");
		subscription.setSubscribeDate(Instant.now());
		subscriptions.add(subscription);
		return subscriptions;
	}
}

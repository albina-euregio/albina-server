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
package eu.albina.controller.publication;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.rasc.webpush.CryptoService;
import ch.rasc.webpush.PushController;
import ch.rasc.webpush.ServerKeys;
import ch.rasc.webpush.dto.Subscription;
import ch.rasc.webpush.dto.SubscriptionKeys;
import eu.albina.exception.AlbinaException;
import eu.albina.util.HibernateUtil;
import eu.albina.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONObject;

import eu.albina.controller.PushSubscriptionController;
import eu.albina.model.PushSubscription;
import eu.albina.model.publication.PushConfiguration;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

public class PushNotificationUtil {

	private static final Logger logger = LoggerFactory.getLogger(PushNotificationUtil.class);
	private final Client client;

	public PushNotificationUtil() {
		this(HttpClientUtil.newClientBuilder().build());
	}

	protected PushNotificationUtil(Client client) {
		this.client = client;
	}

	public void send(MultichannelMessage posting) {
		final JSONObject payload = new JSONObject();
		payload.put("title", posting.getLanguageCode().getBundleString("website.name"));
		payload.put("body", posting.getSocialMediaText());
		payload.put("image", posting.getAttachmentUrl());
		payload.put("url", posting.getWebsiteUrl());

        List<PushSubscription> subscriptions = PushSubscriptionController.get(posting.getLanguageCode(), Collections.singleton(posting.getRegion().getId()));
        logger.info("Sending {} push notifications for language={} regions={}: {}", subscriptions.size(), posting.getLanguageCode(), posting.getRegion(), payload);
        for (PushSubscription subscription : subscriptions) {
            sendPushMessage(subscription, payload, null);
        }
    }

	public void sendWelcomePushMessage(PushSubscription subscription) {
		final JSONObject payload = new JSONObject();
		payload.put("title", subscription.getLanguage().getBundleString("website.name"));
		payload.put("body", "Hello World!");
		sendPushMessage(subscription, payload, null);
	}


	public static Optional<PushConfiguration> getConfiguration() {
		return HibernateUtil.getInstance().run(entityManager -> {
			try {
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<PushConfiguration> select = criteriaBuilder.createQuery(PushConfiguration.class);
				select.from(PushConfiguration.class);
				PushConfiguration configuration = entityManager.createQuery(select).getSingleResult();
				return Optional.ofNullable(configuration);
			} catch (PersistenceException e) {
				return Optional.empty();
			}
		});
	}

	public void sendPushMessage(PushSubscription subscription, JSONObject payload, ServerKeys serverKeys) {
		try {
			logger.debug("Sending push notification to {}", subscription.getEndpoint());
			if (serverKeys == null) {
				PushConfiguration configuration = getConfiguration().orElseThrow();
				serverKeys = new ServerKeys(configuration.getVapidPublicKey(), configuration.getVapidPrivateKey());
			}
			final SubscriptionKeys subscriptionKeys = new SubscriptionKeys(subscription.getP256dh(), subscription.getAuth());
			final Subscription subscription1 = new Subscription(subscription.getEndpoint(), null, subscriptionKeys);
			final byte[] encrypted = new CryptoService().encrypt(payload.toString(), subscriptionKeys, 0);
			final URI endpointURI = URI.create(subscription1.getEndpoint());
			final Invocation.Builder builder = client.target(endpointURI).request();
			builder.header("Content-Type", "application/octet-stream");
			builder.header("TTL", "180");
			builder.header("Authorization", new PushController(serverKeys).getAuthorization(endpointURI));
			final Response response = builder.post(Entity.entity(encrypted, new Variant(MediaType.APPLICATION_OCTET_STREAM_TYPE, (String) null, "aes128gcm")));
			logger.debug("Received response on POST: {}", response.getStatusInfo());
			if (response.getStatusInfo().getStatusCode() != 200 && response.getStatusInfo().getStatusCode() != 201) {
				throw new AlbinaException(response.getStatusInfo() + " " + response.readEntity(String.class));
			}
			logger.debug("Successfully sent push notification to {}", subscription.getEndpoint());
		} catch (Exception e) {
			logger.warn("Failed to send push notification to " + subscription.getEndpoint(), e);
			if (subscription.getFailedCount() >= 10) {
				try {
					logger.warn("Deleting subscription {} with failed count {}", subscription.getEndpoint(), subscription.getFailedCount());
					PushSubscriptionController.delete(subscription);
				} catch (Exception e2) {
					logger.warn("Failed to delete subscription " + subscription.getEndpoint(), e2);
				}
			} else {
				try {
					PushSubscriptionController.incrementFailedCount(subscription);
				} catch (Exception e2) {
					logger.warn("Failed to increment failed fount for " + subscription.getEndpoint(), e2);
				}
			}
		}
	}
}

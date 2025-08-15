// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rasc.webpush.CryptoService;
import ch.rasc.webpush.PushController;
import ch.rasc.webpush.ServerKeys;
import ch.rasc.webpush.dto.Subscription;
import ch.rasc.webpush.dto.SubscriptionKeys;
import eu.albina.controller.PushSubscriptionController;
import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.PushSubscription;
import eu.albina.model.Region;
import eu.albina.model.publication.PushConfiguration;
import eu.albina.util.HibernateUtil;
import eu.albina.util.HttpClientUtil;
import eu.albina.util.JsonUtil;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

public class PushNotificationUtil {

	private static final Logger logger = LoggerFactory.getLogger(PushNotificationUtil.class);
	private final Client client;

	public PushNotificationUtil() {
		this(HttpClientUtil.newClientBuilder().build());
	}

	protected PushNotificationUtil(Client client) {
		this.client = client;
	}

	public static class Message {
		public final String title;
		public final String body;
		public final String image;
		public final String url;

		public Message(String title, String body, String image, String url) {
			this.title = title;
			this.body = body;
			this.image = image;
			this.url = url;
		}
	}

	public void send(MultichannelMessage posting) {
		Message payload = new Message(
			posting.getWebsiteName(),
			posting.getSocialMediaText(),
			posting.getAttachmentUrl(),
			posting.getWebsiteUrl()
		);
        List<PushSubscription> subscriptions = PushSubscriptionController.get(posting.getLanguageCode(), Collections.singleton(posting.getRegion().getId()));
        logger.info("Sending {} push notifications for language={} regions={}: {}", subscriptions.size(), posting.getLanguageCode(), posting.getRegion(), payload);
        for (PushSubscription subscription : subscriptions) {
            sendPushMessage(subscription, payload, null);
        }
    }

	public void sendWelcomePushMessage(PushSubscription subscription) {
		Region region = RegionController.getInstance().getRegion(subscription.getRegion());
		Message payload = new Message(
			region.getWebsiteName(subscription.getLanguage()),
			"Hello World!",
			null,
			null
		);
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

	public void sendPushMessage(PushSubscription subscription, Object payload, ServerKeys serverKeys) {
		try {
			logger.debug("Sending push notification to {}", subscription.getEndpoint());
			if (serverKeys == null) {
				PushConfiguration configuration = getConfiguration().orElseThrow();
				serverKeys = new ServerKeys(configuration.getVapidPublicKey(), configuration.getVapidPrivateKey());
			}
			final SubscriptionKeys subscriptionKeys = new SubscriptionKeys(subscription.getP256dh(), subscription.getAuth());
			final Subscription subscription1 = new Subscription(subscription.getEndpoint(), null, subscriptionKeys);
			final String json = JsonUtil.writeValueUsingJackson(payload);
			final byte[] encrypted = new CryptoService().encrypt(json, subscriptionKeys, 0);
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

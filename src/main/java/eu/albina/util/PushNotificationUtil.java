/*******************************************************************************
 * Copyright (C) 2021 albina-euregio
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
package eu.albina.util;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Collections;
import java.util.List;

import com.github.openjson.JSONObject;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.PushSubscriptionController;
import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;

public final class PushNotificationUtil implements SocialMediaUtil {

	private static final PushNotificationUtil INSTANCE = new PushNotificationUtil();
	private static final Logger logger = LoggerFactory.getLogger(PushNotificationUtil.class);

	public static PushNotificationUtil getInstance() {
		return INSTANCE;
	}

	private PushNotificationUtil() {
		// Add BouncyCastle as an algorithm provider
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	@Override
	public void sendBulletinNewsletter(String message, LanguageCode lang, List<String> regions, String attachmentUrl) {
		final JSONObject payload = new JSONObject();
		payload.put("title", lang.getBundleString("avalanche-report.name"));
		payload.put("body", message);
		payload.put("icon", attachmentUrl);

		List<PushSubscription> subscriptions = PushSubscriptionController.get(lang);
		logger.info("Sending {} push notifications for language={}: {}", subscriptions.size(), lang, payload);
		for (PushSubscription subscription : subscriptions) {
			sendPushMessage(subscription, payload);
		}
	}

	public void sendWelcomePushMessage(PushSubscription subscription) {
		final JSONObject payload = new JSONObject();
		payload.put("title", subscription.getLanguage().getBundleString("avalanche-report.name"));
		payload.put("body", "Hello World!");
		sendPushMessage(subscription, payload);
	}

	public void sendPushMessage(PushSubscription subscription, JSONObject payload) {
		sendPushMessage(subscription, payload.toString().getBytes(StandardCharsets.UTF_8));
	}

	private void sendPushMessage(PushSubscription subscription, byte[] payload) {
		try {
			logger.debug("Sending push notification to {}", subscription.getEndpoint());
			Notification notification = new Notification(
				subscription.getEndpoint(),
				subscription.getP256dh(),
				subscription.getAuth(),
				payload
			);
			PushService pushService = new PushService();
			pushService.setPublicKey(GlobalVariables.getVapidPublicKey());
			pushService.setPrivateKey(GlobalVariables.getVapidPrivateKey());
			pushService.send(notification);
		} catch (Exception e) {
			logger.warn("Failed to send push notification to " + subscription.getEndpoint(), e);
		}
	}

}

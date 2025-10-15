// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import static com.google.common.base.Strings.nullToEmpty;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;

import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.model.publication.WhatsAppConfiguration;

/**
 * A digital communication message meant to be sent via email, push notification, telegram, etc.
 */
public interface MultichannelMessage {

	Region getRegion();

	LanguageCode getLanguageCode();

	default String getWebsiteName() {
		return getRegion().getWebsiteName(getLanguageCode());
	}

	String getWebsiteUrl();

	String getAttachmentUrl();

	String getSubject();

	String getSocialMediaText();

	String getHtmlMessage();

	default String toDefaultString() {
		return MoreObjects.toStringHelper(getClass())
			.add("region", getRegion().getId())
			.add("lang", getLanguageCode())
			.add("websiteName", getWebsiteName())
			.add("websiteUrl", getWebsiteUrl())
			.add("attachmentUrl", getAttachmentUrl())
			.add("subject", getSubject())
			.add("socialMediaText", getSocialMediaText())
			.add("htmlMessage", String.format("(%d bytes)", nullToEmpty(getHtmlMessage()).getBytes(StandardCharsets.UTF_8).length))
			.toString();
	}

	default void sendToAllChannels(PublicationController publicationController) {
		sendMails(publicationController);
		sendTelegramMessage(publicationController);
		sendWhatsAppMessage(publicationController);
		sendPushNotifications(publicationController);
	}

	default void sendMails(PublicationController publicationController) {
		if (!getRegion().isSendEmails()) {
			return;
		}
		tryRunWithLogging("Email newsletter", () -> {
			RapidMailConfiguration mailConfig = publicationController.rapidMailController.getConfiguration(getRegion(), getLanguageCode(), null)
				.orElseThrow(() -> new NoSuchElementException(String.format("No RapidMailConfiguration for %s/%s", getRegion(), getLanguageCode())));
			publicationController.rapidMailController.sendEmail(mailConfig, getHtmlMessage(), getSubject());
			return null;
		});
	}

	default void sendTelegramMessage(PublicationController publicationController) {
		if (!getRegion().isSendTelegramMessages()) {
			return;
		}
		tryRunWithLogging("Telegram message", () -> {
			TelegramConfiguration telegramConfig = publicationController.telegramController.getConfiguration(getRegion(), getLanguageCode()).
				orElseThrow(() -> new NoSuchElementException(String.format("No TelegramConfiguration for %s/%s", getRegion(), getLanguageCode())));
			publicationController.telegramController.trySend(telegramConfig, this, 3);
			return null;
		});
	}

	default void sendWhatsAppMessage(PublicationController publicationController) {
		if (!getRegion().isSendWhatsAppMessages()) {
			return;
		}
		tryRunWithLogging("WhatsApp message", () -> {
			WhatsAppConfiguration whatsAppConfig = publicationController.whatsAppController.getConfiguration(getRegion(), getLanguageCode()).
				orElseThrow(() -> new NoSuchElementException(String.format("No WhatsAppConfiguration for %s/%s", getRegion(), getLanguageCode())));
			publicationController.whatsAppController.trySend(whatsAppConfig, this, 3);
			return null;
		});
	}

	default void sendPushNotifications(PublicationController publicationController) {
		if (!getRegion().isSendPushNotifications()) {
			return;
		}
		tryRunWithLogging("Push notifications", () -> {
			publicationController.pushNotificationUtil.send(this);
			return null;
		});
	}

	default void tryRunWithLogging(String logPrefix, Callable<?> callable) {
		Logger logger = LoggerFactory.getLogger(getClass());
		Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			logger.info("{} for {} triggered", logPrefix, this);
			callable.call();
		} catch (Exception e) {
			logger.warn(String.format("%s for %s could not be sent!", logPrefix, this), e);
		} finally {
			logger.info("{} for {} finished in {}", logPrefix, this, stopwatch);
		}
	}

	static MultichannelMessage of(AvalancheReport avalancheReport, LanguageCode lang) {
		return new AvalancheReportMultichannelMessage(avalancheReport, lang);
	}

	static MultichannelMessage of(BlogConfiguration config, BlogItem blogPost) {
		return new BlogItemMultichannelMessage(config, blogPost);
	}

}

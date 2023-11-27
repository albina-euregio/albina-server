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

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * A digital communication message meant to be sent via email, push notification, telegram, etc.
 */
public interface MultichannelMessage {

	Region getRegion();

	LanguageCode getLanguageCode();

	String getWebsiteUrl();

	String getAttachmentUrl();

	String getSubject();

	String getSocialMediaText();

	String getHtmlMessage();

	default String toDefaultString() {
		return MoreObjects.toStringHelper(getClass())
			.add("region", getRegion().getId())
			.add("lang", getLanguageCode())
			.add("websiteUrl", getWebsiteUrl())
			.add("attachmentUrl", getAttachmentUrl())
			.add("subject", getSubject())
			.add("socialMediaText", getSocialMediaText())
			.add("htmlMessage", String.format("(%d bytes)", nullToEmpty(getHtmlMessage()).getBytes(StandardCharsets.UTF_8).length))
			.toString();
	}

	default void sendToAllChannels() {
		sendMails();
		sendTelegramMessage();
		sendPushNotifications();
	}

	default void sendMails() {
		if (!getRegion().isSendEmails()) {
			return;
		}
		tryRunWithLogging("Email newsletter", () -> {
			RapidMailConfiguration mailConfig = RapidMailController.getConfiguration(getRegion(), getLanguageCode(), null)
				.orElseThrow(() -> new NoSuchElementException(String.format("No RapidMailConfiguration for %s/%s", getRegion(), getLanguageCode())));
			RapidMailController.sendEmail(mailConfig, this);
			return null;
		});
	}

	default void sendTelegramMessage() {
		if (!getRegion().isSendTelegramMessages()) {
			return;
		}
		tryRunWithLogging("Telegram message", () -> {
			TelegramConfiguration telegramConfig = TelegramController.getConfiguration(getRegion(), getLanguageCode()).
				orElseThrow(() -> new NoSuchElementException(String.format("No TelegramConfiguration for %s/%s", getRegion(), getLanguageCode())));
			TelegramController.trySend(telegramConfig, this, 3);
			return null;
		});
	}

	default void sendPushNotifications() {
		if (!getRegion().isSendPushNotifications()) {
			return;
		}
		tryRunWithLogging("Push notifications", () -> {
			new PushNotificationUtil().send(this);
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

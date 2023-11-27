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
import com.google.common.base.Suppliers;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.RapidMailConfiguration;
import eu.albina.model.publication.TelegramConfiguration;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.EmailUtil;
import eu.albina.util.LinkUtil;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

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
			RapidMailConfiguration mailConfig = RapidMailController.getConfiguration(getRegion(), getLanguageCode(), null).orElseThrow();
			RapidMailController.sendEmail(mailConfig, this);
			return null;
		});
	}

	default void sendTelegramMessage() {
		if (!getRegion().isSendTelegramMessages()) {
			return;
		}
		tryRunWithLogging("Telegram message", () -> {
			TelegramConfiguration telegramConfig = TelegramController.getConfiguration(getRegion(), getLanguageCode()).orElseThrow();
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
			logger.atWarn().setCause(e).log("{} for {} could not be sent!", logPrefix, this);
		} finally {
			logger.info("{} for {} finished in {}", logPrefix, this, stopwatch);
		}
	}

	static MultichannelMessage of(AvalancheReport avalancheReport, LanguageCode lang) {
		Supplier<String> htmlMessage = Suppliers.memoize(() -> {
			try {
				return EmailUtil.getInstance().createBulletinEmailHtml(avalancheReport, lang);
			} catch (IOException | TemplateException e) {
				throw new RuntimeException("Failed to create bulletin email", e);
			}
		});
		return new MultichannelMessage() {
			@Override
			public Region getRegion() {
				return avalancheReport.getRegion();
			}

			@Override
			public LanguageCode getLanguageCode() {
				return lang;
			}

			@Override
			public String getWebsiteUrl() {
				return LinkUtil.getBulletinUrl(avalancheReport, lang);
			}

			@Override
			public String getAttachmentUrl() {
				return LinkUtil.getSocialMediaAttachmentUrl(avalancheReport, lang);
			}

			@Override
			public String getSubject() {
				Region region = avalancheReport.getRegion();
				String bundleString = avalancheReport.getStatus() == BulletinStatus.republished
					? lang.getBundleString("email.subject.update", region)
					: lang.getBundleString("email.subject", region);
				return MessageFormat.format(bundleString, lang.getBundleString("website.name", region))
					+ AlbinaUtil.getDate(avalancheReport.getBulletins(), lang);
			}

			@Override
			public String getSocialMediaText() {
				String dateString = AlbinaUtil.getDate(avalancheReport.getBulletins(), lang);
				String bulletinUrl = LinkUtil.getBulletinUrl(avalancheReport, lang);
				return avalancheReport.getStatus() == BulletinStatus.republished
					? MessageFormat.format(lang.getBundleString("social-media.message.update"), lang.getBundleString("website.name"), dateString, bulletinUrl)
					: MessageFormat.format(lang.getBundleString("social-media.message"), lang.getBundleString("website.name"), dateString, bulletinUrl);
			}

			@Override
			public String getHtmlMessage() {
				return htmlMessage.get();
			}

			@Override
			public String toString() {
				return toDefaultString();
			}
		};
	}
}

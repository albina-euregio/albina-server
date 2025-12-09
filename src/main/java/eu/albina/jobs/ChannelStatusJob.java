// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.blog.BlogController;
import eu.albina.controller.publication.blog.BlogItem;
import eu.albina.controller.publication.TelegramController;
import eu.albina.controller.publication.WhatsAppController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.StatusInformation;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A job to check status of the different publication channels (Telegram, WhatsApp, Blog).
 * Also serves the purpose of keeping whapi.cloud channels active enough to not get deactivated during the off-season.
 *
 */
@Singleton
public class ChannelStatusJob {

	private static final Logger logger = LoggerFactory.getLogger(ChannelStatusJob.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	WhatsAppController whatsAppController;

	@Inject
	BlogController blogController;

	@Inject
	TelegramController telegramController;

	Map<String, StatusInformation> whatsAppStatus = new HashMap<>();
	Map<String, StatusInformation> telegramStatus = new HashMap<>();
	Map<String, StatusInformation> blogStatus = new HashMap<>();

	public StatusInformation getOrTriggerWhatsAppStatus(String regionId) throws AlbinaException {
		if(whatsAppStatus.containsKey(regionId)) {
			return whatsAppStatus.get(regionId);
		} else {
			Region region = regionRepository.findByIdOrElseThrow(regionId);
			return triggerWhatsAppCheck(region, region.getDefaultLang());
		}
	}

	public StatusInformation getOrTriggerTelgramStatus(String regionId) throws AlbinaException {
		if(telegramStatus.containsKey(regionId)) {
			return telegramStatus.get(regionId);
		} else {
			Region region = regionRepository.findByIdOrElseThrow(regionId);
			return triggerTelegramCheck(region, region.getDefaultLang());
		}
	}

	public StatusInformation getOrTriggerBlogStatus(String regionId) throws AlbinaException {
		if(blogStatus.containsKey(regionId)) {
			return blogStatus.get(regionId);
		} else {
			Region region = regionRepository.findByIdOrElseThrow(regionId);
			return triggerBlogCheck(region, region.getDefaultLang());
		}
	}

	public StatusInformation triggerWhatsAppCheck(Region region, LanguageCode language) {
		StatusInformation whatsappStatus = whatsAppController.getConfiguration(region, language)
			.map(cfg -> {
				try {
					return whatsAppController.getStatus(cfg);
				} catch (Exception e){
					return new StatusInformation(false, e.getMessage());
				}
			})
			.orElse(new StatusInformation(true, "No config for region " + region.getId() + " and language " + language));

		this.whatsAppStatus.put(region.getId(), whatsappStatus);
		return whatsappStatus;
	}

	public StatusInformation triggerTelegramCheck(Region region, LanguageCode language) {
		StatusInformation telegramStatus = telegramController.getConfiguration(region, language)
			.map(cfg -> {
				try {
					return telegramController.getStatus(cfg);
				} catch (Exception e){
					return new StatusInformation(false, e.getMessage());
				}
			})
			.orElse(new StatusInformation(true, "No config for region " + region.getId() + " and language " + language));

		this.telegramStatus.put(region.getId(), telegramStatus);
		return telegramStatus;
	}

	public StatusInformation triggerBlogCheck(Region region, LanguageCode language) {
		StatusInformation blogStatus;
		if (regionRepository.getPublishBlogRegions().contains(region)) {
			try {
				BlogConfiguration config = blogController.getConfiguration(region, language).orElseThrow();
				BlogItem latest = blogController.getLatestBlogPost(config);
				blogStatus = new StatusInformation(true, "latest=" + latest.getTitle());
			} catch (Exception e) {
				blogStatus = new StatusInformation(false, e.getMessage());
			}
		} else {
			blogStatus = new StatusInformation(true, "region not in publishBlogRegions");
		}
		this.blogStatus.put(region.getId(), blogStatus);
		return blogStatus;
	}

	@Scheduled(cron = "0 0 4 * * ?")
	public void execute() {
		// for all regions with their default language, check WhatsApp, Telegram and Blog
		for (Region region : regionRepository.getPublishBulletinRegions()) {
			LanguageCode language = region.getDefaultLang();

			logger.info("Health check triggered for {}/{}", region.getId(), language);

			StatusInformation whatsappStatus = triggerWhatsAppCheck(region, language);
			StatusInformation telegramStatus = triggerTelegramCheck(region, language);
			StatusInformation blogStatus = triggerBlogCheck(region, language);

			logger.info("Health summary for {}/{}: Telegram={}, WhatsApp={}, Blog={}",
				region.getId(), language, telegramStatus, whatsappStatus, blogStatus);
		}
	}
}

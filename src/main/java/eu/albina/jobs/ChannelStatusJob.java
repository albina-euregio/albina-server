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
import java.util.List;
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

	Map<String, List<StatusInformation>> statusInformationMap = new HashMap<>();

	public List<StatusInformation> getOrTriggerStatusForRegion(String regionId) throws AlbinaException {
		if(statusInformationMap.containsKey(regionId)) {
			return statusInformationMap.get(regionId);
		} else {
			Region region = regionRepository.findByIdOrElseThrow(regionId);
			return triggerStatusChecks(region);
		}
	}

	public List<StatusInformation> triggerStatusChecks(Region region) {
		LanguageCode language = region.getDefaultLang();
		logger.info("Health check triggered for {}/{}", region.getId(), language);
		List<StatusInformation> statusInformation = List.of(
			triggerTelegramCheck(region, language),
			triggerWhatsAppCheck(region, language),
			triggerBlogCheck(region, language)
		);
		statusInformationMap.put(region.getId(), statusInformation);
		return statusInformation;
	}

	public StatusInformation triggerWhatsAppCheck(Region region, LanguageCode language) {
		String title = "WhatsApp for " + region.getId() + "/" + language;
		StatusInformation whatsAppStatus = whatsAppController.getConfiguration(region, language)
			.map(cfg -> {
				try {
					return whatsAppController.getStatus(cfg, title);
				} catch (Exception e){
					return new StatusInformation(false, "WhatsApp" + title, e.getMessage());
				}
			})
			.orElse(new StatusInformation(true, "WhatsApp" + title, "No config"));
		logger.info(whatsAppStatus.toLogLine());
		return whatsAppStatus;
	}

	public StatusInformation triggerTelegramCheck(Region region, LanguageCode language) {
		String title = "Telegram for " + region.getId() + "/" + language;
		StatusInformation telegramStatus = telegramController.getConfiguration(region, language)
			.map(cfg -> {
				try {
					return telegramController.getStatus(cfg, title);
				} catch (Exception e){
					return new StatusInformation(false, "Telegram" + title, e.getMessage());
				}
			})
			.orElse(new StatusInformation(true, "Telegram" + title, "No config"));
		logger.info(telegramStatus.toLogLine());
		return telegramStatus;
	}

	public StatusInformation triggerBlogCheck(Region region, LanguageCode language) {
		String title = "Blog for " + region.getId() + "/" + language;
		StatusInformation blogStatus;
		if (regionRepository.getPublishBlogRegions().contains(region)) {
			try {
				BlogConfiguration config = blogController.getConfiguration(region, language).orElseThrow();
				BlogItem latest = blogController.getLatestBlogPost(config);
				blogStatus = new StatusInformation(true, title,"latest=" + latest.getTitle());
			} catch (Exception e) {
				blogStatus = new StatusInformation(false, title, e.getMessage());
			}
		} else {
			blogStatus = new StatusInformation(true, title,"region not in publishBlogRegions");
		}
		logger.info(blogStatus.toLogLine());
		return blogStatus;
	}

	@Scheduled(cron = "0 0 4 * * ?")
	public void execute() {
		// for all regions with their default language, check WhatsApp, Telegram and Blog
		for (Region region : regionRepository.getPublishBulletinRegions()) {
			triggerStatusChecks(region);
		}
	}
}

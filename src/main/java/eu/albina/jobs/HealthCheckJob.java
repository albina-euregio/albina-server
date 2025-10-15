// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.BlogController;
import eu.albina.controller.publication.BlogItem;
import eu.albina.controller.publication.TelegramController;
import eu.albina.controller.publication.WhatsAppController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A job to check status of the different publication channels (Telegram, WhatsApp, Blog).
 * Also serves the purpose of keeping whapi.cloud channels active enough to not get deactivated during the off-season.
 *
 */
@Singleton
public class HealthCheckJob {

	private static final Logger logger = LoggerFactory.getLogger(HealthCheckJob.class);

	@Inject
	RegionRepository regionRepository;

	@Inject
	WhatsAppController whatsAppController;

	@Inject
	BlogController blogController;

	@Inject
	TelegramController telegramController;

	@Scheduled(cron = "0 0 4 * * ?")
	public void execute() {
		// for all regions with their default language, check WhatsApp, Telegram and Blog
		for (Region region : regionRepository.getPublishBulletinRegions()) {
			LanguageCode language = region.getDefaultLang();
			logger.info("Health check triggered for {}/{}", region.getId(), language);

			String telegramStatus = telegramController.getConfiguration(region, language)
				.map(cfg -> {
					try {
						return telegramController.getMe(cfg).body();
					} catch (Exception e){
						return "FAILED (" + e.getMessage() + ")";
					}
				})
				.orElse("SKIPPED (no config)");

			String whatsappStatus = whatsAppController.getConfiguration(region, language)
				.map(cfg -> {
					try {
						return whatsAppController.getHealth(cfg);
					} catch (Exception e){
						return "FAILED (" + e.getMessage() + ")";
					}
				})
				.orElse("SKIPPED (no config)");

			// Blog (only if region publishes blogs)
			String blogStatus;
			if (regionRepository.getPublishBlogRegions().contains(region)) {
				try {
					BlogConfiguration config = blogController.getConfiguration(region, language).orElseThrow();
					BlogItem latest = blogController.getLatestBlogPost(config);
					blogStatus = "OK (latest=" + latest.getTitle() + ")";
				} catch (Exception e) {
					blogStatus = "FAILED (" + e.getMessage() + ")";
				}
			} else {
				blogStatus = "SKIPPED (region not in publishBlogRegions)";
			}
			logger.info("Health summary for {}/{}: Telegram={}, WhatsApp={}, Blog={}",
				region.getId(), language, telegramStatus, whatsappStatus, blogStatus);
		}
	}
}

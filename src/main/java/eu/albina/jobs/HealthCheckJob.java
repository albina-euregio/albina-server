// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.RegionController;
import eu.albina.controller.publication.BlogController;
import eu.albina.controller.publication.BlogItem;
import eu.albina.controller.publication.TelegramController;
import eu.albina.controller.publication.WhatsAppController;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.publication.BlogConfiguration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * A {@code org.quartz.Job} to check status of the different publication channels (Telegram, WhatsApp, Blog).
 * Also serves the purpose of keeping whapi.cloud channels active enough to not get deactivated during the off-season.
 *
 */
public class HealthCheckJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(HealthCheckJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// for all regions with their default language, check WhatsApp, Telegram and Blog
		for (Region region : RegionController.getInstance().getPublishBulletinRegions()) {
			LanguageCode language = region.getDefaultLang();
			logger.info("Health check triggered for {}/{}", region.getId(), language);

			String telegramStatus = TelegramController.getConfiguration(region, language)
				.map(cfg -> {
					try {
						Response me = TelegramController.getMe(cfg);
						return me.getStatusInfo().toString();
					} catch (Exception e){
						return "FAILED (" + e.getMessage() + ")";
					}
				})
				.orElse("SKIPPED (no config)");

			String whatsappStatus = WhatsAppController.getConfiguration(region, language)
				.map(cfg -> {
					try {
						Response whapiResponse = WhatsAppController.getHealth(cfg);
						return whapiResponse.getStatusInfo().toString();
					} catch (Exception e){
						return "FAILED (" + e.getMessage() + ")";
					}
				})
				.orElse("SKIPPED (no config)");

			// Blog (only if region publishes blogs)
			String blogStatus;
			if (RegionController.getInstance().getPublishBlogRegions().contains(region)) {
				try {
					BlogConfiguration config = BlogController.getConfiguration(region, language);
					BlogItem latest = BlogController.getLatestBlogPost(config);
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

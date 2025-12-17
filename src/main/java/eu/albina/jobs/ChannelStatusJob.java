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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

	Map<String, List<StatusInformation>> statusInformationMap = new ConcurrentHashMap<>();

	public CompletableFuture<List<StatusInformation>> getOrTriggerStatusForRegion(String regionId) throws AlbinaException {
		List<StatusInformation> cached = statusInformationMap.get(regionId);
		if (cached != null) {
			return CompletableFuture.completedFuture(cached);
		}
		Region region = regionRepository.findByIdOrElseThrow(regionId);
		return triggerStatusChecks(region);
	}

	public CompletableFuture<List<StatusInformation>> triggerStatusChecks(Region region) {
		LanguageCode language = region.getDefaultLang();
		logger.info("Health check triggered for {}/{}", region.getId(), language);

		CompletableFuture<StatusInformation> telegram =	triggerTelegramCheck(region, language);
		CompletableFuture<StatusInformation> whatsapp =	triggerWhatsAppCheck(region, language);
		CompletableFuture<StatusInformation> blog =	triggerBlogCheck(region, language);

		return CompletableFuture
			.allOf(telegram, whatsapp, blog)
			.thenApply(v -> {
				List<StatusInformation> result = List.of(
					telegram.join(),
					whatsapp.join(),
					blog.join()
				);
				statusInformationMap.put(region.getId(), result);
				return result;
			});
	}

	public CompletableFuture<StatusInformation> triggerWhatsAppCheck(Region region, LanguageCode language) {
		String title = "WhatsApp for " + region.getId() + "/" + language;

		return whatsAppController.getConfiguration(region, language)
			.map(cfg ->
				whatsAppController.getStatusAsync(cfg, title)
					.thenApply(status -> {
						logger.info(status.toLogLine());
						return status;
					})
			)
			.orElseGet(() -> {
				StatusInformation status = new StatusInformation(true, title, "No config");
				logger.info(status.toLogLine());
				return CompletableFuture.completedFuture(status);
			});
	}

	public CompletableFuture<StatusInformation> triggerTelegramCheck(Region region, LanguageCode language) {
		String title = "Telegram for " + region.getId() + "/" + language;
		return telegramController.getConfiguration(region, language)
			.map(cfg ->
				telegramController.getStatusAsync(cfg, title)
					.thenApply(status -> {
						logger.info(status.toLogLine());
						return status;
					})
			)
			.orElseGet(() -> {
				StatusInformation status = new StatusInformation(true, title, "No config");
				logger.info(status.toLogLine());
				return CompletableFuture.completedFuture(status);
			});
	}

	public CompletableFuture<StatusInformation> triggerBlogCheck(Region region, LanguageCode language) {
		return CompletableFuture.supplyAsync(() -> {
			String title = "Blog for " + region.getId() + "/" + language;
			StatusInformation blogStatus;
			if (regionRepository.getPublishBlogRegions().contains(region)) {
				try {
					BlogConfiguration config = blogController.getConfiguration(region, language).orElseThrow();
					BlogItem latest = blogController.getLatestBlogPost(config);
					blogStatus = new StatusInformation(true, title, "latest=" + latest.getTitle());
				} catch (Exception e) {
					blogStatus = new StatusInformation(false, title, e.getMessage());
				}
			} else {
				blogStatus = new StatusInformation(true, title, "region not in publishBlogRegions");
			}
			logger.info(blogStatus.toLogLine());
			return blogStatus;
		});
	}

	@Scheduled(cron = "0 0 4 * * ?")
	public void execute() {
		// for all regions with their default language, check WhatsApp, Telegram and Blog
		for (Region region : regionRepository.getPublishBulletinRegions()) {
			triggerStatusChecks(region)
				.exceptionally(ex -> {
					logger.error("Health check failed for {}/{}", region.getId(), region.getDefaultLang(), ex);
					return List.of();
				});
		}
	}
}

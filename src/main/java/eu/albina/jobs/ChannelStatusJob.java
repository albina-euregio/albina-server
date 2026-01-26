// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.jobs;

import eu.albina.controller.RegionRepository;
import eu.albina.controller.publication.blog.BlogController;
import eu.albina.controller.publication.rapidmail.RapidMailController;
import eu.albina.controller.publication.TelegramController;
import eu.albina.controller.publication.WhatsAppController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import eu.albina.model.StatusInformation;
import eu.albina.model.publication.RapidMailConfiguration;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	@Inject
	RapidMailController rapidMailController;

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
		logger.info("Health check triggered for {}", region.getId());

		List<CompletableFuture<StatusInformation>> status = Stream.of(
				telegramController.getConfigurations(region).stream().map(c -> telegramController.getStatusAsync(c, "Telegram (%s/%s)".formatted(c.getRegion(), c.getLanguageCode()))),
				whatsAppController.getConfigurations(region).stream().map(c -> whatsAppController.getStatusAsync(c, "WhatsApp (%s/%s)".formatted(c.getRegion(), c.getLanguageCode()))),
				rapidMailController.getConfigurations(region).stream()
					.collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(RapidMailConfiguration::getUsername)))).stream()
					.map(c -> rapidMailController.getStatusAsync(c, "Mail (%s/%s)".formatted(c.getRegion(), c.getLanguageCode()))),
				blogController.getConfigurations(region).stream().map(c -> blogController.getStatusAsync(c, "Blog (%s/%s)".formatted(c.getRegion(), c.getLanguageCode())))
			)
			.flatMap(s -> s)
			.toList();

		return CompletableFuture
			.allOf(status.toArray(CompletableFuture[]::new))
			.thenApply(v -> {
				List<StatusInformation> result = status.stream().map(CompletableFuture::join).toList();
				result.forEach(s -> logger.info(s.toLogLine()));
				statusInformationMap.put(region.getId(), result);
				return result;
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

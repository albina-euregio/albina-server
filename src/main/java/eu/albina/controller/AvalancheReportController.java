// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AbstractPersistentObject;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.AvalancheReportStatus;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.util.JsonUtil;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Controller for avalanche reports.
 *
 * @author Norbert Lanzanasto
 *
 */
@Singleton
public class AvalancheReportController {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheReportController.class);

	@Inject
	AvalancheReportRepository avalancheReportRepository;

	@Inject
	UserRepository userRepository;

	@Inject
	ObjectMapper objectMapper;

	@Repository
	public interface AvalancheReportRepository extends CrudRepository<AvalancheReport, String> {

		List<AvalancheReport> findByDateAndRegion(ZonedDateTime date, Region region);

		List<AvalancheReport> findByDateBetweenAndRegion(ZonedDateTime startDate, ZonedDateTime endDate, Region region);

		List<AvalancheReport> findByDateBetweenAndRegionAndStatusIn(ZonedDateTime startDate, ZonedDateTime endDate, Region region, Set<BulletinStatus> status);

		AvalancheReport findFirstByStatusInOrderByDateDesc(Set<BulletinStatus> status);

		@Query(value = """
			select r.date, r.timestamp, r.status
			from avalanche_reports r
			where r.date between :startDate and :endDate and r.region_id = :regionID and r.status is not null
		""", nativeQuery = true)
		List<AvalancheReportStatus> listByDateBetweenAndRegion(ZonedDateTime startDate, ZonedDateTime endDate, String regionID);

		default List<AvalancheReportStatus> listByDateBetweenAndRegion(Instant startDate, Instant endDate, Region region) {
			return listByDateBetweenAndRegion(startDate.atZone(ZoneOffset.UTC), endDate.atZone(ZoneOffset.UTC), region.getId());
		}
	}

	/**
	 * Return all public reports for a specific time period and {@code region}.
	 *
	 * @param startDate start date if the time period
	 * @param endDate   end date of the time period
	 * @param region    the region of interest
	 * @return all public reports for a specific time period and {@code region}
	 */
	public Collection<AvalancheReport> getPublicReports(Instant startDate, Instant endDate, Region region) {
		List<AvalancheReport> reports = avalancheReportRepository.findByDateBetweenAndRegionAndStatusIn(
			startDate.atZone(ZoneOffset.UTC),
			endDate.atZone(ZoneOffset.UTC),
			region,
			BulletinStatus.PUBLISHED_OR_REPUBLISHED
		);
		Map<Instant, AvalancheReport> result = getHighestStatusMap(reports);
		return result.values();
	}

	/**
	 * Return the public report for specific {@code date} and {@code region} or null
	 * if no report was found.
	 *
	 * @param date   the date of interest
	 * @param region the region of interest
	 * @return the public report for specific {@code date} and {@code region} or
	 * null if not report was found
	 */
	public AvalancheReport getPublicReport(Instant date, Region region) {
		ZonedDateTime startDate = date.atZone(ZoneOffset.UTC);
		List<AvalancheReport> reports = avalancheReportRepository.findByDateBetweenAndRegionAndStatusIn(startDate, startDate, region, BulletinStatus.PUBLISHED_OR_REPUBLISHED);
		return getHighestStatus(reports);
	}

	static final Comparator<AvalancheReport> BY_TIMESTAMP = Comparator.comparing(AvalancheReport::getTimestamp);
	static final Comparator<AvalancheReport> BY_STATUS = Comparator.comparing(AvalancheReport::getStatus, Comparator.nullsFirst(BulletinStatus::comparePublicationStatus));

	static AvalancheReport getHighestStatus(List<AvalancheReport> reports) {
		return reports.stream().max(BY_STATUS.thenComparing(BY_TIMESTAMP)).orElse(null);
	}

	static Map<Instant, AvalancheReport> getHighestStatusMap(List<AvalancheReport> reports) {
		return reports.stream().collect(Collectors.toMap(
			r -> r.getDate().toInstant(),
			r -> r,
			(r1, r2) -> getHighestStatus(List.of(r1, r2))
		));
	}

	/**
	 * Returns the most recent report for specific {@code date} and {@code region}
	 * or null if no report was found.
	 *
	 * @param date   the date of interest
	 * @param region the region of interest
	 * @return the most recent report for specific {@code date} and {@code region}
	 * or null if no report was found
	 */
	public AvalancheReport getInternalReport(Instant date, Region region) {
		List<AvalancheReport> reports = avalancheReportRepository.findByDateAndRegion(date.atZone(ZoneOffset.UTC), region);
		// select most recent report
		return reports.stream().max(BY_TIMESTAMP).orElse(null);
	}

	/**
	 * Save a report for the given {@code startDate} and {@code region} by the
	 * specified {@code user}. If no report was present the status will be
	 * {@code draft}. If a report was already saved the status will be updated from
	 * {@code missing} to {@code updated}, {@code submitted} to {@code draft},
	 * {@code published} to {@code updated}, {@code resubmitted} to {@code updated},
	 * {@code republished} to {@code updated}. A broacast message about the changes
	 * is sent.
	 *
	 * @param bulletins the affected bulletins
	 * @param startDate               the start date of the report
	 * @param region             the region of the report
	 */
	public void saveReport(Map<String, AvalancheBulletin> bulletins, Instant startDate, Region region) {
		try {
			publishReport(new ArrayList<>(bulletins.values()), startDate, region, ZonedDateTime.now().withNano(0).toInstant(), BulletinStatus::saveReport);
		} finally {
			logger.info("Report for region {} saved", region.getId());
		}
	}

	/**
	 * Change status of a report with a given validity time and region to
	 * <code>published</code> (if the previous status was <code>submitted</code>) or
	 * <code>republished</code> (if the previous status was
	 * <code>resubmitted</code>) and set the json string of the bulletins. If there
	 * was not report a new report with status <code>missing</code> is created.
	 *
	 * @param bulletins       the bulletins which are affected by the publication
	 * @param startDate       the start date of the time period
	 * @param region          the region that should be published
	 * @param publicationDate the timestamp when the report was published
	 * @throws AlbinaException if more than one report was found
	 */
	public void publishReport(List<AvalancheBulletin> bulletins, Instant startDate, Region region,
							  Instant publicationDate) {
		try {
			publishReport(bulletins, startDate, region, publicationDate, BulletinStatus::publishReport);
		} finally {
			logger.info("Report for region {} published", region.getId());
		}
	}

	private void publishReport(List<AvalancheBulletin> bulletins, Instant startDate, Region region,
							   Instant publicationDate, UnaryOperator<BulletinStatus> bulletinStatusOperator) {
		AvalancheReport report = getInternalReport(startDate, region);
		boolean mediaFileUploaded = report != null && report.isMediaFileUploaded();

		BulletinStatus status0 = report != null ? report.getStatus() : BulletinStatus.missing;
		BulletinStatus status1 = status0 == null ? BulletinStatus.missing : bulletinStatusOperator.apply(status0);
		logger.info("Status changed from {} to {} for region {}", status0, status1, region.getId());

		// reuse existing report if status does not change
		if (report == null || !Objects.equals(status0, status1)) {
			report = new AvalancheReport();
		}

		report.setTimestamp(publicationDate.atZone(ZoneOffset.UTC));
		report.setDate(startDate.atZone(ZoneOffset.UTC));
		report.setRegion(region);
		report.setMediaFileUploaded(mediaFileUploaded);
		report.setStatus(status1);

		try {
			// set JSON string after status is published/republished
			bulletins = bulletins.stream().map(b -> b.withRegionFilter(region)).toList();
			report.setJsonString(objectMapper.cloneWithViewClass(JsonUtil.Views.Internal.class).writeValueAsString(bulletins));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		avalancheReportRepository.save(report);
	}

	/**
	 * Change status of report with a given validity time and region to
	 * <code>submitted</code> (if the previous status was <code>draft</code>) or
	 * <code>resubmitted</code> (if the previous status was <code>updated</code>)
	 * and set the user. If there was not report a new report with status
	 * <code>missing</code> is created.
	 *
	 * @param bulletins the affected bulletins
	 * @param startDate the start date of the time period
	 * @param region    the region that should be submitted
	 * @param user      the user who submits the report
	 */
	public void submitReport(List<AvalancheBulletin> bulletins, Instant startDate, Region region, User user) {
		try {
			publishReport(bulletins, startDate, region, ZonedDateTime.now().withNano(0).toInstant(), BulletinStatus::submitReport);
		} finally {
			logger.info("Report for region {} submitted by {}", region.getId(), user);
		}
	}

	/**
	 * Return all bulletins in a given time period and for specific regions with
	 * status {@code published} or {@code republished} (ordered by danger rating).
	 *
	 * @param date    the start date of the bulletins
	 * @param regions the regions of interest
	 * @return all published bulletins with the most recent version number
	 * @throws AlbinaException if the report could not be loaded from the DB
	 */
	public List<AvalancheBulletin> getPublishedBulletins(Instant date, Collection<Region> regions) {
		return mergeOrSplitBulletins(regions.stream().map(region -> getPublicReport(date, region)));
	}

	public List<AvalancheBulletin> mergeOrSplitBulletins(Stream<AvalancheReport> reports) {
		List<AvalancheBulletin> bulletins = reports
			.filter(Objects::nonNull)
			.flatMap(report -> report.getPublishedBulletins(objectMapper).stream())
			.toList();

		bulletins = new ArrayList<>(mergeOrSplitBulletins(bulletins));
		Collections.sort(bulletins);
		return bulletins;
	}

	private static Collection<AvalancheBulletin> mergeOrSplitBulletins(Iterable<AvalancheBulletin> bulletins) {
		int revision = 1;
		Map<String, AvalancheBulletin> resultMap = new HashMap<>();
		for (AvalancheBulletin bulletin : bulletins) {
			if (resultMap.containsKey(bulletin.getId())) {
				boolean match = false;

				// merge bulletins with same base id
				for (String bulletinId : resultMap.keySet()) {
					if (bulletinId.split("_")[0].startsWith(bulletin.getId())) {
						if (resultMap.get(bulletinId).equals(bulletin)) {
							for (String publishedRegion : bulletin.getPublishedRegions())
								resultMap.get(bulletinId).addPublishedRegion(publishedRegion);
							for (String savedRegion : bulletin.getSavedRegions())
								resultMap.get(bulletin.getId()).addSavedRegion(savedRegion);
							for (String suggestedRegion : bulletin.getSuggestedRegions())
								resultMap.get(bulletin.getId()).addSuggestedRegion(suggestedRegion);
							match = true;
							break;
						} else {
							continue;
						}
					}
				}

				if (!match) {
					bulletin.setId(bulletin.getId() + "_" + revision);
					revision++;
					resultMap.put(bulletin.getId(), bulletin);
				}
			} else
				resultMap.put(bulletin.getId(), bulletin);
		}
		return resultMap.values();
	}

	/**
	 * Return id of reports for a specific {@code date} and {@code regions} with
	 * status {@code published} or {@code republished}.
	 *
	 * @param date    the date of interest
	 * @param regions the regions of interest
	 * @return id of reports for a specific time period and regions with status
	 * {@code published} or {@code republished}
	 */
	public List<String> getPublishedReportIds(Instant date, List<Region> regions) {
		return regions.stream()
			.map(region -> getPublicReport(date, region))
			.filter(report -> BulletinStatus.isPublishedOrRepublished(report.getStatus()))
			.map(AbstractPersistentObject::getId)
			.toList();
	}

	@Transactional
	public void setAvalancheReportFlag(String avalancheReportId, BiConsumer<AvalancheReport, Boolean> flagSetter) {
		if (avalancheReportId == null) {
			return;
		}
		try {
			AvalancheReport avalancheReport = avalancheReportRepository.findById(avalancheReportId).orElseThrow();
			flagSetter.accept(avalancheReport, true);
			avalancheReportRepository.save(avalancheReport);
		} catch (Exception e) {
			logger.warn("Could not set avalanche report flag for report %s!".formatted(avalancheReportId), e);
		}
	}

	@Transactional
	public void setMediaFileFlag(Instant date, Region region) {
		AvalancheReport result = null;
		List<AvalancheReport> reports = avalancheReportRepository.findByDateAndRegion(date.atZone(ZoneOffset.UTC), region);

		// select most recent report
		for (AvalancheReport avalancheReport : reports)
			if (result == null)
				result = avalancheReport;
			else if (result.getTimestamp().isBefore(avalancheReport.getTimestamp()))
				result = avalancheReport;

		if (result != null) {
			result.setMediaFileUploaded(true);
			avalancheReportRepository.save(result);
		}
	}

	/**
	 * Returns the date of the latest published bulletin.
	 *
	 * @return the date of the latest published bulletin
	 */
	public Instant getLatestDate() throws AlbinaException {
		return avalancheReportRepository.findFirstByStatusInOrderByDateDesc(BulletinStatus.PUBLISHED_OR_REPUBLISHED).getDate().toInstant();
	}
}

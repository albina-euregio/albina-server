// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import eu.albina.model.AvalancheReportStatus;
import eu.albina.util.JsonUtil;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AbstractPersistentObject;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.util.AlbinaUtil;

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

		@Join(value = "user", type = Join.Type.FETCH)
		List<AvalancheReport> findByDateAndRegion(ZonedDateTime date, Region region);

		default List<AvalancheReport> findByDateAndRegion(Instant date, Region region) {
			return findByDateAndRegion(AlbinaUtil.getZonedDateTimeUtc(date), region);
		}

		@Join(value = "user", type = Join.Type.FETCH)
		List<AvalancheReport> findByDateBetweenAndRegion(ZonedDateTime startDate, ZonedDateTime endDate, Region region);

		default List<AvalancheReport> findByDateBetweenAndRegion(Instant startDate, Instant endDate, Region region) {
			return findByDateBetweenAndRegion(AlbinaUtil.getZonedDateTimeUtc(startDate), AlbinaUtil.getZonedDateTimeUtc(endDate), region);
		}

		AvalancheReport findFirstByStatusInOrderByDateDesc(Set<BulletinStatus> status);

		List<AvalancheReportStatus> listByDateBetweenAndRegion(ZonedDateTime startDate, ZonedDateTime endDate, Region region);

		default List<AvalancheReportStatus> listByDateBetweenAndRegion(Instant startDate, Instant endDate, Region region) {
			return listByDateBetweenAndRegion(AlbinaUtil.getZonedDateTimeUtc(startDate), AlbinaUtil.getZonedDateTimeUtc(endDate), region);
		}
	}

	/**
	 * Return the actual status of the bulletins for a specific {@code date} for a
	 * given {@code region} or null if no report was found.
	 *
	 * @param date   the date of interest
	 * @param region the region of interest
	 * @return the actual status of the bulletins of this day or null if no report
	 * was found
	 */
	public BulletinStatus getInternalStatusForDay(Instant date, Region region) {
		AvalancheReport report = getInternalReport(date, region);
		if (report != null)
			return report.getStatus();
		else
			return null;
	}

	/**
	 * Return the official status of the bulletins for every day in a given time
	 * period for a given {@code region}. For each day the highest status is
	 * returned ({@code republished} > {@code published} > {@code submitted} >
	 * {@code draft} > {@code missing}).
	 *
	 * @param startDate the start date of the time period
	 * @param endDate   the end date of the time period
	 * @param region    the region of interest
	 * @return a map of all days within the time period and the official status of
	 * the bulletins of this day
	 * @throws AlbinaException if no region was defined
	 */
	public Map<Instant, BulletinStatus> getStatus(Instant startDate, Instant endDate, Region region)
		throws AlbinaException {
		Map<Instant, BulletinStatus> result = new HashMap<>();

		if (region == null)
			throw new AlbinaException("No region defined!");

		Collection<AvalancheReport> reports = getPublicReports(startDate, endDate, region);
		for (AvalancheReport avalancheReport : reports)
			result.put(avalancheReport.getDate().toInstant(), avalancheReport.getStatus());

		return result;
	}

	/**
	 * Return the official status of the bulletins for every day in a given time
	 * period for multiple {@code regions}. For each day the highest status is
	 * returned ({@code republished} > {@code published} > {@code submitted} >
	 * {@code draft} > {@code missing}).
	 *
	 * @param startDate the start date of the time period
	 * @param endDate   the end date of the time period
	 * @param regions   the regions of interest
	 * @return a map of all days within the time period and the official status of
	 * the bulletins of this day
	 * @throws AlbinaException if no region was defined
	 */
	public Map<Instant, BulletinStatus> getStatus(Instant startDate, Instant endDate, List<Region> regions)
		throws AlbinaException {
		Map<Instant, BulletinStatus> result = new HashMap<>();

		if (regions == null || regions.isEmpty())
			throw new AlbinaException("No region defined!");

		for (Region region : regions) {
			Collection<AvalancheReport> reports = getPublicReports(startDate, endDate, region);
			for (AvalancheReport avalancheReport : reports) {
				if (result.containsKey(avalancheReport.getDate().toInstant())) {
					if (result.get(avalancheReport.getDate().toInstant()).comparePublicationStatus(avalancheReport.getStatus()) < 0)
						result.put(avalancheReport.getDate().toInstant(), avalancheReport.getStatus());
				} else
					result.put(avalancheReport.getDate().toInstant(), avalancheReport.getStatus());
			}
		}

		return result;
	}

	/**
	 * Return the publication status of the bulletins for every day in a given time
	 * period for a given {@code region}. For each day the status is only returned
	 * if it is {@code republished} or {@code published}.
	 *
	 * @param startDate the start date of the time period
	 * @param endDate   the end date of the time period
	 * @param region    the region of interest
	 * @return a map of all days within the time period and the status of the
	 * bulletins of this day if it is {@code republished} or
	 * {@code published}
	 */
	public Map<Instant, AvalancheReport> getPublicationStatus(Instant startDate, Instant endDate, Region region) {

		Collection<AvalancheReport> reports = getPublicReports(startDate, endDate, region);

		return reports.stream()
			.filter(avalancheReport -> avalancheReport.getStatus() == BulletinStatus.published
				|| avalancheReport.getStatus() == BulletinStatus.republished)
			.collect(Collectors.toMap(avalancheReport -> startDate, avalancheReport -> avalancheReport, (a, b) -> b));
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
		List<AvalancheReport> reports = avalancheReportRepository.findByDateBetweenAndRegion(startDate, endDate, region);
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
		List<AvalancheReport> reports = avalancheReportRepository.findByDateAndRegion(date, region);
		return getHighestStatus(reports);
	}

	private AvalancheReport getHighestStatus(List<AvalancheReport> reports) {
		AvalancheReport result = null;
		for (AvalancheReport avalancheReport : reports) {
			if (result == null)
				result = avalancheReport;
			else {
				if (avalancheReport.getStatus() == null)
					continue;
				if (result.getStatus() == null)
					result = avalancheReport;
				else if (result.getStatus().comparePublicationStatus(avalancheReport.getStatus()) <= 0
					&& result.getTimestamp().isBefore(avalancheReport.getTimestamp()))
					result = avalancheReport;
			}
		}
		return result;
	}

	private Map<Instant, AvalancheReport> getHighestStatusMap(List<AvalancheReport> reports) {
		Map<Instant, AvalancheReport> result = new HashMap<>();
		for (AvalancheReport avalancheReport : reports)
			if (result.containsKey(avalancheReport.getDate().toInstant())) {
				if (avalancheReport.getStatus() == null)
					continue;
				if (result.get(avalancheReport.getDate().toInstant()).getStatus() == null)
					result.put(avalancheReport.getDate().toInstant(), avalancheReport);
				else if (result.get(avalancheReport.getDate().toInstant()).getStatus()
					.comparePublicationStatus(avalancheReport.getStatus()) <= 0
					&& result.get(avalancheReport.getDate().toInstant()).getTimestamp()
					.isBefore(avalancheReport.getTimestamp()))
					result.put(avalancheReport.getDate().toInstant(), avalancheReport);
			} else
				result.put(avalancheReport.getDate().toInstant(), avalancheReport);
		return result;
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
		List<AvalancheReport> reports = avalancheReportRepository.findByDateAndRegion(date, region);
		// select most recent report
		return reports.stream().max(Comparator.comparing(AvalancheReport::getTimestamp)).orElse(null);
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
	 * @param avalancheBulletins the affected bulletins
	 * @param date               the start date of the report
	 * @param region             the region of the report
	 * @param user               the user who saves the report
	 */
	public void saveReport(Map<String, AvalancheBulletin> avalancheBulletins, Instant date, Region region, User user) {
		AvalancheReport latestReport = getInternalReport(date, region);
		BulletinStatus latestStatus = latestReport == null ? null : latestReport.getStatus();
		BulletinStatus newStatus = deriveStatus(avalancheBulletins, latestStatus);

		// reuse existing report if status does not change
		AvalancheReport avalancheReport = latestReport != null && Objects.equals(latestStatus, newStatus)
			? latestReport
			: new AvalancheReport();
		avalancheReport.setStatus(newStatus);
		avalancheReport.setTimestamp(AlbinaUtil.getZonedDateTimeNowNoNanos());
		avalancheReport.setUser(user);
		avalancheReport.setDate(date.atZone(ZoneId.of("UTC")));
		avalancheReport.setRegion(region);
		Collection<AvalancheBulletin> bulletins = avalancheBulletins.values().stream().map(b -> b.withRegionFilter(region)).toList();
		try {
			avalancheReport.setJsonString(objectMapper.cloneWithViewClass(JsonUtil.Views.Internal.class).writeValueAsString(bulletins));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		avalancheReportRepository.save(avalancheReport);

		logger.info("Report for region {} saved by {}", region.getId(), user);
	}

	private static BulletinStatus deriveStatus(Map<String, AvalancheBulletin> avalancheBulletins, BulletinStatus latestStatus) {
		if (latestStatus == null) {
			if (avalancheBulletins.isEmpty()) {
				return null;
			} else {
				return BulletinStatus.draft;
			}
		}

		switch (latestStatus) {
			case missing:
			case republished:
			case resubmitted:
			case updated:
			case published:
				return BulletinStatus.updated;
			case draft:
				if (avalancheBulletins.isEmpty()) {
					return null;
				} else {
					return BulletinStatus.draft;
				}
			case submitted:
				return BulletinStatus.draft;
			default:
				return latestStatus;
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
	 * @param username        the user who publishes the report
	 * @param publicationDate the timestamp when the report was published
	 * @return a list of the ids of the published reports
	 * @throws AlbinaException if more than one report was found
	 */
	public AvalancheReport publishReport(List<AvalancheBulletin> bulletins, Instant startDate, Region region, String username,
										 Instant publicationDate) {
		User user = username != null ? userRepository.findById(username).orElseThrow() : null;
		AvalancheReport report = getInternalReport(startDate, region);

		AvalancheReport avalancheReport = new AvalancheReport();
		avalancheReport.setTimestamp(publicationDate.atZone(ZoneId.of("UTC")));
		avalancheReport.setUser(user);
		avalancheReport.setDate(startDate.atZone(ZoneId.of("UTC")));
		avalancheReport.setRegion(region);
		if (report == null) {
			avalancheReport.setStatus(BulletinStatus.missing);
		} else {
			avalancheReport.setMediaFileUploaded(report.isMediaFileUploaded());
			switch (report.getStatus()) {
				case missing:
					avalancheReport.setStatus(BulletinStatus.missing);
					logger.warn("Bulletins have to be created first!");
					break;
				case draft:
					avalancheReport.setStatus(BulletinStatus.updated);
					logger.warn("Bulletins have to be submitted first!");
					break;
				case submitted:
					avalancheReport.setStatus(BulletinStatus.published);
					logger.info("Status set to PUBLISHED for region {}", region.getId());
					break;
				case published:
					avalancheReport.setStatus(BulletinStatus.published);
					logger.warn("Bulletins already published!");
					break;
				case updated:
					avalancheReport.setStatus(BulletinStatus.updated);
					logger.warn("Bulletins have to be resubmitted first!");
					break;
				case resubmitted:
					avalancheReport.setStatus(BulletinStatus.republished);
					logger.info("Status set to REPUBLISHED for region {}", region.getId());
					break;
				case republished:
					avalancheReport.setStatus(BulletinStatus.republished);
					logger.warn("Bulletins already republished!");
					break;
				default:
					break;
			}
		}

		Collection<AvalancheBulletin> bulletins1 = bulletins.stream().map(b -> b.withRegionFilter(region)).toList();
		try {
			avalancheReport.setJsonString(objectMapper.cloneWithViewClass(JsonUtil.Views.Internal.class).writeValueAsString(bulletins1));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		avalancheReportRepository.save(avalancheReport);
		logger.info("Report for region {} published by {}", region.getId(), user);
		return avalancheReport;
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
		AvalancheReport report = getInternalReport(startDate, region);
		AvalancheReport avalancheReport = new AvalancheReport();
		avalancheReport.setTimestamp(AlbinaUtil.getZonedDateTimeNowNoNanos());
		avalancheReport.setUser(user);
		avalancheReport.setDate(startDate.atZone(ZoneId.of("UTC")));
		avalancheReport.setRegion(region);
		if (report == null) {
			avalancheReport.setStatus(BulletinStatus.missing);
			logger.info("Status set to MISSING for region {}", region.getId());
		} else {
			avalancheReport.setMediaFileUploaded(report.isMediaFileUploaded());
			switch (report.getStatus()) {
				case missing:
					avalancheReport.setStatus(BulletinStatus.missing);
					logger.warn("Bulletins have to be created first!");
					break;
				case draft:
					avalancheReport.setStatus(BulletinStatus.submitted);
					logger.info("Status set to SUBMITTED for region {}", region.getId());
					break;
				case submitted:
					avalancheReport.setStatus(BulletinStatus.submitted);
					logger.warn("Bulletins already submitted!");
					break;
				case published:
					avalancheReport.setStatus(BulletinStatus.published);
					logger.warn("Bulletins already published!");
					break;
				case updated:
					avalancheReport.setStatus(BulletinStatus.resubmitted);
					logger.info("Status set to RESUBMITTED for region {}", region.getId());
					break;
				case resubmitted:
					avalancheReport.setStatus(BulletinStatus.resubmitted);
					logger.info("Bulletins already resubmitted!");
					break;
				case republished:
					avalancheReport.setStatus(BulletinStatus.republished);
					logger.warn("Bulletins already republished!");
					break;
				default:
					break;
			}
		}

		// set json string after status is published/republished
		Collection<AvalancheBulletin> bulletins1 = bulletins.stream().map(b -> b.withRegionFilter(region)).toList();
		try {
			avalancheReport.setJsonString(objectMapper.cloneWithViewClass(JsonUtil.Views.Internal.class).writeValueAsString(bulletins1));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		avalancheReportRepository.save(avalancheReport);
		logger.info("Report for region {} submitted by {}", region.getId(), user);
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
	public ArrayList<AvalancheBulletin> getPublishedBulletins(Instant date, List<Region> regions) {
		int revision = 1;
		Map<String, AvalancheBulletin> resultMap = new HashMap<>();

		for (Region region : regions) {
			// get bulletins for this region
			AvalancheReport report = getPublicReport(date, region);
			if (report == null) {
				continue;
			}
			List<AvalancheBulletin> publishedBulletinsForRegion = report.getPublishedBulletins(objectMapper);
			for (AvalancheBulletin bulletin : publishedBulletinsForRegion) {
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
		}

		ArrayList<AvalancheBulletin> bulletins = new ArrayList<>(resultMap.values());
		Collections.sort(bulletins);

		return bulletins;
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
			.filter(report -> report.getStatus() == BulletinStatus.published || report.getStatus() == BulletinStatus.republished)
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
		List<AvalancheReport> reports = avalancheReportRepository.findByDateAndRegion(date, region);

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
		return avalancheReportRepository.findFirstByStatusInOrderByDateDesc(EnumSet.of(BulletinStatus.published, BulletinStatus.republished)).getDate().toInstant();
	}
}

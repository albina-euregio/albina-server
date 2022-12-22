/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
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
package eu.albina.controller;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

import eu.albina.model.AbstractPersistentObject;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.BulletinUpdate;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.rest.websocket.AvalancheBulletinUpdateEndpoint;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.JsonUtil;

/**
 * Controller for avalanche reports.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheReportController {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheReportController.class);

	private static AvalancheReportController instance = null;

	/**
	 * Private constructor.
	 */
	private AvalancheReportController() {
	}

	/**
	 * Returns the {@code AvalancheReportController} object associated with the
	 * current Java application.
	 *
	 * @return the {@code AvalancheReportController} object associated with the
	 *         current Java application.
	 */
	public static AvalancheReportController getInstance() {
		if (instance == null) {
			instance = new AvalancheReportController();
		}
		return instance;
	}

	/**
	 * Return the actual status of the bulletins for every day in a given time
	 * period for a given {@code region}.
	 *
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param region
	 *            the region of interest
	 * @return a map of all days within the time period and the actual status of the
	 *         bulletins of this day
	 * @throws AlbinaException
	 *             if no {@code region} was defined
	 */
	public Map<Instant, BulletinStatus> getInternalStatus(Instant startDate, Instant endDate, Region region)
			throws AlbinaException {

		if (region == null)
			throw new AlbinaException("No region defined!");

		Collection<AvalancheReport> reports = getInternalReports(startDate, endDate, region);

		return reports.stream().filter(report -> report.getStatus() != null)
			.collect(Collectors.toMap(report -> report.getDate().toInstant(), AvalancheReport::getStatus, (a, b) -> b));
	}

	/**
	 * Return the actual status of the bulletins for a specific {@code date} for a
	 * given {@code region} or null if no report was found.
	 *
	 * @param date
	 *            the date of interest
	 * @param region
	 *            the region of interest
	 * @return the actual status of the bulletins of this day or null if no report
	 *         was found
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
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param region
	 *            the region of interest
	 * @return a map of all days within the time period and the official status of
	 *         the bulletins of this day
	 * @throws AlbinaException
	 *             if no region was defined
	 */
	public Map<Instant, BulletinStatus> getStatus(Instant startDate, Instant endDate, Region region)
			throws AlbinaException {
		Map<Instant, BulletinStatus> result = new HashMap<Instant, BulletinStatus>();

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
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param regions
	 *            the regions of interest
	 * @return a map of all days within the time period and the official status of
	 *         the bulletins of this day
	 * @throws AlbinaException
	 *             if no region was defined
	 */
	public Map<Instant, BulletinStatus> getStatus(Instant startDate, Instant endDate, List<Region> regions)
			throws AlbinaException {
		Map<Instant, BulletinStatus> result = new HashMap<Instant, BulletinStatus>();

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
	 * @param startDate
	 *            the start date of the time period
	 * @param endDate
	 *            the end date of the time period
	 * @param region
	 *            the region of interest
	 * @return a map of all days within the time period and the status of the
	 *         bulletins of this day if it is {@code republished} or
	 *         {@code published}
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
	 * @param startDate
	 *            start date if the time period
	 * @param endDate
	 *            end date of the time period
	 * @param region
	 *            the region of interest
	 * @return all public reports for a specific time period and {@code region}
	 */
	public Collection<AvalancheReport> getPublicReports(Instant startDate, Instant endDate, Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheReport> reports = new ArrayList<AvalancheReport>();
			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				reports = entityManager.createQuery(HibernateUtil.queryGetReportsForTimePeriodAndRegion, AvalancheReport.class)
						.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate))
						.setParameter("region", region).getResultList();

				for (AvalancheReport avalancheReport : reports) {
					initializeAndUnproxy(avalancheReport);
					initializeAndUnproxy(avalancheReport.getUser());
				}
			}

			Map<Instant, AvalancheReport> result = getHighestStatusMap(reports);

			return result.values();
		});
	}

	/**
	 * Return the public report for specific {@code date} and {@code region} or null
	 * if no report was found.
	 *
	 * @param date
	 *            the date of interest
	 * @param region
	 *            the region of interest
	 * @return the public report for specific {@code date} and {@code region} or
	 *         null if not report was found
	 */
	public AvalancheReport getPublicReport(Instant date, Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheReport> reports = new ArrayList<AvalancheReport>();
			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				reports = entityManager.createQuery(HibernateUtil.queryGetReportsForDayAndRegion, AvalancheReport.class)
						.setParameter("date", AlbinaUtil.getZonedDateTimeUtc(date))
						.setParameter("region", region).getResultList();

				for (AvalancheReport avalancheReport : reports) {
					initializeAndUnproxy(avalancheReport);
					initializeAndUnproxy(avalancheReport.getUser());
				}
			}

			return getHighestStatus(reports);
		});
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
		Map<Instant, AvalancheReport> result = new HashMap<Instant, AvalancheReport>();
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
	 * Return all most recent reports for a specific time period and {@code region}.
	 *
	 * @param startDate
	 *            start date if the time period
	 * @param endDate
	 *            end date of the time period
	 * @param region
	 *            the region of interest
	 * @return all most recent reports for a specific time period and {@code region}
	 */
	private Collection<AvalancheReport> getInternalReports(Instant startDate, Instant endDate, Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			Map<Instant, AvalancheReport> result = new HashMap<Instant, AvalancheReport>();
			List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				reports = entityManager.createQuery(HibernateUtil.queryGetReportsForTimePeriodAndRegion, AvalancheReport.class)
						.setParameter("startDate", AlbinaUtil.getZonedDateTimeUtc(startDate)).setParameter("endDate", AlbinaUtil.getZonedDateTimeUtc(endDate))
						.setParameter("region", region).getResultList();
			}

			// select most recent report
			for (AvalancheReport avalancheReport : reports) {
				avalancheReport.setRegion(region);
				if (result.containsKey(avalancheReport.getDate().toInstant())) {
					if (result.get(avalancheReport.getDate().toInstant()).getTimestamp().isBefore(avalancheReport.getTimestamp()))
						result.put(avalancheReport.getDate().toInstant(), avalancheReport);
				} else
					result.put(avalancheReport.getDate().toInstant(), avalancheReport);
			}

			return result.values();
		});
	}

	/**
	 * Returns the most recent report for specific {@code date} and {@code region}
	 * or null if no report was found.
	 *
	 * @param date
	 *            the date of interest
	 * @param region
	 *            the region of interest
	 * @return the most recent report for specific {@code date} and {@code region}
	 *         or null if no report was found
	 */
	public AvalancheReport getInternalReport(Instant date, Region region) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheReport result = null;
			List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				reports = entityManager.createQuery(HibernateUtil.queryGetReportsForDayAndRegion, AvalancheReport.class)
						.setParameter("date", AlbinaUtil.getZonedDateTimeUtc(date))
						.setParameter("region", region).getResultList();
			}

			// select most recent report
			for (AvalancheReport avalancheReport : reports) {
				avalancheReport.setRegion(region);
				if (result == null)
					result = avalancheReport;
				else if (result.getTimestamp().isBefore(avalancheReport.getTimestamp()))
					result = avalancheReport;
			}

			return result;
		});
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
	 * @param avalancheBulletins
	 *            the affected bulletins
	 * @param date
	 *            the start date of the report
	 * @param region
	 *            the region of the report
	 * @param user
	 *            the user who saves the report
	 * @throws AlbinaException
	 *             if more than one report was found for the given day
	 */
	public void saveReport(Map<String, AvalancheBulletin> avalancheBulletins, Instant date, Region region, User user)
			throws AlbinaException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			BulletinUpdate bulletinUpdate = null;

			BulletinStatus latestStatus = getInternalStatusForDay(date, region);

			AvalancheReport avalancheReport = new AvalancheReport();
			avalancheReport.setTimestamp(AlbinaUtil.getZonedDateTimeNowNoNanos());
			avalancheReport.setUser(user);
			avalancheReport.setDate(date.atZone(ZoneId.of("UTC")));
			avalancheReport.setRegion(region);
			if (latestStatus != null)
				switch (latestStatus) {
				case missing:
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				case draft:
					avalancheReport.setStatus(BulletinStatus.draft);
					break;
				case submitted:
					avalancheReport.setStatus(BulletinStatus.draft);
					break;
				case published:
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				case updated:
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				case resubmitted:
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				case republished:
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				default:
					break;
				}
			else
				avalancheReport.setStatus(BulletinStatus.draft);

			// set json string after status is published/republished
			avalancheReport
					.setJsonString(JsonUtil.createJSONString(avalancheBulletins.values(), region, false).toString());

			entityManager.persist(avalancheReport);
			bulletinUpdate = new BulletinUpdate(region.getId(), date, avalancheReport.getStatus());

			if (bulletinUpdate != null) {
				AvalancheBulletinUpdateEndpoint.broadcast(bulletinUpdate);
			}

			logger.info("Report for region {} saved", region.getId());

			return null;
		});
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
	 * @param user            the user who publishes the report
	 * @param publicationDate the timestamp when the report was published
	 * @return a list of the ids of the published reports
	 * @throws AlbinaException if more than one report was found
	 */
	public AvalancheReport publishReport(List<AvalancheBulletin> bulletins, Instant startDate, Region region, User user,
										 Instant publicationDate) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheReport report = getInternalReport(startDate, region);

			BulletinUpdate bulletinUpdate = null;

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
					logger.warn("Bulletins have to be created first!");
					// avalancheReport.setStatus(BulletinStatus.published);
					break;
				case draft:
					logger.warn("Bulletins have to be submitted first!");
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				case submitted:
					avalancheReport.setStatus(BulletinStatus.published);
					logger.info("Status set to PUBLISHED for region {}", region.getId());
					break;
				case published:
					logger.warn("Bulletins already published!");
					break;
				case updated:
					logger.warn("Bulletins have to be resubmitted first!");
					break;
				case resubmitted:
					avalancheReport.setStatus(BulletinStatus.republished);
					logger.info("Status set to REPUBLISHED for region {}", region.getId());
					break;
				case republished:
					logger.warn("Bulletins already republished!");
					break;
				default:
					break;
				}
			}

			avalancheReport.setJsonString(JsonUtil.createJSONString(bulletins, region, false).toString());

			entityManager.persist(avalancheReport);
			bulletinUpdate = new BulletinUpdate(region.getId(), startDate, avalancheReport.getStatus());
			AvalancheBulletinUpdateEndpoint.broadcast(bulletinUpdate);

			logger.info("Report for region {} published", region.getId());

			return avalancheReport;
		});
	}

	/**
	 * Change status of report with a given validity time and region to
	 * <code>submitted</code> (if the previous status was <code>draft</code>) or
	 * <code>resubmitted</code> (if the previous status was <code>updated</code>)
	 * and set the user. If there was not report a new report with status
	 * <code>missing</code> is created.
	 *
	 * @param bulletins
	 *            the affected bulletins
	 * @param startDate
	 *            the start date of the time period
	 * @param region
	 *            the region that should be submitted
	 * @param user
	 *            the user who submits the report
	 */
	public void submitReport(List<AvalancheBulletin> bulletins, Instant startDate, Region region, User user) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheReport report = getInternalReport(startDate, region);
			BulletinUpdate bulletinUpdate = null;

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
					logger.warn("Bulletins have to be created first!");
					break;
				case draft:
					avalancheReport.setStatus(BulletinStatus.submitted);
					logger.info("Status set to SUBMITTED for region {}", region.getId());
					break;
				case submitted:
					logger.warn("Bulletins already submitted!");
					break;
				case published:
					logger.warn("Bulletins already published!");
					break;
				case updated:
					avalancheReport.setStatus(BulletinStatus.resubmitted);
					logger.info("Status set to RESUBMITTED for region {}", region.getId());
					break;
				case resubmitted:
					logger.info("Bulletins already resubmitted!");
					break;
				case republished:
					logger.warn("Bulletins already republished!");
					break;
				default:
					break;
				}
			}

			// set json string after status is published/republished
			avalancheReport.setJsonString(JsonUtil.createJSONString(bulletins, region, false).toString());

			entityManager.persist(avalancheReport);
			bulletinUpdate = new BulletinUpdate(region.getId(), startDate, avalancheReport.getStatus());

			if (bulletinUpdate != null) {
				AvalancheBulletinUpdateEndpoint.broadcast(bulletinUpdate);
			}

			logger.info("Report for region {} submitted", region.getId());

			return null;
		});
	}

	/**
	 * Return all bulletins in a given time period and for specific regions with
	 * status {@code published} or {@code republished} (ordered by danger rating).
	 *
	 * @param date
	 *            the start date of the bulletins
	 * @param regions
	 *            the regions of interest
	 * @return all published bulletins with the most recent version number
	 * @throws AlbinaException
	 *             if the report could not be loaded from the DB
	 */
	public ArrayList<AvalancheBulletin> getPublishedBulletins(Instant date, List<Region> regions)
			throws AlbinaException {
		int revision = 1;
		Map<String, AvalancheBulletin> resultMap = new HashMap<String, AvalancheBulletin>();

		for (Region region : regions) {
			// get bulletins for this region
			List<AvalancheBulletin> publishedBulletinsForRegion = getPublishedBulletinsForRegion(date, region);
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

		ArrayList<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>(resultMap.values());
		Collections.sort(bulletins);

		return bulletins;
	}

	/**
	 * Return id of reports for a specific {@code date} and {@code regions} with
	 * status {@code published} or {@code republished}.
	 *
	 * @param date
	 *            the date of interest
	 * @param regions
	 *            the regions of interest
	 * @return id of reports for a specific time period and regions with status
	 *         {@code published} or {@code republished}
	 */
	public List<String> getPublishedReportIds(Instant date, List<Region> regions) {
		return regions.stream()
			.map(region -> getPublicReport(date, region))
			.filter(report -> report.getStatus() == BulletinStatus.published || report.getStatus() == BulletinStatus.republished)
			.map(AbstractPersistentObject::getId)
			.collect(Collectors.toList());
	}

	/**
	 * Return all published bulletins for a specific time period and region.
	 *
	 * @param date
	 *            start of the time period
	 * @param endDate
	 *            end of the time period
	 * @param region
	 *            the region of interest
	 * @return all published bulletins for a specific time period and region
	 */
	private List<AvalancheBulletin> getPublishedBulletinsForRegion(Instant date, Region region) {
		// get report for date and region
		AvalancheReport report = getPublicReport(date, region);

		List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();
		if (report != null
				&& (report.getStatus() == BulletinStatus.published || report.getStatus() == BulletinStatus.republished)
				&& report.getJsonString() != null) {
			JSONArray jsonArray = new JSONArray(report.getJsonString());
			for (Object object : jsonArray)
				if (object instanceof JSONObject) {
					AvalancheBulletin bulletin = new AvalancheBulletin((JSONObject) object, UserController.getInstance()::getUser);
					// only add bulletins with published regions
					if (bulletin.getPublishedRegions() != null && !bulletin.getPublishedRegions().isEmpty())
						results.add(bulletin);
				}
		}

		return results;
	}

	/**
	 * Initialize and unproxy fields of entity.
	 *
	 * @param entity
	 *            the entity to initialize and unproxy
	 * @return the entity
	 */
	@SuppressWarnings("unchecked")
	private static <T> T initializeAndUnproxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}

		Hibernate.initialize(entity);
		if (entity instanceof HibernateProxy) {
			entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}

	public void setAvalancheReportFlag(String avalancheReportId, BiConsumer<AvalancheReport, Boolean> flagSetter) {
		if (avalancheReportId == null) {
			return;
		}
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
			flagSetter.accept(avalancheReport, true);
			entityManager.flush();
			return null;
		});
	}

	public void setMediaFileFlag(Instant date, Region region) {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheReport result = null;
			List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

			if (region != null && !Strings.isNullOrEmpty(region.getId())) {
				reports = entityManager.createQuery(HibernateUtil.queryGetReportsForDayAndRegion, AvalancheReport.class)
						.setParameter("date", AlbinaUtil.getZonedDateTimeUtc(date))
						.setParameter("region", region).getResultList();
			}

			// select most recent report
			for (AvalancheReport avalancheReport : reports)
				if (result == null)
					result = avalancheReport;
				else if (result.getTimestamp().isBefore(avalancheReport.getTimestamp()))
					result = avalancheReport;

			result.setMediaFileUploaded(true);
			entityManager.persist(result);
			entityManager.flush();
			return null;
		});
	}

	/**
	 * Returns the date of the latest published bulletin.
	 *
	 * @return the date of the latest published bulletin
	 * @throws HibernateException
	 *             if no report was found
	 */
	public Instant getLatestDate() throws AlbinaException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			AvalancheReport report = (AvalancheReport) entityManager.createQuery(HibernateUtil.queryGetLatestDate)
					.setMaxResults(1).getSingleResult();

			if (report != null)
				return report.getDate().toInstant();
			else
				throw new HibernateException("No report found!");
		});
	}
}

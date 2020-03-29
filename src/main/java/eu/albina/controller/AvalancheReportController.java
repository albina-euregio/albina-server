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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;
import org.joda.time.DateTime;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.BulletinUpdate;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.rest.AvalancheBulletinUpdateEndpoint;
import eu.albina.util.HibernateUtil;
import eu.albina.util.JsonUtil;

/**
 * Controller for avalanche reports.
 *
 * @author Norbert Lanzanasto
 *
 */
public class AvalancheReportController {

	private static Logger logger = LoggerFactory.getLogger(AvalancheReportController.class);

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
	public Map<DateTime, BulletinStatus> getInternalStatus(DateTime startDate, DateTime endDate, String region)
			throws AlbinaException {
		Map<DateTime, BulletinStatus> result = new HashMap<DateTime, BulletinStatus>();

		if (region == null || region.isEmpty())
			throw new AlbinaException("No region defined!");

		Collection<AvalancheReport> reports = getInternalReports(startDate, endDate, region);

		for (AvalancheReport report : reports) {
			if (report.getStatus() != null)
				result.put(report.getDate(), report.getStatus());
		}

		return result;
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
	public BulletinStatus getInternalStatusForDay(DateTime date, String region) {
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
	public Map<DateTime, BulletinStatus> getStatus(DateTime startDate, DateTime endDate, String region)
			throws AlbinaException {
		Map<DateTime, BulletinStatus> result = new HashMap<DateTime, BulletinStatus>();

		if (region == null || region == "")
			throw new AlbinaException("No region defined!");

		Collection<AvalancheReport> reports = getPublicReports(startDate, endDate, region);
		for (AvalancheReport avalancheReport : reports)
			result.put(avalancheReport.getDate(), avalancheReport.getStatus());

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
	public Map<DateTime, BulletinStatus> getStatus(DateTime startDate, DateTime endDate, List<String> regions)
			throws AlbinaException {
		Map<DateTime, BulletinStatus> result = new HashMap<DateTime, BulletinStatus>();

		if (regions == null || regions.isEmpty())
			throw new AlbinaException("No region defined!");

		for (String region : regions) {
			Collection<AvalancheReport> reports = getPublicReports(startDate, endDate, region);
			for (AvalancheReport avalancheReport : reports) {
				if (result.containsKey(avalancheReport.getDate())) {
					if (result.get(avalancheReport.getDate()).comparePublicationStatus(avalancheReport.getStatus()) < 0)
						result.put(avalancheReport.getDate(), avalancheReport.getStatus());
				} else
					result.put(avalancheReport.getDate(), avalancheReport.getStatus());
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
	public Map<DateTime, AvalancheReport> getPublicationStatus(DateTime startDate, DateTime endDate, String region) {
		Map<DateTime, AvalancheReport> result = new HashMap<DateTime, AvalancheReport>();
		DateTime date = startDate;

		Collection<AvalancheReport> reports = getPublicReports(startDate, endDate, region);

		for (AvalancheReport avalancheReport : reports)
			if (avalancheReport.getStatus() == BulletinStatus.published
					|| avalancheReport.getStatus() == BulletinStatus.republished)
				result.put(date, avalancheReport);

		return result;
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
	@SuppressWarnings("unchecked")
	public Collection<AvalancheReport> getPublicReports(DateTime startDate, DateTime endDate, String region) {
		List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		if (region != null && region != "") {
			reports = entityManager.createQuery(HibernateUtil.queryGetReportsForTimePeriodAndRegion)
					.setParameter("startDate", startDate).setParameter("endDate", endDate)
					.setParameter("region", region).getResultList();

			for (AvalancheReport avalancheReport : reports) {
				initializeAndUnproxy(avalancheReport);
				initializeAndUnproxy(avalancheReport.getUser());
			}

			transaction.commit();
			entityManager.close();
		}

		Map<DateTime, AvalancheReport> result = getHighestStatusMap(reports);

		return result.values();
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
	@SuppressWarnings("unchecked")
	private AvalancheReport getPublicReport(DateTime date, String region) {
		List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		if (region != null && region != "") {
			reports = entityManager.createQuery(HibernateUtil.queryGetReportsForDayAndRegion).setParameter("date", date)
					.setParameter("region", region).getResultList();

			for (AvalancheReport avalancheReport : reports) {
				initializeAndUnproxy(avalancheReport);
				initializeAndUnproxy(avalancheReport.getUser());
			}

			transaction.commit();
			entityManager.close();
		}

		AvalancheReport result = getHighestStatus(reports);
		return result;
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

	private Map<DateTime, AvalancheReport> getHighestStatusMap(List<AvalancheReport> reports) {
		Map<DateTime, AvalancheReport> result = new HashMap<DateTime, AvalancheReport>();
		for (AvalancheReport avalancheReport : reports)
			if (result.containsKey(avalancheReport.getDate())) {
				if (avalancheReport.getStatus() == null)
					continue;
				if (result.get(avalancheReport.getDate()).getStatus() == null)
					result.put(avalancheReport.getDate(), avalancheReport);
				else if (result.get(avalancheReport.getDate()).getStatus()
						.comparePublicationStatus(avalancheReport.getStatus()) <= 0
						&& result.get(avalancheReport.getDate()).getTimestamp()
								.isBefore(avalancheReport.getTimestamp()))
					result.put(avalancheReport.getDate(), avalancheReport);
			} else
				result.put(avalancheReport.getDate(), avalancheReport);
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
	@SuppressWarnings("unchecked")
	private Collection<AvalancheReport> getInternalReports(DateTime startDate, DateTime endDate, String region) {
		Map<DateTime, AvalancheReport> result = new HashMap<DateTime, AvalancheReport>();
		List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		if (region != null && region != "") {
			reports = entityManager.createQuery(HibernateUtil.queryGetReportsForTimePeriodAndRegion)
					.setParameter("startDate", startDate).setParameter("endDate", endDate)
					.setParameter("region", region).getResultList();

			transaction.commit();
			entityManager.close();
		}

		// select most recent report
		for (AvalancheReport avalancheReport : reports)
			if (result.containsKey(avalancheReport.getDate())) {
				if (result.get(avalancheReport.getDate()).getTimestamp().isBefore(avalancheReport.getTimestamp()))
					result.put(avalancheReport.getDate(), avalancheReport);
			} else
				result.put(avalancheReport.getDate(), avalancheReport);

		return result.values();
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
	@SuppressWarnings("unchecked")
	private AvalancheReport getInternalReport(DateTime date, String region) {
		AvalancheReport result = null;
		List<AvalancheReport> reports = new ArrayList<AvalancheReport>();

		if (region != null && region != "") {
			EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
			EntityTransaction transaction = entityManager.getTransaction();
			transaction.begin();

			reports = entityManager.createQuery(HibernateUtil.queryGetReportsForDayAndRegion).setParameter("date", date)
					.setParameter("region", region).getResultList();

			transaction.commit();
			entityManager.close();
		}

		// select most recent report
		for (AvalancheReport avalancheReport : reports)
			if (result == null)
				result = avalancheReport;
			else if (result.getTimestamp().isBefore(avalancheReport.getTimestamp()))
				result = avalancheReport;

		return result;
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
	public void saveReport(Map<String, AvalancheBulletin> avalancheBulletins, DateTime date, String region, User user)
			throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			BulletinUpdate bulletinUpdate = null;

			BulletinStatus latestStatus = getInternalStatusForDay(date, region);

			AvalancheReport avalancheReport = new AvalancheReport();
			avalancheReport.setTimestamp(new DateTime());
			avalancheReport.setUser(user);
			avalancheReport.setDate(date);
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
			bulletinUpdate = new BulletinUpdate(region, date, avalancheReport.getStatus());

			transaction.commit();

			if (bulletinUpdate != null) {
				AvalancheBulletinUpdateEndpoint.broadcast(bulletinUpdate);
			}

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Change a report. This is only a small change and does not trigger the whole
	 * publication process. The timestamp, user and revoision as well as the JSON
	 * string of all bulletins is updated in the report.
	 *
	 * @param publishedBulletins
	 *            the bulletins affected by this change
	 * @param startDate
	 *            the start date of the report
	 * @param region
	 *            the region of the report
	 * @param user
	 *            the user who changes the report
	 * @return the id of the report
	 * @throws AlbinaException
	 *             if the report can not be loaded from the DB
	 */
	public String changeReport(List<AvalancheBulletin> publishedBulletins, DateTime startDate, String region, User user)
			throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			AvalancheReport latestReport = getInternalReport(startDate, region);
			if (latestReport != null) {
				transaction.begin();
				AvalancheReport avalancheReport = new AvalancheReport();
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setDate(startDate);
				avalancheReport.setRegion(region);
				avalancheReport.setStatus(latestReport.getStatus());

				avalancheReport.setJsonString(JsonUtil.createJSONString(publishedBulletins, region, false).toString());
				entityManager.persist(avalancheReport);
				transaction.commit();
				return avalancheReport.getId();
			} else {
				throw new AlbinaException("Report error!");
			}
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} catch (AlbinaException ae) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(ae.getMessage());
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Change status of reports with a given validity time and region to
	 * <code>published</code> (if the previous status was <code>submitted</code>) or
	 * <code>republished</code> (if the previous status was
	 * <code>resubmitted</code>) and set the json string of the bulletins. If there
	 * was not report a new report with status <code>missing</code> is created.
	 *
	 * @param bulletins
	 *            the bulletins which are affected by the publication
	 * @param startDate
	 *            the start date of the time period
	 * @param regions
	 *            the region that should be published
	 * @param user
	 *            the user who publishes the report
	 * @param publicationDate
	 *            the timestamp when the report was published
	 * @return a list of the ids of the published reports
	 * @throws AlbinaException
	 *             if more than one report was found
	 */
	public List<String> publishReport(Collection<AvalancheBulletin> bulletins, DateTime startDate, List<String> regions,
			User user, DateTime publicationDate) throws AlbinaException {
		List<String> avalancheReportIds = new ArrayList<String>();
		for (String region : regions) {
			String avalancheReportId = publishReport(bulletins, startDate, region, user, publicationDate);
			avalancheReportIds.add(avalancheReportId);
		}
		return avalancheReportIds;
	}

	/**
	 * Change status of a report with a given validity time and region to
	 * <code>published</code> (if the previous status was <code>submitted</code>) or
	 * <code>republished</code> (if the previous status was
	 * <code>resubmitted</code>) and set the json string of the bulletins. If there
	 * was not report a new report with status <code>missing</code> is created.
	 *
	 * @param bulletins
	 *            the bulletins which are affected by the publication
	 * @param startDate
	 *            the start date of the time period
	 * @param region
	 *            the region that should be published
	 * @param user
	 *            the user who publishes the report
	 * @param publicationDate
	 *            the timestamp when the report was published
	 * @return a list of the ids of the published reports
	 * @throws AlbinaException
	 *             if more than one report was found
	 */
	public String publishReport(Collection<AvalancheBulletin> bulletins, DateTime startDate, String region, User user,
			DateTime publicationDate) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			AvalancheReport report = getInternalReport(startDate, region);

			BulletinUpdate bulletinUpdate = null;

			AvalancheReport avalancheReport = new AvalancheReport();
			avalancheReport.setTimestamp(publicationDate);
			avalancheReport.setUser(user);
			avalancheReport.setDate(startDate);
			avalancheReport.setRegion(region);
			if (report == null) {
				avalancheReport.setStatus(BulletinStatus.missing);
			} else {
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
					break;
				case published:
					logger.warn("Bulletins already published!");
					break;
				case updated:
					logger.warn("Bulletins have to be resubmitted first!");
					break;
				case resubmitted:
					avalancheReport.setStatus(BulletinStatus.republished);
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
			bulletinUpdate = new BulletinUpdate(region, startDate, avalancheReport.getStatus());

			transaction.commit();

			if (bulletinUpdate != null) {
				AvalancheBulletinUpdateEndpoint.broadcast(bulletinUpdate);
			}

			return avalancheReport.getId();

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
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
	 * @throws AlbinaException
	 *             if more than one report was found
	 */
	public void submitReport(List<AvalancheBulletin> bulletins, DateTime startDate, String region, User user)
			throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			AvalancheReport report = getInternalReport(startDate, region);
			BulletinUpdate bulletinUpdate = null;

			AvalancheReport avalancheReport = new AvalancheReport();
			avalancheReport.setTimestamp(new DateTime());
			avalancheReport.setUser(user);
			avalancheReport.setDate(startDate);
			avalancheReport.setRegion(region);
			if (report == null) {
				avalancheReport.setStatus(BulletinStatus.missing);
			} else {
				switch (report.getStatus()) {
				case missing:
					logger.warn("Bulletins have to be created first!");
					break;
				case draft:
					avalancheReport.setStatus(BulletinStatus.submitted);
					break;
				case submitted:
					logger.warn("Bulletins already submitted!");
					break;
				case published:
					logger.warn("Bulletins already published!");
					break;
				case updated:
					avalancheReport.setStatus(BulletinStatus.resubmitted);
					break;
				case resubmitted:
					logger.debug("Bulletins already resubmitted!");
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
			bulletinUpdate = new BulletinUpdate(region, startDate, avalancheReport.getStatus());

			transaction.commit();

			if (bulletinUpdate != null) {
				AvalancheBulletinUpdateEndpoint.broadcast(bulletinUpdate);
			}

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
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
	public ArrayList<AvalancheBulletin> getPublishedBulletins(DateTime date, List<String> regions)
			throws AlbinaException {
		int revision = 1;
		Map<String, AvalancheBulletin> resultMap = new HashMap<String, AvalancheBulletin>();
		Map<String, AvalancheBulletin> tmpMap = new HashMap<String, AvalancheBulletin>();

		for (String region : regions) {
			// get bulletins for this region
			List<AvalancheBulletin> publishedBulletinsForRegion = getPublishedBulletinsForRegion(date, region);
			for (AvalancheBulletin bulletin : publishedBulletinsForRegion) {
				if (resultMap.containsKey(bulletin.getId())) {
					// merge bulletins with same id
					if (resultMap.get(bulletin.getId()).equals(bulletin)) {
						for (String publishedRegion : bulletin.getPublishedRegions())
							resultMap.get(bulletin.getId()).addPublishedRegion(publishedRegion);
						for (String savedRegion : bulletin.getSavedRegions())
							resultMap.get(bulletin.getId()).addSavedRegion(savedRegion);
						for (String suggestedRegion : bulletin.getSuggestedRegions())
							resultMap.get(bulletin.getId()).addSuggestedRegion(suggestedRegion);
					} else {
						tmpMap = new HashMap<String, AvalancheBulletin>();
						for (String bulletinId : resultMap.keySet()) {
							if (bulletinId.startsWith(bulletin.getId())) {
								if (resultMap.get(bulletinId).equals(bulletin)) {
									for (String publishedRegion : bulletin.getPublishedRegions())
										resultMap.get(bulletinId).addPublishedRegion(publishedRegion);
									for (String savedRegion : bulletin.getSavedRegions())
										resultMap.get(bulletin.getId()).addSavedRegion(savedRegion);
									for (String suggestedRegion : bulletin.getSuggestedRegions())
										resultMap.get(bulletin.getId()).addSuggestedRegion(suggestedRegion);
								} else {
									bulletin.setId(bulletin.getId() + "_" + revision);
									revision++;
									tmpMap.put(bulletin.getId(), bulletin);
								}
							}
						}
						for (Entry<String, AvalancheBulletin> entry : tmpMap.entrySet()) {
							resultMap.put(entry.getKey(), entry.getValue());
						}
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
	public List<String> getPublishedReportIds(DateTime date, List<String> regions) {
		List<String> ids = new ArrayList<String>();

		for (String region : regions) {
			AvalancheReport report = getPublicReport(date, region);
			if (report.getStatus() == BulletinStatus.published || report.getStatus() == BulletinStatus.republished)
				ids.add(report.getId());
		}

		return ids;
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
	private List<AvalancheBulletin> getPublishedBulletinsForRegion(DateTime date, String region) {
		// get report for date and region
		AvalancheReport report = getPublicReport(date, region);

		List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();
		if (report != null
				&& (report.getStatus() == BulletinStatus.published || report.getStatus() == BulletinStatus.republished)
				&& report.getJsonString() != null) {
			JSONArray jsonArray = new JSONArray(report.getJsonString());
			for (Object object : jsonArray)
				if (object instanceof JSONObject) {
					AvalancheBulletin bulletin = new AvalancheBulletin((JSONObject) object);
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

	/**
	 * Set the email flag for all reports with {@code avalancheReportIds},
	 * indicating that the emails for this reports have been sent.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportEmailFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setEmailCreated(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Email flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the pdf flag for all reports with {@code avalancheReportIds}, indicating
	 * that the pdfs for this reports have been created.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportPdfFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setPdfCreated(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("PDF flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the html flag for all reports with {@code avalancheReportIds}, indicating
	 * that the simple html version for this reports have been created.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportHtmlFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setHtmlCreated(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("HTML flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the static widget flag for all reports with {@code avalancheReportIds},
	 * indicating that the static widgets (images for press) for this reports have
	 * been created.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportStaticWidgetFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setStaticWidgetCreated(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Static widget flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the map flag for all reports with {@code avalancheReportIds}, indicating
	 * that the maps for this reports have been created.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportMapFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setMapCreated(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Map flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the caaml flag for all reports with {@code avalancheReportIds},
	 * indicating that the caaml (XML) files for this reports have been created.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportCaamlFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setCaamlCreated(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Map flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the whatsapp flag for all reports with {@code avalancheReportIds},
	 * indicating that the messages via whatsapp for this reports have been sent.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportWhatsappFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setWhatsappSent(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Whatsapp flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Set the telegram flag for all reports with {@code avalancheReportIds},
	 * indicating that the messages via telegram for this reports have been sent.
	 *
	 * @param avalancheReportIds
	 *            the ids of the reports for whom the flag should be set
	 */
	public void setAvalancheReportTelegramFlag(List<String> avalancheReportIds) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (String avalancheReportId : avalancheReportIds) {
				AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
				avalancheReport.setTelegramSent(true);
			}
			entityManager.flush();
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			logger.error("Telegram flag could not be set!");
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Returns the date of the latest published bulletin.
	 *
	 * @return the date of the latest published bulletin
	 * @throws AlbinaException
	 *             if no report was found
	 */
	public DateTime getLatestDate() throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();

		transaction.begin();
		AvalancheReport report = (AvalancheReport) entityManager.createQuery(HibernateUtil.queryGetLatestDate)
				.setMaxResults(1).getSingleResult();
		transaction.commit();
		entityManager.close();

		if (report != null)
			return report.getDate();
		else
			throw new AlbinaException("No report found!");
	}
}

package eu.albina.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.proxy.HibernateProxy;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinVersionTuple;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Texts;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
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

	private AvalancheReportController() {
	}

	public static AvalancheReportController getInstance() {
		if (instance == null) {
			instance = new AvalancheReportController();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public Map<DateTime, BulletinStatus> getStatus(DateTime startDate, DateTime endDate, String region)
			throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			Map<DateTime, BulletinStatus> result = new HashMap<DateTime, BulletinStatus>();
			DateTime date = startDate;

			while (date.isBefore(endDate) || date.isEqual(endDate)) {
				// get report
				List<AvalancheReport> reports = new ArrayList<AvalancheReport>();
				if (region == null || region == "")
					reports = entityManager.createQuery(HibernateUtil.queryGetReports).setParameter("date", date)
							.getResultList();
				else
					reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
							.setParameter("date", date).setParameter("region", region).getResultList();

				BulletinStatus status = BulletinStatus.missing;
				if (reports.size() == 1)
					status = reports.get(0).getStatus();
				else if (reports.size() > 1)
					throw new AlbinaException("Report error!");

				result.put(date, status);
				date = date.plusDays(1);
			}

			transaction.commit();

			return result;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public Map<DateTime, AvalancheReport> getPublicationStatus(DateTime startDate, DateTime endDate, String region)
			throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			Map<DateTime, AvalancheReport> result = new HashMap<DateTime, AvalancheReport>();
			DateTime date = startDate;

			while (date.isBefore(endDate) || date.isEqual(endDate)) {
				// get report
				List<AvalancheReport> reports = new ArrayList<AvalancheReport>();
				if (region != null && region != "")
					reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
							.setParameter("date", date).setParameter("region", region).getResultList();
				for (AvalancheReport avalancheReport : reports) {
					if (avalancheReport.getStatus() == BulletinStatus.published
							|| avalancheReport.getStatus() == BulletinStatus.republished) {
						initializeAndUnproxy(avalancheReport);
						initializeAndUnproxy(avalancheReport.getUser());
						result.put(date, avalancheReport);
					}
				}
				date = date.plusDays(1);
			}

			transaction.commit();

			return result;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void saveReport(DateTime startDate, String region, User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
					.setParameter("date", startDate).setParameter("region", region).getResultList();

			// get revision number
			AuditReader reader = AuditReaderFactory.get(entityManager);
			int revision = (int) reader.createQuery().forRevisionsOfEntity(AvalancheBulletin.class, false, true)
					.addProjection(AuditEntity.revisionNumber().max()).getSingleResult();

			JSONObject data = null;

			if (reports.isEmpty()) {
				AvalancheReport avalancheReport = new AvalancheReport();
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setDate(startDate);
				avalancheReport.setRegion(region);
				avalancheReport.setStatus(BulletinStatus.draft);
				avalancheReport.setRevision(revision);
				entityManager.persist(avalancheReport);
				data = JsonUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else if (reports.size() == 1) {
				AvalancheReport avalancheReport = reports.get(0);
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setRevision(revision);
				switch (avalancheReport.getStatus()) {
				case draft:
					break;
				case submitted:
					avalancheReport.setStatus(BulletinStatus.draft);
					break;
				case published:
					avalancheReport.setStatus(BulletinStatus.updated);
					break;
				case updated:
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
				data = JsonUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			// TODO implement via websockets
			// if (data != null)
			// SocketIOController.getInstance().sendEvent(EventName.bulletinUpdate.toString(),
			// data.toString());

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public String changeReport(DateTime startDate, String region, User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
					.setParameter("date", startDate).setParameter("region", region).getResultList();

			// get revision number
			AuditReader reader = AuditReaderFactory.get(entityManager);
			int revision = (int) reader.createQuery().forRevisionsOfEntity(AvalancheBulletin.class, false, true)
					.addProjection(AuditEntity.revisionNumber().max()).getSingleResult();

			AvalancheReport avalancheReport;
			if (reports.size() == 1) {
				avalancheReport = reports.get(0);
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setRevision(revision);
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			return avalancheReport.getId();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public List<String> publishReport(DateTime startDate, List<String> regions, User user, DateTime publicationDate)
			throws AlbinaException {
		List<String> avalancheReportIds = new ArrayList<String>();
		for (String region : regions) {
			String avalancheReportId = publishReport(startDate, region, user, publicationDate);
			avalancheReportIds.add(avalancheReportId);
		}
		return avalancheReportIds;
	}

	@SuppressWarnings("unchecked")
	public String publishReport(DateTime startDate, String region, User user, DateTime publicationDate)
			throws AlbinaException {

		// TODO save CAAML and JSON to report?

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
					.setParameter("date", startDate).setParameter("region", region).getResultList();

			// get revision number
			AuditReader reader = AuditReaderFactory.get(entityManager);
			int revision = 1;
			AuditQuery revisionsOfEntity = reader.createQuery().forRevisionsOfEntity(AvalancheBulletin.class, false,
					true);
			if (revisionsOfEntity != null) {
				AuditQuery addProjection = revisionsOfEntity.addProjection(AuditEntity.revisionNumber().max());
				if (addProjection != null) {
					Object singleResult = addProjection.getSingleResult();
					if (singleResult != null)
						revision = (int) singleResult;
				}
			}

			JSONObject data = null;

			AvalancheReport avalancheReport;

			if (reports.isEmpty()) {
				avalancheReport = new AvalancheReport();
				avalancheReport.setTimestamp(publicationDate);
				avalancheReport.setUser(user);
				avalancheReport.setDate(startDate);
				avalancheReport.setRegion(region);
				avalancheReport.setStatus(BulletinStatus.published);
				avalancheReport.setRevision(revision);
				entityManager.persist(avalancheReport);
				data = JsonUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else if (reports.size() == 1) {
				avalancheReport = reports.get(0);
				avalancheReport.setTimestamp(publicationDate);
				avalancheReport.setUser(user);
				avalancheReport.setRevision(revision);
				switch (avalancheReport.getStatus()) {
				case missing:
					logger.warn("Bulletins have to be created first!");
					avalancheReport.setStatus(BulletinStatus.published);
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
				data = JsonUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			// TODO implement via websockets
			// if (data != null)
			// SocketIOController.getInstance().sendEvent(EventName.bulletinUpdate.toString(),
			// data.toString());

			return avalancheReport.getId();

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void submitReport(DateTime startDate, String region, User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
					.setParameter("date", startDate).setParameter("region", region).getResultList();

			// get revision number
			AuditReader reader = AuditReaderFactory.get(entityManager);
			int revision = 0;
			AuditQuery revisionsOfEntity = reader.createQuery().forRevisionsOfEntity(AvalancheBulletin.class, false,
					true);
			if (revisionsOfEntity != null) {
				AuditQuery addProjection = revisionsOfEntity.addProjection(AuditEntity.revisionNumber().max());
				if (addProjection != null) {
					Object singleResult = addProjection.getSingleResult();
					if (singleResult != null)
						revision = (int) singleResult;
				}
			}

			JSONObject data = null;

			if (reports.isEmpty()) {
				AvalancheReport avalancheReport = new AvalancheReport();
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setDate(startDate);
				avalancheReport.setRegion(region);
				avalancheReport.setStatus(BulletinStatus.missing);
				avalancheReport.setRevision(revision);
				entityManager.persist(avalancheReport);
				data = JsonUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else if (reports.size() == 1) {
				AvalancheReport avalancheReport = reports.get(0);
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setRevision(revision);
				switch (avalancheReport.getStatus()) {
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
				data = JsonUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			// TODO implement via websockets
			// if (data != null)
			// SocketIOController.getInstance().sendEvent(EventName.bulletinUpdate.toString(),
			// data.toString());

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public AvalancheBulletinVersionTuple getPublishedBulletins(DateTime startDate, DateTime endDate,
			List<String> regions) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();

		int revision = -1;

		try {
			Map<String, Integer> revisionMap = new HashMap<String, Integer>();
			Map<String, AvalancheBulletin> resultMap = new HashMap<String, AvalancheBulletin>();

			for (String region : regions) {
				// get bulletins for this region
				AvalancheBulletinVersionTuple publishedBulletinsForRegion = getPublishedBulletinsForRegion(startDate,
						endDate, region);

				for (AvalancheBulletin bulletin : publishedBulletinsForRegion.bulletins) {
					if (revisionMap.containsKey(bulletin.getId())) {
						// merge bulletins with same id
						if (revisionMap.get(bulletin.getId()).equals(publishedBulletinsForRegion.version)
								|| bulletin.equals(resultMap.get(bulletin.getId()))) {
							for (String publishedRegion : bulletin.getPublishedRegions()) {
								resultMap.get(bulletin.getId()).addPublishedRegion(publishedRegion);
							}
							// create new bulletin if revisions are different
						} else {
							bulletin.setId(bulletin.getId() + "_" + revisionMap.get(bulletin.getId()));
							if (!revisionMap.containsKey(bulletin.getId())) {
								revisionMap.put(bulletin.getId(), publishedBulletinsForRegion.version);
								resultMap.put(bulletin.getId(), bulletin);
							} else {
								for (String publishedRegion : bulletin.getPublishedRegions()) {
									resultMap.get(bulletin.getId()).addPublishedRegion(publishedRegion);
								}
							}
						}
					} else {
						revisionMap.put(bulletin.getId(), publishedBulletinsForRegion.version);
						resultMap.put(bulletin.getId(), bulletin);
					}
				}
			}

			return new AvalancheBulletinVersionTuple(revision, resultMap.values());
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	private AvalancheBulletinVersionTuple getPublishedBulletinsForRegion(DateTime startDate, DateTime endDate,
			String region) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		Number revision = -1;

		try {
			// get report for date and region
			transaction.begin();
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReportsForRegion)
					.setParameter("date", startDate).setParameter("region", region).getResultList();
			transaction.commit();

			List<AvalancheBulletin> results = new ArrayList<AvalancheBulletin>();

			if (reports != null && !reports.isEmpty()) {

				List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();

				// get report history
				transaction.begin();
				AuditReader reader = AuditReaderFactory.get(entityManager);
				AuditQuery q = reader.createQuery().forRevisionsOfEntity(AvalancheReport.class, true, true);
				q.add(AuditEntity.id().eq(reports.get(0).getId()));
				List<AvalancheReport> audit = q.getResultList();
				transaction.commit();

				// get latest report with status published or republished
				for (AvalancheReport avalancheReport : audit) {
					if ((avalancheReport.getRegion().startsWith(region)
							|| region.startsWith(avalancheReport.getRegion()))
							&& (avalancheReport.getStatus() == BulletinStatus.published
									|| avalancheReport.getStatus() == BulletinStatus.republished)) {
						if (avalancheReport.getRevision().intValue() > revision.intValue()) {
							revision = avalancheReport.getRevision();
						}
					}
				}

				if (revision.intValue() > -1) {
					transaction.begin();
					AuditQuery q2 = reader.createQuery().forEntitiesAtRevision(AvalancheBulletin.class, revision);
					bulletins = q2.getResultList();

					// just used to initialize all necessary fields
					for (AvalancheBulletin avalancheBulletin : bulletins)
						initializeBulletin(avalancheBulletin);

					transaction.commit();
				}

				for (AvalancheBulletin bulletin : bulletins) {
					if (bulletin.getValidFrom().toDateTime(DateTimeZone.UTC)
							.equals(startDate.toDateTime(DateTimeZone.UTC))
							|| bulletin.getValidUntil().toDateTime(DateTimeZone.UTC)
									.equals(endDate.toDateTime(DateTimeZone.UTC))) {
						Set<String> newPublishedRegions = new HashSet<String>();

						// delete all published regions which are foreign
						if (bulletin.getPublishedRegions() != null) {
							for (String publishedRegion : bulletin.getPublishedRegions()) {
								if (publishedRegion.startsWith(region))
									newPublishedRegions.add(publishedRegion);
							}
							if (newPublishedRegions.size() != 0) {
								bulletin.setPublishedRegions(newPublishedRegions);
								results.add(bulletin);
							}
						}
					}
				}
			}

			return new AvalancheBulletinVersionTuple(revision.intValue(), results);

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	private void initializeBulletin(AvalancheBulletin bulletin) {
		if (bulletin.getUser() != null)
			bulletin.setUser(initializeAndUnproxy(bulletin.getUser()));
		if (bulletin.getForenoon() != null) {
			bulletin.setForenoon(initializeAndUnproxy(bulletin.getForenoon()));
			if (bulletin.getForenoon().getAvalancheSituation1() != null) {
				bulletin.getForenoon()
						.setAvalancheSituation1(initializeAndUnproxy(bulletin.getForenoon().getAvalancheSituation1()));
				bulletin.getForenoon().getAvalancheSituation1().getAspects().size();
			}
			if (bulletin.getForenoon().getAvalancheSituation2() != null) {
				bulletin.getForenoon()
						.setAvalancheSituation2(initializeAndUnproxy(bulletin.getForenoon().getAvalancheSituation2()));
				bulletin.getForenoon().getAvalancheSituation2().getAspects().size();
			}
		}
		if (bulletin.getAfternoon() != null) {
			bulletin.setAfternoon(initializeAndUnproxy(bulletin.getAfternoon()));
			if (bulletin.getAfternoon().getAvalancheSituation1() != null) {
				bulletin.getAfternoon()
						.setAvalancheSituation1(initializeAndUnproxy(bulletin.getAfternoon().getAvalancheSituation1()));
				bulletin.getAfternoon().getAvalancheSituation1().getAspects().size();
			}
			if (bulletin.getAfternoon().getAvalancheSituation2() != null) {
				bulletin.getAfternoon()
						.setAvalancheSituation2(initializeAndUnproxy(bulletin.getAfternoon().getAvalancheSituation2()));
				bulletin.getAfternoon().getAvalancheSituation2().getAspects().size();
			}
		}
		if (bulletin.getAdditionalAuthors() != null)
			bulletin.getAdditionalAuthors().size();
		if (bulletin.getPublishedRegions() != null)
			bulletin.getPublishedRegions().size();
		if (bulletin.getSuggestedRegions() != null)
			bulletin.getSuggestedRegions().size();
		if (bulletin.getSavedRegions() != null)
			bulletin.getSavedRegions().size();
		if (bulletin.getTextPartsMap() != null) {
			bulletin.getTextPartsMap().size();
			for (Texts element : bulletin.getTextPartsMap().values())
				element.getTexts().size();
		}
	}

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

	public void setAvalancheReportWhatsappFlag(String avalancheReportId) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
			avalancheReport.setWhatsappSent(true);
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

	public void setAvalancheReportTelegramFlag(String avalancheReportId) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			AvalancheReport avalancheReport = entityManager.find(AvalancheReport.class, avalancheReportId);
			avalancheReport.setTelegramSent(true);
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
}

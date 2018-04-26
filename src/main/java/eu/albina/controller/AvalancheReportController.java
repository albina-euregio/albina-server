package eu.albina.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.HibernateException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinVersionTuple;
import eu.albina.model.AvalancheReport;
import eu.albina.model.User;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.EventName;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.AuthorizationUtil;
import eu.albina.util.HibernateUtil;

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
	public BulletinStatus getStatus(DateTime startDate, String region) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReports)
					.setParameter("date", startDate).setParameter("region", region).getResultList();

			BulletinStatus result = BulletinStatus.missing;
			if (reports.size() == 1)
				result = reports.get(0).getStatus();
			else if (reports.size() > 1)
				throw new AlbinaException("Report error!");
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
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReports)
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
				data = AlbinaUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
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
				data = AlbinaUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			if (data != null)
				SocketIOController.getInstance().sendEvent(EventName.bulletinUpdate.toString(), data.toString());

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void changeReport(DateTime startDate, String region, User user) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReports)
					.setParameter("date", startDate).setParameter("region", region).getResultList();

			// get revision number
			AuditReader reader = AuditReaderFactory.get(entityManager);
			int revision = (int) reader.createQuery().forRevisionsOfEntity(AvalancheBulletin.class, false, true)
					.addProjection(AuditEntity.revisionNumber().max()).getSingleResult();

			if (reports.size() == 1) {
				AvalancheReport avalancheReport = reports.get(0);
				avalancheReport.setTimestamp(new DateTime());
				avalancheReport.setUser(user);
				avalancheReport.setRevision(revision);
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public void publishReport(DateTime startDate, List<String> regions, User user, DateTime publicationDate)
			throws AlbinaException {
		for (String region : regions)
			publishReport(startDate, region, user, publicationDate);
	}

	@SuppressWarnings("unchecked")
	public void publishReport(DateTime startDate, String region, User user, DateTime publicationDate)
			throws AlbinaException {

		// TODO save CAAML and JSON to report?

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get report
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReports)
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
				avalancheReport.setTimestamp(publicationDate);
				avalancheReport.setUser(user);
				avalancheReport.setDate(startDate);
				avalancheReport.setRegion(region);
				avalancheReport.setStatus(BulletinStatus.published);
				avalancheReport.setRevision(revision);
				entityManager.persist(avalancheReport);
				data = AlbinaUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else if (reports.size() == 1) {
				AvalancheReport avalancheReport = reports.get(0);
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
				data = AlbinaUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			if (data != null)
				SocketIOController.getInstance().sendEvent(EventName.bulletinUpdate.toString(), data.toString());

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
			List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReports)
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
				data = AlbinaUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
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
				data = AlbinaUtil.createBulletinStatusUpdateJson(region, startDate, avalancheReport.getStatus());
			} else {
				throw new AlbinaException("Report error!");
			}

			transaction.commit();

			if (data != null)
				SocketIOController.getInstance().sendEvent(EventName.bulletinUpdate.toString(), data.toString());

		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public AvalancheBulletinVersionTuple getPublishedBulletins(DateTime startDate, DateTime endDate,
			List<String> regions) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
			int revision = 0;

			for (String region : regions) {
				// get report
				transaction.begin();
				List<AvalancheReport> reports = entityManager.createQuery(HibernateUtil.queryGetReports)
						.setParameter("date", startDate).setParameter("region", region).getResultList();
				transaction.commit();

				List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();

				if (reports != null && !reports.isEmpty()) {
					transaction.begin();
					AuditReader reader = AuditReaderFactory.get(entityManager);
					AuditQuery q = reader.createQuery().forRevisionsOfEntity(AvalancheReport.class, true, true);
					q.add(AuditEntity.id().eq(reports.get(0).getId()));
					List<AvalancheReport> audit = q.getResultList();
					transaction.commit();

					for (AvalancheReport avalancheReport : audit) {
						if (avalancheReport.getRegion().startsWith(region)
								&& (avalancheReport.getStatus() == BulletinStatus.published
										|| avalancheReport.getStatus() == BulletinStatus.republished)) {
							if (avalancheReport.getRevision().intValue() > revision) {
								transaction.begin();
								AuditQuery q2 = reader.createQuery().forEntitiesAtRevision(AvalancheBulletin.class,
										avalancheReport.getRevision());
								bulletins = q2.getResultList();

								transaction.commit();
								revision = avalancheReport.getRevision().intValue();
							}
						}
					}
					for (AvalancheBulletin bulletin : bulletins)
						if (AuthorizationUtil.getRegion(bulletin.getUser().getRole()).startsWith(region)
								&& (bulletin.getValidFrom().equals(startDate)
										|| bulletin.getValidUntil().equals(endDate)))
							result.add(bulletin);
				}
			}

			// just used to initialize all necessary fields
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try {
				docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				for (AvalancheBulletin avalancheBulletin : result) {
					avalancheBulletin.toCAAML(doc, LanguageCode.en, startDate, 0);
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			return new AvalancheBulletinVersionTuple(revision, result);
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}
}

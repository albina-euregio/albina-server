// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

public class HibernateUtil {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

	private static HibernateUtil instance = null;

	private EntityManagerFactory entityManagerFactory;

	public static String queryGetBulletins = "from AvalancheBulletin as b where b.validFrom = :startDate or b.validUntil = :endDate";
	public static String queryGetReports = "from AvalancheReport as r where r.date between :startDate and :endDate";
	public static String queryGetReportsStartDate = "from AvalancheReport as r where r.date = :startDate";
	public static String queryGetSubscribersForLanguage = "from Subscriber as s where :language = s.language";
	public static String queryGetUsers = "from User as u where u.deleted = 0";
	public static String queryGetUserRegionRoleLinks = "from UserRegionRoleLink as l where l.user = :userEmail and l.region = :region";

	public static String queryGetReportsForDayAndRegion = "from AvalancheReport as r where r.date = :date and r.region = :region";
	public static String queryGetReportsForTimePeriodAndRegion = "from AvalancheReport as r where r.date between :startDate and :endDate and r.region = :region";

	public static String queryGetLatestDate = "from AvalancheReport as r where r.status = 3 or r.status = 0 order by r.date desc";

	public static String queryGetLocalServerInstance = "from ServerInstance as i where i.externalServer = false";
	public static String queryGetExternalServerInstances = "from ServerInstance as i where i.externalServer = true";
	public static String queryGetTelegramConfiguration = "from TelegramConfiguration as c where c.region = :region and c.lang = :lang";
	public static String queryGetWhatsAppConfiguration = "from WhatsAppConfiguration as c where c.region = :region and c.lang = :lang";

	public static String queryGetDangerSourceVariants = "from DangerSourceVariant as v where v.validFrom = :startDate or v.validUntil = :endDate";
	public static String queryGetDangerSourceVariantsForTimePeriod = "from DangerSourceVariant as v where v.validFrom between :startDate and :endDate";
	public static String queryGetDangerSources = "from DangerSource as d where d.creationDate between :startDate and :endDate";

	public static String queryGetDangerSourceVariantTexts = "from DangerSourceVariantText as t where t.avalancheType = :avalancheType";

	public static HibernateUtil getInstance() {
		if (System.getenv("CI_JOB_NAME") != null) {
			// https://docs.gitlab.com/ee/ci/variables/#list-all-environment-variables
			throw new IllegalStateException("No Hibernate for continuous integration!");
		}
		if (instance == null) {
			instance = new HibernateUtil();
		}
		return instance;
	}

	private HibernateUtil() {
	}

	public void setUp() {
		Map<String, String> properties = DBEnvConfig.initConfig(System.getenv()).asMap();
		entityManagerFactory = Persistence.createEntityManagerFactory("eu.albina", properties);
		logger.info("Entity manager factory created!");
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public <T> T runTransaction(Consumer<EntityManager> execution, Supplier<T> returnSupplier) {
		return runTransaction(entityManager -> {
			execution.accept(entityManager);
			return returnSupplier.get();
		});
	}

	public <T> T runTransaction(Function<EntityManager, T> function) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			final T result = function.apply(entityManager);
			transaction.commit();
			return result;
		} catch (NoResultException e) {
			transaction.rollback();
			throw e;
		} catch (PersistenceException e) {
			logger.warn("PersistenceException: " + e.getMessage(), e);
			transaction.rollback();
			throw e;
		} finally {
			entityManager.close();
		}
	}

	public <T> T run(Function<EntityManager, T> function) {
		try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
			return function.apply(entityManager);
		} catch (NoResultException e) {
			throw e;
		} catch (PersistenceException e) {
			logger.warn("PersistenceException: " + e.getMessage(), e);
			throw e;
		}
	}

	public void shutDown() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
			logger.info("Entity manager factory closed!");
		}
	}
}

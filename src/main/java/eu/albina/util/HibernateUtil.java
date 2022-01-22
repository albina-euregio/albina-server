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
package eu.albina.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

	private static HibernateUtil instance = null;

	private EntityManagerFactory entityManagerFactory;

	public static String queryGetBulletins = "from AvalancheBulletin as b where b.validFrom = :startDate or b.validUntil = :endDate";
	public static String queryGetReports = "from AvalancheReport as r where r.date between :startDate and :endDate";
	public static String queryGetReportsForRegion = "from AvalancheReport as r where r.date between :startDate and :endDate and :region = r.region";
	public static String queryGetReportsForRegionStartDate = "from AvalancheReport as r where r.date = :startDate and :region = r.region";
	public static String queryGetReportsStartDate = "from AvalancheReport as r where r.date = :startDate";
	public static String queryGetSubscribersForLanguage = "from Subscriber as s where :language = s.language";
	public static String queryGetChatMessages = "from ChatMessage as cm";
	public static String queryGetChatMessagesDate = "from ChatMessage as cm where cm.dateTime >= :date";
	public static String queryGetUsers = "from User as u";

	public static String queryGetReportsForDayAndRegion = "from AvalancheReport as r where r.date = :date and r.region = :region";
	public static String queryGetReportsForTimePeriodAndRegion = "from AvalancheReport as r where r.date between :startDate and :endDate and r.region = :region";

	public static String queryGetLatestDate = "from AvalancheReport as r where r.status = 3 or r.status = 0 order by r.date desc";

	public static HibernateUtil getInstance() {
		if (instance == null) {
			instance = new HibernateUtil();
		}
		return instance;
	}

	public HibernateUtil() {
	}

	public void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("eu.albina");
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
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			final T result = function.apply(entityManager);
			transaction.commit();
			return result;
		} catch (HibernateException e) {
			logger.warn("HibernateException: " + e.getMessage());
			transaction.rollback();
			throw e;
		} finally {
			entityManager.close();
		}
	}

	public void shutDown() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
			logger.info("Entity manager factory closed!");
		}
	}
}

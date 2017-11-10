package eu.albina.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

	private static HibernateUtil instance = null;

	private EntityManagerFactory entityManagerFactory;

	public static String queryGetBulletins = "from AvalancheBulletin as b where :startDate = b.validFrom or :endDate = b.validUntil";
	public static String queryGetTopLevelRegions = "from Region as r where r.parentRegion is null";
	public static String queryGetSubregions = "from Region as r where r.parentRegion.id = :regionId";
	public static String queryGetNews = "from News as n";
	public static String queryGetNewsStart = "from News as n where n.datetime >= :startDate";
	public static String queryGetNewsEnd = "from News as n where n.datetime <= :endDate";
	public static String queryGetNewsStartEnd = "from News as n where n.datetime >= :startDate and n.datetime <= :endDate";
	public static String queryGetChatMessages = "from ChatMessage as cm";
	public static String queryGetChatMessagesDate = "from ChatMessage as cm where cm.dateTime >= :date";

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
		logger.info("Entity manager created!");
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public void shutDown() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
			logger.info("Entity manager closed!");
		}
	}
}

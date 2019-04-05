package eu.albina.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
	public static String queryGetTopLevelRegions = "from Region as r where r.parentRegion is null";
	public static String queryGetSubregions = "from Region as r where r.parentRegion.id = :regionId";
	public static String queryGetChatMessages = "from ChatMessage as cm";
	public static String queryGetChatMessagesDate = "from ChatMessage as cm where cm.dateTime >= :date";

	public static String queryGetLatestReports = ""
			+ "from "
			+ "		AvalancheReport as report"
			+ "where "
			+ "		report.id = some ("
			+ "			select "
			+ "				AvalancheReport.id, "
			+ "				AvalancheReport.date, "
			+ "				AvalancheReport.region, "
			+ "				MAX(date) as MaxDate "
			+ "			from "
			+ "				AvalancheReport "
			+ "			where "
			+ "				(status=3 or status=6) and date between :startDate and :endDate "
			+ "			group by "
			+ "				date, "
			+ "				region"
			+ "		)"
			+ ";";

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

	public void shutDown() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
			logger.info("Entity manager factory closed!");
		}
	}
}

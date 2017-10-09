package eu.albina.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

	private static HibernateUtil instance = null;

	private SessionFactory sessionFactory;
	private final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();

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

	public void createSessionFactory() {
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			logger.debug("Session factory created!");
		} catch (Exception e) {
			logger.error("Error creating Session: " + e);
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	public void closeSessionFactory() {
		if (sessionFactory != null) {
			try {
				sessionFactory.close();
				logger.debug("Session factory closed!");
			} catch (HibernateException ignored) {
				logger.error("Couldn't close SessionFactory", ignored);
			}
		}
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}

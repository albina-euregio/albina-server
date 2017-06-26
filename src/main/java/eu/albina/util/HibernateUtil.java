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

	private static SessionFactory sessionFactory;
	private static final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();

	public static String queryGetBulletins = "from AvalancheBulletin as b where b.validFrom <= :date and b.validUntil > :date";

	public static void createSessionFactory() {
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			logger.debug("Session factory created!");
		} catch (Exception e) {
			logger.error("Error creating Session: " + e);
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	public static void closeSessionFactory() {
		if (sessionFactory != null) {
			try {
				sessionFactory.close();
				logger.debug("Session factory closed!");
			} catch (HibernateException ignored) {
				logger.error("Couldn't close SessionFactory", ignored);
			}
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}

package eu.albina.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

	private static SessionFactory sessionFactory;

	public static void createSessionFactory() {
		try {
			Configuration configuration = new Configuration().configure();
			sessionFactory = configuration.buildSessionFactory();
			logger.debug("Session factory created!");
		} catch (HibernateException he) {
			System.err.println("Error creating Session: " + he);
			throw new ExceptionInInitializerError(he);
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

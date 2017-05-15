package eu.albina.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;

public class HibernateUtil {

	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

	private static SessionFactory sessionFactory;
	private static final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();

	public static String queryGetBulletinsValidFrom = "from AvalancheBulletin as b where b.validFrom <= :validFrom and b.validUntil >= :validFrom";

	public static void createSessionFactory() {
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			logger.debug("Session factory created!");

			// TODO just for testing, delete
			loadBulletins();

		} catch (Exception e) {
			logger.error("Error creating Session: " + e);
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	/** Just for testing **/
	public static void loadBulletins() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		ArrayList<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();

		for (int i = 1; i < 6; i++) {
			InputStream is = classloader.getResourceAsStream("bulletin_" + i + ".json");
			StringBuilder bulletinStringBuilder = new StringBuilder();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = in.readLine()) != null) {
					bulletinStringBuilder.append(line);
				}
			} catch (Exception e) {
				logger.warn("Error parsing bulletin!");
			}

			JSONObject jsonBulletin = new JSONObject(bulletinStringBuilder.toString());
			bulletins.add(new AvalancheBulletin(jsonBulletin, "norbert.lanzanasto@tirol.gv.at"));
		}

		for (AvalancheBulletin bulletin : bulletins) {
			try {
				AvalancheBulletinController.getInstance().saveBulletin(bulletin);
			} catch (AlbinaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

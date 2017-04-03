package eu.albina.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.SnowProfile;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.CountryCode;
import eu.albina.util.GlobalVariables;

/**
 * Controller for snow profiles.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class SnowProfileController extends AlbinaController {

	private static Logger logger = LoggerFactory.getLogger(SnowProfileController.class);

	private static SnowProfileController instance = null;

	private SnowProfileController() {
		loadSnowProfiles();
	}

	public static SnowProfileController getInstance() {
		if (instance == null) {
			instance = new SnowProfileController();
		}
		return instance;
	}

	private void loadSnowProfiles() {
		// TODO just for testing
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		for (int i = 2377; i <= 5430; i++) {
			InputStream is = classloader.getResourceAsStream("snowprofiles/profile" + i + ".json");
			if (is != null) {
				StringBuilder snowProfileStringBuilder = new StringBuilder();
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(is));
					String line = null;
					while ((line = in.readLine()) != null) {
						snowProfileStringBuilder.append(line);
					}
					JSONObject json = new JSONObject(snowProfileStringBuilder.toString());
					SnowProfile profile = new SnowProfile(json);
					saveSnowProfile(profile);
				} catch (Exception e) {
					logger.warn("Error parsing snow profile - " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Retrieve a snow profile from the database by ID.
	 * 
	 * @param profileId
	 *            The ID of the desired snow profile.
	 * @return The snow profile with the given ID.
	 * @throws AlbinaException
	 */
	public SnowProfile getSnowProfile(String profileId) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			SnowProfile snowProfile = session.get(SnowProfile.class, profileId);
			if (snowProfile == null) {
				transaction.rollback();
				throw new AlbinaException("No profile with ID: " + profileId);
			}
			Hibernate.initialize(snowProfile.getLayers());
			Hibernate.initialize(snowProfile.getStabilityTests());
			Hibernate.initialize(snowProfile.getTemperatureProfile());
			Hibernate.initialize(snowProfile.getRammProfile());
			Hibernate.initialize(snowProfile.getDensityProfile());
			Hibernate.initialize(snowProfile.getComment());
			transaction.commit();
			return snowProfile;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public Serializable saveSnowProfile(SnowProfile profile) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Serializable profileId = session.save(profile);
			transaction.commit();
			return profileId;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public void updateSnowProfile(SnowProfile profile) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.update(profile);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<SnowProfile> getSnowProfiles(int page, CountryCode country, String region, String subregion,
			DateTime startDate, DateTime endDate, int above, int below, List<Aspect> aspects)
			throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(SnowProfile.class)
					.setFirstResult((page - 1) * GlobalVariables.paginationCount)
					.setFetchSize(GlobalVariables.paginationCount);

			if (country != null) {
				criteria.add(Restrictions.eq("location.countryCode", country));
			}
			if (region != null && !region.isEmpty()) {
				criteria.add(Restrictions.eq("location.region", region).ignoreCase());
			}
			if (subregion != null && !subregion.isEmpty()) {
				criteria.add(Restrictions.eq("location.subregion", subregion).ignoreCase());
			}
			if (startDate != null) {
				criteria.add(Restrictions.ge("dateTime.dateTime", startDate));
			}
			if (endDate != null) {
				criteria.add(Restrictions.le("dateTime.dateTime", endDate));
			}
			if (above > 0) {
				criteria.add(Restrictions.ge("location.elevation", above));
			}
			if (below > 0) {
				criteria.add(Restrictions.le("location.elevation", below));
			}
			if (aspects != null && !aspects.isEmpty()) {
				criteria.add(Restrictions.in("location.aspect", aspects));
			}

			List<SnowProfile> profiles = criteria.list();
			transaction.commit();
			return profiles;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public void deleteSnowProfile(String profileId) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			SnowProfile snowProfile = session.get(SnowProfile.class, profileId);
			session.delete(snowProfile);
			session.delete(snowProfile);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

}

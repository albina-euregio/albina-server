package eu.albina.controller.socialmedia;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.hibernate.HibernateException;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.Channel;
import eu.albina.model.socialmedia.MessengerPeopleConfig;
import eu.albina.model.socialmedia.RapidMailConfig;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.model.socialmedia.TwitterConfig;
import eu.albina.util.HibernateUtil;

public class RegionConfigurationController extends CommonProcessor {
	ObjectMapper objectMapper = new ObjectMapper();

	private static RegionConfigurationController instance = null;

	public static RegionConfigurationController getInstance() {
		if (instance == null) {
			instance = new RegionConfigurationController();
		}
		return instance;
	}

	public RegionConfigurationController() {
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		objectMapper.registerModule(new JtsModule());
	}

	public RegionConfiguration getRegionConfiguration(String regionConfigurationId) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			TypedQuery<Long> query = entityManager.createQuery(
					"SELECT c.id FROM RegionConfiguration c WHERE c.region.id='" + regionConfigurationId + "'",
					Long.class);
			Long id = query.getSingleResult();
			RegionConfiguration regionConfiguration = entityManager.find(RegionConfiguration.class, id);
			if (regionConfiguration == null) {
				transaction.rollback();
				throw new AlbinaException("No configuration with ID: " + regionConfigurationId);
			}
			transaction.commit();
			if (regionConfiguration.getMessengerPeopleConfig() == null) {
				regionConfiguration.setMessengerPeopleConfig(new MessengerPeopleConfig());
			}
			if (regionConfiguration.getTwitterConfig() == null) {
				regionConfiguration.setTwitterConfig(new TwitterConfig());
			}
			if (regionConfiguration.getRapidMailConfig() == null) {
				regionConfiguration.setRapidMailConfig(new RapidMailConfig());
			}
			return regionConfiguration;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Long saveRegionConfiguration(RegionConfiguration regionConfiguration) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.merge(regionConfiguration.getMessengerPeopleConfig());
			entityManager.merge(regionConfiguration.getRapidMailConfig());
			entityManager.merge(regionConfiguration.getTwitterConfig());
			entityManager.merge(regionConfiguration);
			transaction.commit();
			return regionConfiguration.getId();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public List<Channel> getChannels() throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			TypedQuery<Channel> query = entityManager.createQuery("SELECT c FROM Channel c", Channel.class);
			List<Channel> channelList = query.getResultList();
			transaction.commit();
			return channelList;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

}

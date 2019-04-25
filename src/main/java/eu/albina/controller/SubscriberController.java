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
package eu.albina.controller;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import eu.albina.controller.socialmedia.RapidMailProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.Subscriber;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.rapidmail.recipients.post.PostRecipientsRequest;
import eu.albina.model.socialmedia.RegionConfiguration;
import eu.albina.util.HibernateUtil;

/**
 * Controller for subscribers.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class SubscriberController {
	// private static Logger logger =
	// LoggerFactory.getLogger(SubscriberController.class);
	private static SubscriberController instance = null;

	private SubscriberController() {
	}

	public static SubscriberController getInstance() {
		if (instance == null) {
			instance = new SubscriberController();
		}
		return instance;
	}

	public Subscriber getSubscriber(String email) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			Subscriber subscriber = entityManager.find(Subscriber.class, email);
			if (subscriber == null) {
				transaction.rollback();
				throw new AlbinaException("No subscriber with email: " + email);
			}
			transaction.commit();

			return subscriber;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Serializable createSubscriber(Subscriber subscriber)
			throws HibernateException, URISyntaxException, IOException, AlbinaException {

		try {
			Subscriber s = getSubscriber(subscriber.getEmail());
			if (s != null)
				deleteSubscriber(subscriber.getEmail());
		} catch (AlbinaException e) {
		}

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(subscriber);
			transaction.commit();
			return subscriber.getEmail();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw he;
		} finally {
			entityManager.close();
		}
	}

	public Serializable deleteSubscriber(String email) throws AlbinaException, HibernateException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			Subscriber subscriber = entityManager.find(Subscriber.class, email);
			if (subscriber == null) {
				transaction.rollback();
				throw new AlbinaException("No subscriber with email: " + email);
			}
			entityManager.remove(subscriber);
			transaction.commit();
			return subscriber.getEmail();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw he;
		} finally {
			entityManager.close();
		}
	}

	public void confirmSubscriber(String email) throws AlbinaException, HibernateException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			Subscriber subscriber = getSubscriber(email);
			subscriber.setConfirmed(true);
			entityManager.merge(subscriber);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw he;
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Subscriber> getSubscribers(LanguageCode lang, List<String> regions) throws HibernateException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<Subscriber> subscribers = entityManager.createQuery(HibernateUtil.queryGetSubscribersForLanguage)
					.setParameter("language", lang).getResultList();
			List<Subscriber> results = new ArrayList<Subscriber>();
			for (Subscriber subscriber : subscribers) {
				for (String region : regions)
					if (subscriber.affectsRegion(region)) {
						results.add(subscriber);
						break;
					}
			}

			for (Subscriber subscriber : results)
				initializeSubscriber(subscriber);

			transaction.commit();
			return results;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw he;
		} finally {
			entityManager.close();
		}
	}

	public List<String> getSubscriberEmails(LanguageCode lang, List<String> regions) throws HibernateException {
		List<String> result = new ArrayList<String>();
		List<Subscriber> subscribers = getSubscribers(lang, regions);

		for (Subscriber subscriber : subscribers)
			result.add(subscriber.getEmail());

		return result;
	}

	private void initializeSubscriber(Subscriber subscriber) {
		Hibernate.initialize(subscriber.getEmail());
		Hibernate.initialize(subscriber.getConfirmed());
		Hibernate.initialize(subscriber.getLanguage());
		Hibernate.initialize(subscriber.getPdfAttachment());
		Hibernate.initialize(subscriber.getRegions());
	}

	public void createSubscriberRapidmail(Subscriber subscriber) throws AlbinaException, KeyManagementException,
			CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, Exception {
		if (subscriber.getLanguage() == null)
			throw new AlbinaException("No language defined!");
		for (String region : subscriber.getRegions()) {
			RegionConfigurationController ctRc = RegionConfigurationController.getInstance();
			RegionConfiguration rc = ctRc.getRegionConfiguration(region);
			RapidMailProcessorController ctRm = RapidMailProcessorController.getInstance();

			PostRecipientsRequest recipient = new PostRecipientsRequest();
			recipient.setEmail(subscriber.getEmail());

			ctRm.createRecipient(rc.getRapidMailConfig(), recipient, null, subscriber.getLanguage().toString());
		}
	}
}

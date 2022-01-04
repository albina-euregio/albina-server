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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

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
 * Controller for email subscribers. The sending of emails is handled by
 * rapidmail, but in order to apply to GDPR regulations the subscription to the
 * email newsletter is done by this controller and the confirmation of the user
 * is stored in our database.
 *
 * @author Norbert Lanzanasto
 *
 */
public class SubscriberController {
	// private static Logger logger =
	// LoggerFactory.getLogger(SubscriberController.class);
	private static SubscriberController instance = null;

	/**
	 * Private constructor.
	 */
	private SubscriberController() {
	}

	/**
	 * Returns the {@code SubscriberController} object associated with the current
	 * Java application.
	 *
	 * @return the {@code SubscriberController} object associated with the current
	 *         Java application.
	 */
	public static SubscriberController getInstance() {
		if (instance == null) {
			instance = new SubscriberController();
		}
		return instance;
	}

	/**
	 * Return the {@code Subscriber} object with {@code email} as primary key.
	 *
	 * @param email
	 *            the email address of the desired subscriber
	 * @return the {@code Subscriber} object with {@code email} as primary key
	 * @throws HibernateException
	 *             if the subscriber could not be found
	 */
	public Subscriber getSubscriber(String email) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			Subscriber subscriber = entityManager.find(Subscriber.class, email);
			if (subscriber == null) {
				throw new HibernateException("No subscriber with email: " + email);
			}
			return subscriber;
		});
	}

	/**
	 * Save a {@code Subscriber} to the database. If a {@code Subscriber} with the
	 * same email address already exists it will be deleted first and newly created.
	 *
	 * @param subscriber
	 *            the {@code Subscriber} to be saved in the database
	 * @return the email address of the saved subscriber
	 * @throws HibernateException
	 *             if the {@code Subscriber} could not be saved
	 */
	public Serializable createSubscriber(Subscriber subscriber) throws HibernateException {

		try {
			Subscriber s = getSubscriber(subscriber.getEmail());
			if (s != null)
				deleteSubscriber(subscriber.getEmail());
		} catch (Exception e) {
		}

		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(subscriber);
			return subscriber.getEmail();
		});
	}

	/**
	 * Delete the {@code Subscriber} with {@code email} from the database.
	 *
	 * @param email
	 *            the email address of the {@code Subscriber} who should be deleted
	 * @return the email address of the deleted subscriber
	 */
	public Serializable deleteSubscriber(String email) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			Subscriber subscriber = entityManager.find(Subscriber.class, email);
			if (subscriber == null) {
				throw new HibernateException("No subscriber with email: " + email);
			}
			entityManager.remove(subscriber);
			return subscriber.getEmail();
		});
	}

	/**
	 * Set the confirmation flag for the {@code Subscriber} with the specified
	 * {@code email}.
	 *
	 * @param email
	 *            the email address of the subscriber to be confirmed
	 */
	public void confirmSubscriber(String email) throws AlbinaException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			Subscriber subscriber = getSubscriber(email);
			subscriber.setConfirmed(true);
			entityManager.merge(subscriber);
			return null;
		});
	}

	/**
	 * Return all subscribers for the language {@code lang} and the specified
	 * {@code regions}.
	 *
	 * @param lang
	 *            the returned subscribers have to subscribed for this language
	 * @param regions
	 *            the returned subscribers have to be subscribed for at least one of
	 *            these regions
	 * @return all subscribers for the language {@code lang} and the specified
	 *         {@code regions}
	 */
	@SuppressWarnings("unchecked")
	public List<Subscriber> getSubscribers(LanguageCode lang, List<String> regions) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
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

			return results;
		});
	}

	/**
	 * Return the email address of all subscribers for the language {@code lang} and
	 * the specified {@code regions}.
	 *
	 * @param lang
	 *            the returned subscribers have to subscribed for this language
	 * @param regions
	 *            the returned subscribers have to be subscribed for at least one of
	 *            these regions
	 * @return the email address of all subscribers for the language {@code lang}
	 *         and the specified {@code regions}
	 */
	public List<String> getSubscriberEmails(LanguageCode lang, List<String> regions) {
		List<String> result = new ArrayList<String>();
		List<Subscriber> subscribers = getSubscribers(lang, regions);

		for (Subscriber subscriber : subscribers)
			result.add(subscriber.getEmail());

		return result;
	}

	/**
	 * Initialize all fields of the {@code subscriber} to be able to access it after
	 * the DB transaction was closed.
	 *
	 * @param subscriber
	 *            the subscriber that should be initialized
	 */
	private void initializeSubscriber(Subscriber subscriber) {
		Hibernate.initialize(subscriber.getEmail());
		Hibernate.initialize(subscriber.getConfirmed());
		Hibernate.initialize(subscriber.getLanguage());
		Hibernate.initialize(subscriber.getPdfAttachment());
		Hibernate.initialize(subscriber.getRegions());
	}

	/**
	 * Create a subscriber on rapidmail.
	 *
	 * @param subscriber
	 *            the subscriber that should be created
	 * @throws AlbinaException
	 * @throws KeyManagementException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws Exception
	 */
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

			ctRm.createRecipient(rc.getRapidMailConfig(), recipient, null, subscriber.getLanguage());
		}
	}
}

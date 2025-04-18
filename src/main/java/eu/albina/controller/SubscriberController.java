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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Subscriber;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(SubscriberController.class);

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
		return HibernateUtil.getInstance().run(entityManager -> {
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
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			Subscriber existing = entityManager.find(Subscriber.class, subscriber.getEmail());
			if (existing != null) {
				logger.info("Removing existing subscriber {}", existing.getEmail());
				entityManager.remove(existing);
			}
			logger.info("Creating subscriber {}", subscriber.getEmail());
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
			logger.info("Removing existing subscriber {}", subscriber.getEmail());
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
			Subscriber subscriber = entityManager.find(Subscriber.class, email);
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
	 * @param regionIds
	 *            the returned subscribers have to be subscribed for at least one of
	 *            these regions
	 * @return all subscribers for the language {@code lang} and the specified
	 *         {@code regions}
	 */
	public List<Subscriber> getSubscribers(LanguageCode lang, List<String> regionIds) {
		return HibernateUtil.getInstance().run(entityManager -> {
			List<Subscriber> subscribers = entityManager.createQuery(HibernateUtil.queryGetSubscribersForLanguage, Subscriber.class)
					.setParameter("language", lang).getResultList();
			List<Subscriber> results = subscribers.stream()
				.filter(subscriber -> regionIds.stream().anyMatch(regionId -> subscriber.affectsRegion(RegionController.getInstance().getRegion(regionId))))
				.collect(Collectors.toList());

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
		List<Subscriber> subscribers = getSubscribers(lang, regions);

		return subscribers.stream().map(Subscriber::getEmail).collect(Collectors.toList());
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

}

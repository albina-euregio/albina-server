package eu.albina.controller;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Subscriber;
import eu.albina.util.EmailUtil;
import eu.albina.util.HibernateUtil;

/**
 * Controller for subscribers.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class SubscriberController {
	private static Logger logger = LoggerFactory.getLogger(SubscriberController.class);
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
			throws HibernateException, URISyntaxException, IOException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(subscriber);
			transaction.commit();

			EmailUtil.getInstance().sendConfirmationEmail(subscriber);

			return subscriber.getEmail();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw he;
		} catch (IOException e) {
			logger.error("Confirmation email could not be send!");
			throw e;
		} catch (URISyntaxException e) {
			logger.error("Confirmation email could not be send!");
			throw e;
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
}
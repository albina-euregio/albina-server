package eu.albina.controller;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.exception.AlbinaException;
import eu.albina.model.News;

/**
 * Controller for news.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class NewsController extends AlbinaController {

	private static Logger logger = LoggerFactory.getLogger(NewsController.class);

	private static NewsController instance = null;

	private NewsController() {
	}

	public static NewsController getInstance() {
		if (instance == null) {
			instance = new NewsController();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public List<News> getNews(DateTime startDate, DateTime endDate) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(News.class);

			if (startDate != null) {
				criteria.add(Restrictions.ge("dateTime", startDate));
			}
			if (endDate != null) {
				criteria.add(Restrictions.le("dateTime", endDate));
			}
			List<News> news = criteria.list();

			for (News entry : news) {
				Hibernate.initialize(entry.getTitle());
				Hibernate.initialize(entry.getContent());
			}

			transaction.commit();
			return news;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public Serializable saveNews(News news) throws AlbinaException {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Serializable newsId = session.save(news);
			transaction.commit();
			return newsId;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}
}

package eu.albina.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import eu.albina.exception.AlbinaException;
import eu.albina.model.News;
import eu.albina.model.Text;
import eu.albina.util.HibernateUtil;

/**
 * Controller for news.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class NewsController {

	// private static Logger logger =
	// LoggerFactory.getLogger(NewsController.class);

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
		Session session = HibernateUtil.getSessionFactory().openSession();
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
		Session session = HibernateUtil.getSessionFactory().openSession();
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

	@SuppressWarnings("deprecation")
	public List<News> findNews(String searchString) throws AlbinaException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(News.class);

			@SuppressWarnings("unchecked")
			List<News> news = criteria.list();
			List<News> results = new ArrayList<News>();
			boolean hit = false;

			for (News entry : news) {
				Hibernate.initialize(entry.getTitle());
				Hibernate.initialize(entry.getContent());
				for (Text title : entry.getTitle().getTexts()) {
					if (title.getText().contains(searchString)) {
						results.add(entry);
						hit = true;
						break;
					}
				}
				if (!hit)
					for (Text content : entry.getContent().getTexts()) {
						if (content.getText().contains(searchString)) {
							results.add(entry);
							break;
						}
					}
				hit = false;
			}

			transaction.commit();
			return results;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}
}

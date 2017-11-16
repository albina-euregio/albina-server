package eu.albina.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.joda.time.DateTime;

import eu.albina.exception.AlbinaException;
import eu.albina.model.News;
import eu.albina.model.Text;
import eu.albina.model.enumerations.NewsStatus;
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

	/**
	 * Retrieve a news from the database by ID.
	 * 
	 * @param newsId
	 *            The ID of the desired news.
	 * @return The news with the given ID.
	 * @throws AlbinaException
	 */
	public News getNews(String newsId) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			News news = entityManager.find(News.class, newsId);
			if (news == null) {
				transaction.rollback();
				throw new AlbinaException("No news with ID: " + newsId);
			}
			transaction.commit();
			Hibernate.initialize(news.getTitle());
			Hibernate.initialize(news.getContent());
			return news;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<News> getNews(DateTime startDate, DateTime endDate) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<News> news = null;
			if (startDate != null && endDate != null)
				news = entityManager.createQuery(HibernateUtil.queryGetNewsStartEnd)
						.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();
			else if (startDate != null)
				news = entityManager.createQuery(HibernateUtil.queryGetNewsStart).setParameter("startDate", startDate)
						.getResultList();
			else if (endDate != null)
				news = entityManager.createQuery(HibernateUtil.queryGetNewsStart).setParameter("endDate", endDate)
						.getResultList();
			else
				news = entityManager.createQuery(HibernateUtil.queryGetNews).getResultList();

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
			entityManager.close();
		}
	}

	public Serializable saveNews(News news) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(news);
			transaction.commit();
			return news.getId();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Serializable updateNews(String newsId, News news) throws AlbinaException {
		deleteNews(newsId);
		return saveNews(news);
	}

	@SuppressWarnings("unchecked")
	public List<News> findNews(String searchString) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<News> news = entityManager.createQuery(HibernateUtil.queryGetNews).getResultList();
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
			entityManager.close();
		}
	}

	public void deleteNews(String newsId) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			News news = entityManager.find(News.class, newsId);
			if (news == null) {
				transaction.rollback();
				throw new AlbinaException("No news with ID: " + newsId);
			} else if (news.getStatus() == NewsStatus.published) {
				transaction.rollback();
				throw new AlbinaException("News already published!");
			}
			transaction.commit();
			transaction.begin();
			entityManager.remove(news);
			transaction.commit();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public void publishNews(String newsId) throws AlbinaException {
		News news = getNews(newsId);
		news.setStatus(NewsStatus.published);
		updateNews(newsId, news);
	}
}

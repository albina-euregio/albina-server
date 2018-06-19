package eu.albina.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.HibernateException;
import org.joda.time.DateTime;

import eu.albina.exception.AlbinaException;
import eu.albina.model.ChatMessage;
import eu.albina.model.User;
import eu.albina.util.HibernateUtil;

/**
 * Controller for chat.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class ChatController {

	// private static Logger logger =
	// LoggerFactory.getLogger(ChatController.class);

	private static ChatController instance = null;
	private List<User> activeUsers;

	private ChatController() {
		activeUsers = new ArrayList<User>();
	}

	public static ChatController getInstance() {
		if (instance == null) {
			instance = new ChatController();
		}
		return instance;
	}

	public List<User> getActiveUsers() {
		return activeUsers;
	}

	@SuppressWarnings("unchecked")
	public List<ChatMessage> getChatMessages(DateTime date) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			List<ChatMessage> chatMessages = null;
			if (date != null)
				chatMessages = entityManager.createQuery(HibernateUtil.queryGetChatMessagesDate)
						.setParameter("date", date).getResultList();
			else
				chatMessages = entityManager.createQuery(HibernateUtil.queryGetChatMessages).getResultList();

			transaction.commit();
			return chatMessages;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public Serializable saveChatMessage(ChatMessage chatMessage) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			entityManager.persist(chatMessage);
			transaction.commit();
			return chatMessage.getId();
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

	public void addActiveUser(User user) throws AlbinaException {
		boolean found = false;
		for (User u : activeUsers) {
			if (u.getEmail().equals(user.getEmail())) {
				found = true;
				break;
			}
		}
		if (!found)
			activeUsers.add(user);
	}

	public void deleteActiveUser(User user) throws AlbinaException {
		List<User> remove = new ArrayList<User>();
		for (User u : activeUsers) {
			if (u.getEmail().equals(user.getEmail()))
				remove.add(u);
		}
		for (User u2 : remove)
			activeUsers.remove(u2);
	}
}

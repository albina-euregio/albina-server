package eu.albina.controller;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;

import eu.albina.exception.AlbinaException;
import eu.albina.model.ChatMessage;
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

	private ChatController() {
	}

	public static ChatController getInstance() {
		if (instance == null) {
			instance = new ChatController();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public List<ChatMessage> getChatMessages(DateTime date) throws AlbinaException {
		Session session = HibernateUtil.getInstance().getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			List<ChatMessage> chatMessages = null;
			if (date != null)
				chatMessages = session.createQuery(HibernateUtil.queryGetChatMessagesDate).setParameter("date", date)
						.list();
			else
				chatMessages = session.createQuery(HibernateUtil.queryGetChatMessages).list();

			transaction.commit();
			return chatMessages;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}

	public Serializable saveChatMessage(ChatMessage chatMessage) throws AlbinaException {
		Session session = HibernateUtil.getInstance().getSessionFactory().openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Serializable chatMessageId = session.save(chatMessage);
			transaction.commit();
			return chatMessageId;
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			session.close();
		}
	}
}

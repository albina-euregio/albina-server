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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
	private final List<User> activeUsers;

	/**
	 * Private constructor.
	 */
	private ChatController() {
		activeUsers = new ArrayList<User>();
	}

	/**
	 * Returns the {@code ChatController} object associated with the current Java
	 * application.
	 *
	 * @return the {@code ChatController} object associated with the current Java
	 *         application.
	 */
	public static ChatController getInstance() {
		if (instance == null) {
			instance = new ChatController();
		}
		return instance;
	}

	/**
	 * Return all currently active users.
	 *
	 * @return all currently active users
	 */
	public List<User> getActiveUsers() {
		return activeUsers;
	}

	/**
	 * Return all chat messages starting from a specific {@code date}.
	 *
	 * @param date
	 *            chat messages newer than this date will be returned
	 * @return all chat messages starting from a specific {@code date}
	 */
	@SuppressWarnings("unchecked")
	public List<ChatMessage> getChatMessages(DateTime date) throws AlbinaException {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		List<ChatMessage> chatMessages = null;
		if (date != null)
			chatMessages = entityManager.createQuery(HibernateUtil.queryGetChatMessagesDate).setParameter("date", date)
					.getResultList();
		else
			chatMessages = entityManager.createQuery(HibernateUtil.queryGetChatMessages).getResultList();

		transaction.commit();
		entityManager.close();
		return chatMessages;
	}

	/**
	 * Save a chat message.
	 *
	 * @param chatMessage
	 *            the chat message to be saved
	 * @return the id of the saved chat message
	 */
	public Serializable saveChatMessage(ChatMessage chatMessage) {
		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(chatMessage);
		transaction.commit();
		entityManager.close();
		return chatMessage.getId();
	}

	/**
	 * Add an active user.
	 *
	 * @param user
	 *            the user do be added
	 */
	public synchronized void addActiveUser(User user) {
		boolean found = activeUsers.stream().anyMatch(u -> u.getEmail().equals(user.getEmail()));
		if (!found)
			activeUsers.add(user);
	}

	/**
	 * Delete an active user.
	 *
	 * @param user
	 *            the user to be deleted
	 */
	public void deleteActiveUser(User user) {
		activeUsers.removeIf(u -> u.getEmail().equals(user.getEmail()));
	}
}

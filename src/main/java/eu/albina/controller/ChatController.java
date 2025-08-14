// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import eu.albina.model.ChatMessage;
import eu.albina.model.User;
import eu.albina.util.AlbinaUtil;
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
	public List<ChatMessage> getChatMessages(Instant date) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<ChatMessage> chatMessages = null;
			if (date != null)
				chatMessages = entityManager.createQuery(HibernateUtil.queryGetChatMessagesDate, ChatMessage.class)
						.setParameter("date", AlbinaUtil.getZonedDateTimeUtc(date))
						.getResultList();
			else
				chatMessages = entityManager.createQuery(HibernateUtil.queryGetChatMessages, ChatMessage.class).getResultList();

			return chatMessages;
		});
	}

	/**
	 * Save a chat message.
	 *
	 * @param chatMessage
	 *            the chat message to be saved
	 * @return the id of the saved chat message
	 */
	public Serializable saveChatMessage(ChatMessage chatMessage) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(chatMessage);
			return chatMessage.getId();
		});
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

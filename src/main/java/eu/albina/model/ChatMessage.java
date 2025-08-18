// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages")
public class ChatMessage extends AbstractPersistentObject {

	@Column(name = "TEXT", length = 191)
	private String text;

	@Column(name = "USERNAME", length = 191)
	private String username;

	@Column(name = "DATETIME")
	private ZonedDateTime dateTime;

	@Column(name = "CHAT_ID")
	private int chatId;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ZonedDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(ZonedDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public int getChatId() {
		return this.chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

}

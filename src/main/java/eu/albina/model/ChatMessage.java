// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.github.openjson.JSONObject;

@Entity
@Table(name = "chat_messages")
public class ChatMessage extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "TEXT", length = 191)
	private String text;

	@Column(name = "USERNAME", length = 191)
	private String username;

	@Column(name = "DATETIME")
	private ZonedDateTime dateTime;

	@Column(name = "CHAT_ID")
	private int chatId;

	/**
	 * Standard constructor for chat message.
	 */
	public ChatMessage() {
	}

	public ChatMessage(JSONObject json) {
		if (json.has("username"))
			this.username = json.getString("username");

		if (json.has("text"))
			this.text = json.getString("text");

		if (json.has("time"))
			this.dateTime = ZonedDateTime.parse(json.getString("time"));

		if (json.has("chatId"))
			this.chatId = json.getInt("chatId");
	}

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

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (text != null)
			json.put("text", text);
		if (username != null)
			json.put("username", username);
		if (dateTime != null)
			json.put("time", DateTimeFormatter.ISO_INSTANT.format(dateTime));
		if (chatId > -1)
			json.put("chatId", chatId);

		return json;
	}

}

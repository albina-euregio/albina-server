package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.json.JSONObject;

import eu.albina.util.GlobalVariables;

@Entity
@Table(name = "chat_message")
public class ChatMessage extends AbstractPersistentObject implements AvalancheInformationObject {

	@Column(name = "TEXT")
	private String text;

	@Column(name = "USERNAME")
	private String username;

	@Column(name = "DATETIME")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime dateTime;

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
			this.dateTime = new org.joda.time.DateTime(json.getString("time"));

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

	public org.joda.time.DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(org.joda.time.DateTime dateTime) {
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
			json.put("time", dateTime.toString(GlobalVariables.formatterDateTime));
		if (chatId > -1)
			json.put("chatId", chatId);

		return json;
	}

}

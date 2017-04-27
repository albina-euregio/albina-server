package eu.albina.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.model.enumerations.Status;
import eu.albina.util.GlobalVariables;

@Entity
@Table(name = "NEWS")
public class News extends AbstractPersistentObject implements AvalancheInformationObject {

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "TITLE_ID")
	private Texts title;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTENT_ID")
	private Texts content;

	@Column(name = "DATETIME")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime dateTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	private Status status;

	/**
	 * Standard constructor for an author.
	 */
	public News() {
	}

	public News(JSONObject json) {
		if (json.has("title") && !json.isNull("title")) {
			this.title = new Texts(json.getJSONArray("title"));
		}
		if (json.has("content") && !json.isNull("content")) {
			this.content = new Texts(json.getJSONArray("content"));
		}
		if (json.has("date") && !json.isNull("date"))
			dateTime = new org.joda.time.DateTime(json.getString("date"));
		else
			dateTime = new org.joda.time.DateTime();
		if (json.has("status") && !json.isNull("status"))
			status = Status.fromString(json.getString("status"));
	}

	public Texts getTitle() {
		return title;
	}

	public void setTitle(Texts title) {
		this.title = title;
	}

	public Texts getContent() {
		return content;
	}

	public void setContent(Texts content) {
		this.content = content;
	}

	public org.joda.time.DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(org.joda.time.DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (id != null && id != "")
			json.put("id", id);
		if (title != null) {
			JSONArray array = title.toJSONArray();
			json.put("title", array);
		}
		if (content != null) {
			JSONArray array = content.toJSONArray();
			json.put("content", array);
		}
		if (dateTime != null)
			json.put("date", dateTime.toString(GlobalVariables.formatterDateTime));

		json.put("status", status);

		return json;
	}

}

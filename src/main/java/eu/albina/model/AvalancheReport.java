package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.UserController;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;

/**
 * This class holds all information about one avalanche report.
 * 
 * @author Norbert Lanzanasto
 *
 */
@Audited
@Entity
@Table(name = "AVALANCHE_REPORTS")
public class AvalancheReport extends AbstractPersistentObject implements AvalancheInformationObject {

	/** Information about the author of the avalanche bulletin */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(name = "REGION")
	private String region;

	@Column(name = "DATE")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime date;

	@Column(name = "TIMESTAMP")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private org.joda.time.DateTime timestamp;

	@Column(name = "STATUS")
	private BulletinStatus status;

	@Column(name = "REVISION")
	private int revision;

	/**
	 * Standard constructor for an avalanche report.
	 */
	public AvalancheReport() {
	}

	/**
	 * Custom constructor that creates an avalanche bulletin object from JSON input.
	 * 
	 * @param json
	 *            JSONObject holding information about an avalanche bulletin.
	 */
	public AvalancheReport(JSONObject json, String username) {
		this();

		if (username != null) {
			try {
				this.user = UserController.getInstance().getUser(username);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (json.has("region"))
			this.region = json.getString("region");

		if (json.has("date"))
			this.date = new org.joda.time.DateTime(json.getString("date"));

		if (json.has("timestamp"))
			this.timestamp = new org.joda.time.DateTime(json.getString("timestamp"));

		if (json.has("status"))
			this.status = BulletinStatus.fromString(json.getString("status"));

		if (json.has("revision"))
			this.revision = json.getInt("revision");
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public org.joda.time.DateTime getDate() {
		return date;
	}

	public void setDate(org.joda.time.DateTime date) {
		this.date = date;
	}

	public org.joda.time.DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(org.joda.time.DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public BulletinStatus getStatus() {
		return status;
	}

	public void setStatus(BulletinStatus status) {
		this.status = status;
	}

	public Number getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		if (id != null && id != "")
			json.put("id", id);

		if (user != null && user.getName() != null && user.getName() != "")
			json.put("user", user.getName());

		if (region != null && region != "")
			json.put("region", region);

		if (date != null)
			json.put("date", date.toString(GlobalVariables.formatterDateTime));

		if (timestamp != null)
			json.put("timestamp", timestamp.toString(GlobalVariables.formatterDateTime));

		if (status != null)
			json.put("status", status.toString());

		if (revision > 0)
			json.put("revision", revision);

		return json;
	}

	public Element toCAAML(Document doc, LanguageCode languageCode) {
		// TODO implement
		return null;
	}
}

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
package eu.albina.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
@Table(name = "avalanche_reports")
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

	@Column(name = "CAAML_CREATED")
	private boolean caamlCreated;

	@Column(name = "PDF_CREATED")
	private boolean pdfCreated;

	@Column(name = "HTML_CREATED")
	private boolean htmlCreated;

	@Column(name = "STATIC_WIDGET_CREATED")
	private boolean staticWidgetCreated;

	@Column(name = "MAP_CREATED")
	private boolean mapCreated;

	@Column(name = "EMAIL_CREATED")
	private boolean emailCreated;

	@Column(name = "WHATSAPP_SENT")
	private boolean whatsappSent;

	@Column(name = "TELEGRAM_SENT")
	private boolean telegramSent;

	@Lob
	@Column(name = "JSON_STRING")
	private String jsonString;

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

		if (json.has("caamlCreated"))
			this.caamlCreated = json.getBoolean("caamlCreated");
		if (json.has("pdfCreated"))
			this.pdfCreated = json.getBoolean("pdfCreated");
		if (json.has("htmlCreated"))
			this.htmlCreated = json.getBoolean("htmlCreated");
		if (json.has("staticWidgetCreated"))
			this.staticWidgetCreated = json.getBoolean("staticWidgetCreated");
		if (json.has("emailCreated"))
			this.emailCreated = json.getBoolean("emailCreated");
		if (json.has("mapCreated"))
			this.mapCreated = json.getBoolean("mapCreated");
		if (json.has("whatsappSent"))
			this.whatsappSent = json.getBoolean("whatsappSent");
		if (json.has("telegramSent"))
			this.telegramSent = json.getBoolean("telegramSent");

		if (json.has("jsonString"))
			this.jsonString = json.getString("jsonString");
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

	public boolean isCaamlCreated() {
		return caamlCreated;
	}

	public void setCaamlCreated(boolean caaml) {
		this.caamlCreated = caaml;
	}

	public boolean isPdfCreated() {
		return pdfCreated;
	}

	public void setPdfCreated(boolean pdf) {
		this.pdfCreated = pdf;
	}

	public boolean isHtmlCreated() {
		return htmlCreated;
	}

	public void setHtmlCreated(boolean html) {
		this.htmlCreated = html;
	}

	public boolean isStaticWidgetCreated() {
		return staticWidgetCreated;
	}

	public void setStaticWidgetCreated(boolean staticWidget) {
		this.staticWidgetCreated = staticWidget;
	}

	public boolean isMapCreated() {
		return mapCreated;
	}

	public void setMapCreated(boolean mapCreated) {
		this.mapCreated = mapCreated;
	}

	public boolean isEmailCreated() {
		return emailCreated;
	}

	public void setEmailCreated(boolean email) {
		this.emailCreated = email;
	}

	public boolean isWhatsappSent() {
		return whatsappSent;
	}

	public void setWhatsappSent(boolean whatsapp) {
		this.whatsappSent = whatsapp;
	}

	public boolean isTelegramSent() {
		return telegramSent;
	}

	public void setTelegramSent(boolean telegram) {
		this.telegramSent = telegram;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
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

		if (caamlCreated)
			json.put("caamlCreated", true);
		else
			json.put("caamlCreated", false);

		if (pdfCreated)
			json.put("pdfCreated", true);
		else
			json.put("pdfCreated", false);

		if (htmlCreated)
			json.put("htmlCreated", true);
		else
			json.put("htmlCreated", false);

		if (staticWidgetCreated)
			json.put("staticWidgetCreated", true);
		else
			json.put("staticWidgetCreated", false);

		if (mapCreated)
			json.put("mapCreated", true);
		else
			json.put("mapCreated", false);

		if (emailCreated)
			json.put("emailCreated", true);
		else
			json.put("emailCreated", false);

		if (whatsappSent)
			json.put("whatsappSent", true);
		else
			json.put("whatsappSent", false);

		if (telegramSent)
			json.put("telegramSent", true);
		else
			json.put("telegramSent", false);

		if (jsonString != null)
			json.put("jsonString", jsonString);

		return json;
	}

	public Element toCAAML(Document doc, LanguageCode languageCode) {
		return null;
	}
}

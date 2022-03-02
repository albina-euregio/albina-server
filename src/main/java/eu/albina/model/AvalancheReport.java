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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.github.openjson.JSONObject;
import com.google.common.base.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;

/**
 * This class holds all information about one avalanche report.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "avalanche_reports", indexes = {
		@Index(name = "avalanche_reports_DATE_IDX", columnList = "DATE"),
})
public class AvalancheReport extends AbstractPersistentObject implements AvalancheInformationObject {

	/** Information about the author of the avalanche bulletin */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REGION_ID")
	private Region region;

	@Column(name = "DATE")
	private ZonedDateTime date;

	@Column(name = "TIMESTAMP")
	private ZonedDateTime timestamp;

	@Column(name = "STATUS")
	private BulletinStatus status;

	@Column(name = "CAAML_CREATED")
	private boolean caamlCreated;

	@Column(name = "PDF_CREATED")
	private boolean pdfCreated;

	@Column(name = "HTML_CREATED")
	private boolean htmlCreated;

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(ZonedDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public BulletinStatus getStatus() {
		return status;
	}

	public void setStatus(BulletinStatus status) {
		this.status = status;
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

		if (!Strings.isNullOrEmpty(id))
			json.put("id", id);

		if (user != null && !Strings.isNullOrEmpty(user.getName()))
			json.put("user", user.getName());

		if (region != null && !Strings.isNullOrEmpty(region.getId()))
			json.put("region", region.getId());

		if (date != null)
			json.put("date", DateTimeFormatter.ISO_INSTANT.format(date));

		if (timestamp != null)
			json.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(timestamp));

		if (status != null)
			json.put("status", status.toString());

		json.put("caamlCreated", caamlCreated);
		json.put("pdfCreated", pdfCreated);
		json.put("htmlCreated", htmlCreated);
		json.put("mapCreated", mapCreated);
		json.put("emailCreated", emailCreated);
		json.put("whatsappSent", whatsappSent);
		json.put("telegramSent", telegramSent);

		if (jsonString != null)
			json.put("jsonString", jsonString);

		return json;
	}

	public Element toCAAML(Document doc, LanguageCode languageCode) {
		return null;
	}
}

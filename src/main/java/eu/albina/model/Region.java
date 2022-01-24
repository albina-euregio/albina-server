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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

/**
 * This class holds all information about one region.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "regions")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Region.class)
public class Region implements AvalancheInformationObject {

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "PUBLISH_BULLETINS")
	private boolean publishBulletins;

	@Column(name = "PUBLISH_BLOGS")
	private boolean publishBlogs;

	@Column(name = "SEND_EMAILS")
	private boolean sendEmails;

	@Column(name = "SEND_TELEGRAM_MESSAGES")
	private boolean sendTelegramMessages;

	@Column(name = "SEND_PUSH_NOTIFICATIONS")
	private boolean sendPushNotifications;

	@Column(name = "EXTERNAL")
	private boolean external;

	@Column(name = "EXTERNAL_API_URL")
	private String externalApiUrl;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
	}

	public Region(JSONObject json) {
		this();
		if (json.has("id") && !json.isNull("id"))
			this.id = json.getString("id");
		if (json.has("publishBulletins") && !json.isNull("publishBulletins"))
			this.publishBulletins = json.getBoolean("publishBulletins");
		if (json.has("publishBlogs") && !json.isNull("publishBlogs"))
			this.publishBlogs = json.getBoolean("publishBlogs");
		if (json.has("sendEmails") && !json.isNull("sendEmails"))
			this.sendEmails = json.getBoolean("sendEmails");
		if (json.has("sendTelegramMessages") && !json.isNull("sendTelegramMessages"))
			this.sendTelegramMessages = json.getBoolean("sendTelegramMessages");
		if (json.has("sendPushNotifications") && !json.isNull("sendPushNotifications"))
			this.sendPushNotifications = json.getBoolean("sendPushNotifications");
		if (json.has("external") && !json.isNull("external"))
			this.external = json.getBoolean("external");
		if (json.has("externalApiUrl") && !json.isNull("externalApiUrl"))
			this.externalApiUrl = json.getString("externalApiUrl");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isPublishBulletins() {
		return publishBulletins;
	}

	public void setPublishBulletins(boolean publishBulletins) {
		this.publishBulletins = publishBulletins;
	}

	public boolean isPublishBlogs() {
		return publishBlogs;
	}

	public void setPublishBlogs(boolean publishBlogs) {
		this.publishBlogs = publishBlogs;
	}

	public boolean isSendEmails() {
		return sendEmails;
	}

	public void setSendEmails(boolean sendEmails) {
		this.sendEmails = sendEmails;
	}

	public boolean isSendTelegramMessages() {
		return sendTelegramMessages;
	}

	public void setSendTelegramMessages(boolean sendTelegramMessages) {
		this.sendTelegramMessages = sendTelegramMessages;
	}

	public boolean isSendPushNotifications() {
		return sendPushNotifications;
	}

	public void setSendPushNotifications(boolean sendPushNotifications) {
		this.sendPushNotifications = sendPushNotifications;
	}

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public String getExternalApiUrl() {
		return externalApiUrl;
	}

	public void setExternalApiUrl(String externalApiUrl) {
		this.externalApiUrl = externalApiUrl;
	}

	public Element toCAAML(Document doc) {
		Element region = doc.createElement("Region");
		region.setAttribute("gml:id", getId());
		Element regionSubType = doc.createElement("regionSubType");
		region.appendChild(regionSubType);
		return region;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject feature = new JSONObject();

		feature.put("type", "Feature");
		JSONObject featureProperties = new JSONObject();
		featureProperties.put("id", getId());
		featureProperties.put("publishBulletins", isPublishBulletins());
		featureProperties.put("publishBlogs", isPublishBlogs());
		featureProperties.put("sendEmails", isSendEmails());
		featureProperties.put("sendTelegramMessages", isSendTelegramMessages());
		featureProperties.put("sendPushNotifications", isSendPushNotifications());
		featureProperties.put("external", isExternal());
		featureProperties.put("externalApiUrl", getExternalApiUrl());
		feature.put("properties", featureProperties);

		return feature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Region region = (Region) o;
		return Objects.equals(id, region.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public static Region readRegion(final URL resource) throws IOException {
		final String string = Resources.toString(resource, StandardCharsets.UTF_8);
		return new Region(new JSONObject(string));
	}

}

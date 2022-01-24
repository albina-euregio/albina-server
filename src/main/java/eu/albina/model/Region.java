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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

	@Column(name = "MICRO_REGIONS")
	private int microRegions;

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

	@Column(name = "EXTERNAL_INSTANCE")
	private boolean externalInstance;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVER_INSTANCE_ID")
	private ServerInstance serverInstance;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
	}

	public Region(JSONObject json) {
		this();
		if (json.has("id") && !json.isNull("id"))
			this.id = json.getString("id");
		if (json.has("microRegions") && !json.isNull("microRegions"))
			this.microRegions = json.getInt("microRegions");
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
		if (json.has("externalInstance") && !json.isNull("externalInstance"))
			this.externalInstance = json.getBoolean("external");
		if (json.has("serverInstance") && !json.isNull("serverInstance"))
			this.serverInstance = new ServerInstance(json.getJSONObject("serverInstance"));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getMicroRegions() {
		return microRegions;
	}

	public void setMicroRegions(int microRegions) {
		this.microRegions = microRegions;
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

	public boolean isExternalInstance() {
		return externalInstance;
	}

	public void setExternalInstance(boolean externalInstance) {
		this.externalInstance = externalInstance;
	}

	public ServerInstance getServerInstance() {
		return serverInstance;
	}

	public void setServerInstance(ServerInstance serverInstance) {
		this.serverInstance = serverInstance;
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
		JSONObject json = new JSONObject();

		json.put("id", getId());
		json.put("microRegions", getMicroRegions());
		json.put("publishBulletins", isPublishBulletins());
		json.put("publishBlogs", isPublishBlogs());
		json.put("sendEmails", isSendEmails());
		json.put("sendTelegramMessages", isSendTelegramMessages());
		json.put("sendPushNotifications", isSendPushNotifications());
		json.put("externalInstance", isExternalInstance());
		json.put("serverInstance", getServerInstance().toJSON());

		return json;
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

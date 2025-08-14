// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.JsonUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import eu.albina.model.enumerations.BulletinStatus;
import org.slf4j.LoggerFactory;

/**
 * This class holds all information about one avalanche report.
 *
 * @author Norbert Lanzanasto
 */
@Entity
@Table(name = "avalanche_reports", indexes = {
	@Index(name = "avalanche_reports_DATE_IDX", columnList = "DATE"),
})
public class AvalancheReport extends AbstractPersistentObject implements HasValidityDate, HasPublicationDate {

	/**
	 * Information about the author of the avalanche bulletin
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	@JsonSerialize(using = User.UserNameSerializer.class)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REGION_ID")
	@JsonSerialize(using = Region.RegionSerializer.class)
	@JsonDeserialize(using = Region.RegionDeserializer.class)
	private Region region;

	@Column(name = "DATE")
	private ZonedDateTime date;

	@Column(name = "TIMESTAMP")
	private ZonedDateTime timestamp;

	@Column(name = "STATUS")
	private BulletinStatus status;

	@Column(name = "CAAML_V5_CREATED")
	private boolean caamlV5Created;

	@Column(name = "CAAML_V6_CREATED")
	private boolean caamlV6Created;

	@Column(name = "JSON_CREATED")
	private boolean jsonCreated;

	@Column(name = "PDF_CREATED")
	private boolean pdfCreated;

	@Column(name = "HTML_CREATED")
	private boolean htmlCreated;

	@Column(name = "MAP_CREATED")
	private boolean mapCreated;

	@Column(name = "EMAIL_CREATED")
	private boolean emailCreated;

	@Column(name = "TELEGRAM_SENT")
	private boolean telegramSent;

	@Column(name = "WHATSAPP_SENT")
	private boolean whatsAppSent;

	@Column(name = "PUSH_SENT")
	private boolean pushSent;

	@Column(name = "MEDIA_FILE_UPLOADED")
	private boolean mediaFileUploaded;

	@Lob
	@Column(name = "JSON_STRING")
	@JsonIgnore
	private String jsonString;

	@Transient
	private List<AvalancheBulletin> bulletins;

	@Transient
	private List<AvalancheBulletin> globalBulletins;

	@Transient
	private ServerInstance serverInstance;

	/**
	 * Standard constructor for an avalanche report.
	 */
	public AvalancheReport() {
	}

	public static AvalancheReport of(List<AvalancheBulletin> bulletins, Region region, ServerInstance serverInstance) {
		final AvalancheReport avalancheReport = new AvalancheReport();
		avalancheReport.setServerInstance(serverInstance);
		avalancheReport.setRegion(region);
		avalancheReport.setBulletins(bulletins); // after region
		return avalancheReport;
	}

	@JsonIgnore
	public List<AvalancheBulletin> getPublishedBulletins() {
		if (getStatus() != BulletinStatus.published && getStatus() != BulletinStatus.republished) {
			LoggerFactory.getLogger(AvalancheReport.class).warn("Report has not been published!");
			return List.of();
		}
		if (getJsonString() == null || getJsonString().isEmpty()) {
			LoggerFactory.getLogger(AvalancheReport.class).warn("JSON string empty: {}, {}", getDate(), getRegion());
			return List.of();
		}
		return Arrays.stream(JsonUtil.parseUsingJackson(getJsonString(), AvalancheBulletin[].class))
			// only add bulletins with published regions
			.filter(bulletin -> bulletin.getPublishedRegions() != null && !bulletin.getPublishedRegions().isEmpty())
			.collect(Collectors.toList());
	}

	public void createJsonFile() throws IOException {
		Path pdfDirectory = getPdfDirectory();
		Files.createDirectories(pdfDirectory);
		Path path = pdfDirectory.resolve(getRegion().getId() + ".json");
		if (getBulletins().isEmpty()) {
			return;
		}
		Region region = getRegion();
		Collection<AvalancheBulletin> bulletins = getBulletins().stream().map(b -> b.withRegionFilter(region)).collect(Collectors.toList());
		String jsonString = JsonUtil.writeValueUsingJackson(bulletins, JsonUtil.Views.Public.class);
		Files.writeString(path, jsonString, StandardCharsets.UTF_8);
	}

	public void setBulletins(List<AvalancheBulletin> bulletins) {
		setBulletins(bulletins, bulletins);
	}

	public void setBulletins(List<AvalancheBulletin> bulletins, List<AvalancheBulletin> globalBulletins) {
		Preconditions.checkArgument(Sets.difference(new HashSet<>(bulletins), new HashSet<>(globalBulletins)).isEmpty(),
			"bulletins must be subset of globalBulletins");
		this.bulletins = bulletins;
		this.globalBulletins = globalBulletins;
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

	public boolean isCaamlV5Created() {
		return caamlV5Created;
	}

	public void setCaamlV5Created(boolean caaml) {
		this.caamlV5Created = caaml;
	}

	public boolean isCaamlV6Created() {
		return caamlV6Created;
	}

	public void setCaamlV6Created(boolean caaml) {
		this.caamlV6Created = caaml;
	}

	public boolean isJsonCreated() {
		return jsonCreated;
	}

	public void setJsonCreated(boolean json) {
		this.jsonCreated = json;
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

	public boolean isTelegramSent() {
		return telegramSent;
	}

	public void setTelegramSent(boolean telegram) {
		this.telegramSent = telegram;
	}

	public boolean isWhatsAppSent() {
		return whatsAppSent;
	}

	public void setWhatsAppSent(boolean whatsapp) {
		this.whatsAppSent = whatsapp;
	}

	public boolean isPushSent() {
		return pushSent;
	}

	public void setPushSent(boolean push) {
		this.pushSent = push;
	}

	public boolean isMediaFileUploaded() {
		return mediaFileUploaded;
	}

	public void setMediaFileUploaded(boolean mediaFile) {
		this.mediaFileUploaded = mediaFile;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public List<AvalancheBulletin> getGlobalBulletins() {
		return globalBulletins;
	}

	public List<AvalancheBulletin> getBulletins() {
		return bulletins;
	}

	public ServerInstance getServerInstance() {
		return serverInstance;
	}

	public void setServerInstance(ServerInstance serverInstance) {
		this.serverInstance = serverInstance;
		if (this.bulletins == null) {
			return;
		}
		for (AvalancheBulletin bulletin : this.bulletins) {
			if (bulletin.getForenoon() != null) {
				bulletin.getForenoon().serverInstance(serverInstance);
			}
			if (bulletin.getAfternoon() != null) {
				bulletin.getAfternoon().serverInstance(serverInstance);
			}
		}
	}

	@JsonIgnore
	public Path getMapsPath() {
		return Paths.get(getServerInstance().getMapsPath(), getValidityDateString(), getPublicationTimeString());
	}

	@JsonIgnore
	public Path getPdfDirectory() {
		return Paths.get(getServerInstance().getPdfDirectory(), getValidityDateString(), getPublicationTimeString());
	}

	@JsonIgnore
	public Path getHtmlDirectory() {
		return Paths.get(getServerInstance().getHtmlDirectory(), getValidityDateString());
	}

	public String getGeneralHeadline(LanguageCode lang) {
		return !this.bulletins.isEmpty()
			? this.bulletins.get(0).getGeneralHeadlineCommentIn(lang)
			: "";
	}

	public boolean hasDaytimeDependency() {
		return getBulletins().stream().anyMatch(AvalancheBulletin::isHasDaytimeDependency);
	}


	@Override
	@JsonIgnore
	public LocalDate getValidityDate() {
		return globalBulletins.stream()
			.map(AvalancheBulletin::getValidityDate)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElseThrow();
	}

	@Override
	@JsonIgnore
	public ZonedDateTime getPublicationDate() {
		return globalBulletins.stream()
			.map(AvalancheBulletin::getPublicationDate)
			.filter(Objects::nonNull)
			.max(Comparator.naturalOrder())
			.orElse(null);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("region", region)
			.add("region.enabledLanguages", region.getEnabledLanguages())
			.add("date", date)
			.add("timestamp", timestamp)
			.add("bulletins", bulletins)
			.toString();
	}
}

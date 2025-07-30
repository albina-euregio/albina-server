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
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import eu.albina.util.JsonUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.albina.model.converter.LanguageCodeConverter;
import eu.albina.model.enumerations.LanguageCode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.model.enumerations.Position;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class Region {

	static class RegionSerializer extends JsonSerializer<Region> {
		@Override
		public void serialize(Region value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.getId());
		}
	}

	static class RegionDeserializer extends JsonDeserializer<Region> {
		@Override
		public Region deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return new Region(p.getValueAsString());
		}
	}

	@Id
	@Column(name = "ID", length = 191)
	private String id;

	@Column(name = "MICRO_REGIONS")
	private int microRegions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="region_hierarchy",
	 joinColumns=@JoinColumn(name="SUPER_REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="SUB_REGION_ID")
	)
	@JsonSerialize(contentUsing = RegionSerializer.class)
	@JsonDeserialize(contentUsing = RegionDeserializer.class)
	private Set<Region> subRegions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="region_hierarchy",
	 joinColumns=@JoinColumn(name="SUB_REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="SUPER_REGION_ID")
	)
	@JsonSerialize(contentUsing = RegionSerializer.class)
	@JsonDeserialize(contentUsing = RegionDeserializer.class)
	private Set<Region> superRegions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="region_neighbors",
	 joinColumns=@JoinColumn(name="REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="NEIGHBOR_REGION_ID")
	)
	@JsonSerialize(contentUsing = RegionSerializer.class)
	@JsonDeserialize(contentUsing = RegionDeserializer.class)
	private Set<Region> neighborRegions;

	@Column(name = "ENABLED_LANGUAGES", columnDefinition = "set('de', 'it', 'en', 'fr', 'es', 'ca', 'oc')")
	@Convert(converter = LanguageCodeConverter.class)
	private Set<LanguageCode> enabledLanguages;

	@Column(name = "TTS_LANGUAGES", columnDefinition = "set('de', 'it', 'en', 'fr', 'es', 'ca', 'oc')")
	@Convert(converter = LanguageCodeConverter.class)
	private Set<LanguageCode> ttsLanguages;

	@Column(name = "PUBLISH_BULLETINS")
	private boolean publishBulletins;

	@Column(name = "PUBLISH_BLOGS")
	private boolean publishBlogs;

	@Column(name = "CREATE_CAAML_V5")
	private boolean createCaamlV5;

	@Column(name = "CREATE_CAAML_V6")
	private boolean createCaamlV6;

	@Column(name = "CREATE_JSON")
	private boolean createJson;

	@Column(name = "CREATE_MAPS")
	private boolean createMaps;

	@Column(name = "CREATE_PDF")
	private boolean createPdf;

	@Column(name = "CREATE_SIMPLE_HTML")
	private boolean createSimpleHtml;

	@Column(name = "SEND_EMAILS")
	private boolean sendEmails;

	@Column(name = "SEND_TELEGRAM_MESSAGES")
	private boolean sendTelegramMessages;

	@Column(name = "SEND_WHATSAPP_MESSAGES")
	private boolean sendWhatsAppMessages;

	@Column(name = "SEND_PUSH_NOTIFICATIONS")
	private boolean sendPushNotifications;

	@Column(name = "ENABLE_MEDIA_FILE")
	private boolean enableMediaFile;

	@Column(name = "ENABLE_AVALANCHE_PROBLEM_CORNICES")
	private boolean enableAvalancheProblemCornices;

	@Column(name = "ENABLE_AVALANCHE_PROBLEM_NO_DISTINCT_AVALANCHE_PROBLEM")
	private boolean enableAvalancheProblemNoDistinctAvalancheProblem;

	@Column(name = "ENABLE_DANGER_SOURCES")
	private boolean enableDangerSources;

	@Column(name = "ENABLE_OBSERVATIONS")
	private boolean enableObservations;

	@Column(name = "ENABLE_MODELLING")
	private boolean enableModelling;

	@Column(name = "ENABLE_WEATHERBOX")
	private boolean enableWeatherbox;

	@Column(name = "ENABLE_EDITABLE_FIELDS")
	private boolean enableEditableFields;

	@Column(name = "SHOW_MATRIX")
	private boolean showMatrix;

	@Column(name = "ENABLE_STRATEGIC_MINDSET")
	private boolean enableStrategicMindset;

	@Column(name = "ENABLE_STRESS_LEVEL")
	private boolean enableStressLevel;

	@Column(name = "ENABLE_GENERAL_HEADLINE")
	private boolean enableGeneralHeadline;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVER_INSTANCE_ID")
	private ServerInstance serverInstance;

	@Column(name = "PDF_COLOR", length = 191)
	private String pdfColor;

	@Column(name = "EMAIL_COLOR", length = 191)
	private String emailColor;

	@Column(name = "PDF_MAP_Y_AM_PM")
	private int pdfMapYAmPm;

	@Column(name = "PDF_MAP_Y_FD")
	private int pdfMapYFd;

	@Column(name = "PDF_MAP_WIDTH_AM_PM")
	private int pdfMapWidthAmPm;

	@Column(name = "PDF_MAP_WIDTH_FD")
	private int pdfMapWidthFd;

	@Column(name = "PDF_MAP_HEIGHT")
	private int pdfMapHeight;

	@Column(name = "PDF_FOOTER_LOGO")
	private boolean pdfFooterLogo;

	@Column(name = "PDF_FOOTER_LOGO_COLOR_PATH", length = 191)
	private String pdfFooterLogoColorPath;

	@Column(name = "PDF_FOOTER_LOGO_BW_PATH", length = 191)
	private String pdfFooterLogoBwPath;

	@Column(name = "MAP_X_MAX")
	private int mapXmax;

	@Column(name = "MAP_X_MIN")
	private int mapXmin;

	@Column(name = "MAP_Y_MAX")
	private int mapYmax;

	@Column(name = "MAP_Y_MIN")
	private int mapYmin;

	@Column(name = "SIMPLE_HTML_TEMPLATE_NAME", length = 191)
	private String simpleHtmlTemplateName;

	@Column(name = "GEO_DATA_DIRECTORY", length = 191)
	private String geoDataDirectory;

	@Column(name = "MAP_LOGO_COLOR_PATH", length = 191)
	private String mapLogoColorPath;

	@Column(name = "MAP_LOGO_BW_PATH", length = 191)
	private String mapLogoBwPath;

	@Enumerated(EnumType.STRING)
	@Column(name = "MAP_LOGO_POSITION", length = 191)
	private Position mapLogoPosition;

	@Column(name = "MAP_CENTER_LAT", columnDefinition = "double")
	private double mapCenterLat;

	@Column(name = "MAP_CENTER_LNG", columnDefinition = "double")
	private double mapCenterLng;

	@Column(name = "IMAGE_COLORBAR_COLOR_PATH", length = 191)
	private String imageColorbarColorPath;

	@Column(name = "IMAGE_COLORBAR_BW_PATH", length = 191)
	private String imageColorbarBwPath;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
		this.superRegions = new HashSet<Region>();
		this.subRegions = new HashSet<Region>();
		this.neighborRegions = new HashSet<Region>();
		this.enabledLanguages = new HashSet<LanguageCode>();
		this.ttsLanguages = new HashSet<LanguageCode>();
	}

	public Region(String id) {
		super();
		this.id = id;
	}

	public Region(String id, String geoDataDirectory, int mapXmax, int mapXmin, int mapYmax, int mapYmin) {
		this.id = id;
		this.geoDataDirectory = geoDataDirectory;
		this.mapXmax = mapXmax;
		this.mapXmin = mapXmin;
		this.mapYmax = mapYmax;
		this.mapYmin = mapYmin;
	}

	public Region(String json, Function<String, Region> regionFunction) throws JsonProcessingException {
		this();

		// Use Jackson to populate all "normal" fields
		JsonUtil.ALBINA_OBJECT_MAPPER.readerForUpdating(this).readValue(json);

		// Handle region references manually
		JsonNode node = JsonUtil.ALBINA_OBJECT_MAPPER.readTree(json);
		BiConsumer<String, Set<Region>> extractRegionsFromJSON = (key, targetSet) -> {
			JsonNode arrayNode = node.get(key);
			if (arrayNode != null && arrayNode.isArray()) {
				for (JsonNode entryNode : arrayNode) {
					Region region = regionFunction.apply(entryNode.asText());
					if (region != null)
						targetSet.add(region);
				}
			}
		};
		extractRegionsFromJSON.accept("subRegions", this.subRegions);
		extractRegionsFromJSON.accept("superRegions", this.superRegions);
		extractRegionsFromJSON.accept("neighborRegions", this.neighborRegions);
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

	public Set<Region> getSubRegions() {
		return subRegions;
	}

	public void setSubRegions(Set<Region> subRegions) {
		this.subRegions = subRegions;
	}

	public void addSubRegion(Region subRegion) {
		this.subRegions.add(subRegion);
	}

	public Set<Region> getSuperRegions() {
		return superRegions;
	}

	public void setSuperRegions(Set<Region> superRegions) {
		this.superRegions = superRegions;
	}

	public void addSuperRegion(Region superRegion) {
		this.superRegions.add(superRegion);
	}

	public Set<Region> getNeighborRegions() {
		return neighborRegions;
	}

	public void setNeighborRegions(Set<Region> neighborRegions) {
		this.neighborRegions = neighborRegions;
	}

	public void addNeighborRegion(Region neighborRegion) {
		this.neighborRegions.add(neighborRegion);
	}

	public Set<LanguageCode> getEnabledLanguages() {
		return enabledLanguages;
	}

	public void  setEnabledLanguages(Set<LanguageCode> enabledLanguages) {
		this.enabledLanguages = enabledLanguages;
	}

	@JsonProperty("ttsLanguages")
	public Set<LanguageCode> getTTSLanguages() {
		return ttsLanguages;
	}

	@JsonProperty("ttsLanguages")
	public void  setTTSLanguages(Set<LanguageCode> ttsLanguages) {
		this.ttsLanguages = ttsLanguages;
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

	public boolean isCreateCaamlV5() {
		return createCaamlV5;
	}

	public void setCreateCaamlV5(boolean createCaamlV5) {
		this.createCaamlV5 = createCaamlV5;
	}

	public boolean isCreateCaamlV6() {
		return createCaamlV6;
	}

	public void setCreateCaamlV6(boolean createCaamlV6) {
		this.createCaamlV6 = createCaamlV6;
	}

	public boolean isCreateJson() {
		return createJson;
	}

	public void setCreateJson(boolean createJson) {
		this.createJson = createJson;
	}

	public boolean isCreateMaps() {
		return createMaps;
	}

	public void setCreateMaps(boolean createMaps) {
		this.createMaps = createMaps;
	}

	public boolean isCreatePdf() {
		return createPdf;
	}

	public void setCreatePdf(boolean createPdf) {
		this.createPdf = createPdf;
	}

	public boolean isCreateSimpleHtml() {
		return createSimpleHtml;
	}

	public void setCreateSimpleHtml(boolean createSimpleHtml) {
		this.createSimpleHtml = createSimpleHtml;
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

	public boolean isSendWhatsAppMessages() {
		return sendWhatsAppMessages;
	}

	public void setSendWhatsAppMessages(boolean sendWhatsAppMessages) {
		this.sendWhatsAppMessages = sendWhatsAppMessages;
	}

	public boolean isSendPushNotifications() {
		return sendPushNotifications;
	}

	public void setSendPushNotifications(boolean sendPushNotifications) {
		this.sendPushNotifications = sendPushNotifications;
	}

	public boolean isEnableMediaFile() {
		return enableMediaFile;
	}

	public void setEnableMediaFile(boolean enableMediaFile) {
		this.enableMediaFile = enableMediaFile;
	}

	public boolean isEnableAvalancheProblemCornices() {
		return enableAvalancheProblemCornices;
	}

	public void setEnableAvalancheProblemCornices(boolean enableAvalancheProblemCornices) {
		this.enableAvalancheProblemCornices = enableAvalancheProblemCornices;
	}

	public boolean isEnableAvalancheProblemNoDistinctAvalancheProblem() {
		return enableAvalancheProblemNoDistinctAvalancheProblem;
	}

	public void setEnableAvalancheProblemNoDistinctAvalancheProblem(boolean enableAvalancheProblemNoDistinctAvalancheProblem) {
		this.enableAvalancheProblemNoDistinctAvalancheProblem = enableAvalancheProblemNoDistinctAvalancheProblem;
	}

	public boolean isShowMatrix() {
		return showMatrix;
	}

	public void setShowMatrix(boolean showMatrix) {
		this.showMatrix = showMatrix;
	}

	public boolean isEnableStrategicMindset() {
		return enableStrategicMindset;
	}

	public void setEnableStrategicMindset(boolean enableStrategicMindset) {
		this.enableStrategicMindset = enableStrategicMindset;
	}

	public boolean isEnableStressLevel() {
		return enableStressLevel;
	}

	public void setEnableStressLevel(boolean enableStressLevel) {
		this.enableStressLevel = enableStressLevel;
	}

	public ServerInstance getServerInstance() {
		return serverInstance;
	}

	public void setServerInstance(ServerInstance serverInstance) {
		this.serverInstance = serverInstance;
	}

	public String getPdfColor() {
		return pdfColor;
	}

	public void setPdfColor(String pdfColor) {
		this.pdfColor = pdfColor;
	}

	public String getEmailColor() {
		return emailColor;
	}

	public void setEmailColor(String emailColor) {
		this.emailColor = emailColor;
	}

	public int getPdfMapYAmPm() {
		return pdfMapYAmPm;
	}

	public void setPdfMapYAmPm(int pdfMapYAmPm) {
		this.pdfMapYAmPm = pdfMapYAmPm;
	}

	public int getPdfMapYFd() {
		return pdfMapYFd;
	}

	public void setPdfMapYFd(int pdfMapYFd) {
		this.pdfMapYFd = pdfMapYFd;
	}

	public int getPdfMapWidthAmPm() {
		return pdfMapWidthAmPm;
	}

	public void setPdfMapWidthAmPm(int pdfMapWidthAmPm) {
		this.pdfMapWidthAmPm = pdfMapWidthAmPm;
	}

	public int getPdfMapWidthFd() {
		return pdfMapWidthFd;
	}

	public void setPdfMapWidthFd(int pdfMapWidthFd) {
		this.pdfMapWidthFd = pdfMapWidthFd;
	}

	public int getPdfMapHeight() {
		return pdfMapHeight;
	}

	public void setPdfMapHeight(int pdfMapHeight) {
		this.pdfMapHeight = pdfMapHeight;
	}

	public boolean isPdfFooterLogo() {
		return pdfFooterLogo;
	}

	public void setPdfFooterLogo(boolean pdfFooterLogo) {
		this.pdfFooterLogo = pdfFooterLogo;
	}

	public String getPdfFooterLogoColorPath() {
		return pdfFooterLogoColorPath;
	}

	public void setPdfFooterLogoColorPath(String pdfFooterLogoColorPath) {
		this.pdfFooterLogoColorPath = pdfFooterLogoColorPath;
	}

	public String getPdfFooterLogoBwPath() {
		return pdfFooterLogoBwPath;
	}

	public void setPdfFooterLogoBwPath(String pdfFooterLogoBwPath) {
		this.pdfFooterLogoBwPath = pdfFooterLogoBwPath;
	}

	public int getMapXmax() {
		return mapXmax;
	}

	public void setMapXmax(int mapXmax) {
		this.mapXmax = mapXmax;
	}

	public int getMapXmin() {
		return mapXmin;
	}

	public void setMapXmin(int mapXmin) {
		this.mapXmin = mapXmin;
	}

	public int getMapYmax() {
		return mapYmax;
	}

	public void setMapYmax(int mapYmax) {
		this.mapYmax = mapYmax;
	}

	public int getMapYmin() {
		return mapYmin;
	}

	public void setMapYmin(int mapYmin) {
		this.mapYmin = mapYmin;
	}

	public String getSimpleHtmlTemplateName() {
		return simpleHtmlTemplateName;
	}

	public void setSimpleHtmlTemplateName(String simpleHtmlTemplateName) {
		this.simpleHtmlTemplateName = simpleHtmlTemplateName;
	}

	public String getGeoDataDirectory() {
		return geoDataDirectory;
	}

	public void setGeoDataDirectory(String geoDataDirectory) {
		this.geoDataDirectory = geoDataDirectory;
	}

	public String getMapLogoColorPath() {
		return mapLogoColorPath;
	}

	public void setMapLogoColorPath(String mapLogoColorPath) {
		this.mapLogoColorPath = mapLogoColorPath;
	}

	public String getMapLogoBwPath() {
		return mapLogoBwPath;
	}

	public void setMapLogoBwPath(String mapLogoBwPath) {
		this.mapLogoBwPath = mapLogoBwPath;
	}

	public Position getMapLogoPosition() {
		return mapLogoPosition;
	}

	public void setMapLogoPosition(Position mapLogoPosition) {
		this.mapLogoPosition = mapLogoPosition;
	}

	public double getMapCenterLat() {
		return mapCenterLat;
	}

	public void setMapCenterLat(double CenterLat) {
		this.mapCenterLat = CenterLat;
	}

	public double getMapCenterLng() {
		return mapCenterLng;
	}

	public void setMapCenterLng(double CenterLng) {
		this.mapCenterLng = CenterLng;
	}

	public String getImageColorbarColorPath() {
		return imageColorbarColorPath;
	}

	public void setImageColorbarColorPath(String imageColorbarColorPath) {
		this.imageColorbarColorPath = imageColorbarColorPath;
	}

	public String getImageColorbarBwPath() {
		return imageColorbarBwPath;
	}

	public void setImageColorbarBwPath(String imageColorbarBwPath) {
		this.imageColorbarBwPath = imageColorbarBwPath;
	}

	public boolean isEnableDangerSources() {
		return enableDangerSources;
	}

	public void setEnableDangerSources(boolean enableDangerSources) {
		this.enableDangerSources = enableDangerSources;
	}

	public boolean isEnableObservations() {
		return enableObservations;
	}

	public void setEnableObservations(boolean enableObservations) {
		this.enableObservations = enableObservations;
	}

	public boolean isEnableModelling() {
		return enableModelling;
	}

	public void setEnableModelling(boolean enableModelling) {
		this.enableModelling = enableModelling;
	}

	public boolean isEnableEditableFields() { return enableEditableFields; }

	public void setEnableEditableField(boolean enableEditableFields) { this.enableEditableFields = enableEditableFields; }

	public boolean isEnableWeatherbox() {
		return enableWeatherbox;
	}

	public void setEnableWeatherbox(boolean enableWeatherbox) {
		this.enableWeatherbox = enableWeatherbox;
	}

	public boolean isEnableGeneralHeadline() {
		return enableGeneralHeadline;
	}

	public void setEnableGeneralHeadline(boolean enableGeneralHeadline) {
		this.enableGeneralHeadline = enableGeneralHeadline;
	}

	public Element toCAAML(Document doc) {
		Element region = doc.createElement("Region");
		region.setAttribute("gml:id", getId());
		Element regionSubType = doc.createElement("regionSubType");
		region.appendChild(regionSubType);
		return region;
	}

	public boolean affects(String regionId) {
		if (regionId.startsWith(this.getId()))
			return true;

		return subRegions.stream().anyMatch(subRegion -> regionId.startsWith(subRegion.getId()));
	}

	public String toJSON() throws JsonProcessingException {
		return JsonUtil.ALBINA_OBJECT_MAPPER.writeValueAsString(this);
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
	public String toString() {
		return id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

    public static Region readRegion(URL resource) throws UncheckedIOException {
		try {
			return new Region(Resources.toString(resource, StandardCharsets.UTF_8), Region::new);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }

	@JsonIgnore
	public boolean isCreateAudioFiles() {
		return superRegions == null || superRegions.isEmpty();
	}
}

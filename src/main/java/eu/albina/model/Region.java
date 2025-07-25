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
import java.util.function.Function;

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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.openjson.JSONArray;
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

	@Column(name = "SHOW_MATRIX")
	private boolean showMatrix;

	@Column(name = "ENABLE_STRATEGIC_MINDSET")
	private boolean enableStrategicMindset;

	@Column(name = "ENABLE_STRESS_LEVEL")
	private boolean enableStressLevel;

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

	public Region(JSONObject json, Function<String, Region> regionFunction) {
		this();
		if (json.has("id") && !json.isNull("id"))
			this.id = json.getString("id");
		if (json.has("microRegions") && !json.isNull("microRegions"))
			this.microRegions = json.getInt("microRegions");
		if (json.has("subRegions")) {
			JSONArray subRegions = json.getJSONArray("subRegions");
			for (Object entry : subRegions) {
				Region region = regionFunction.apply((String) entry);
				if (region != null)
					this.subRegions.add(region);
			}
		}
		if (json.has("superRegions")) {
			JSONArray superRegions = json.getJSONArray("superRegions");
			for (Object entry : superRegions) {
				Region region = regionFunction.apply((String) entry);
				if (region != null)
					this.superRegions.add(region);
			}
		}
		if (json.has("neighborRegions")) {
			JSONArray neighborRegions = json.getJSONArray("neighborRegions");
			for (Object entry : neighborRegions) {
				Region region = regionFunction.apply((String) entry);
				if (region != null)
					this.neighborRegions.add(region);
			}
		}
		if (json.has("enabledLanguages")) {
			JSONArray enabledLanguages = json.getJSONArray("enabledLanguages");
			for (Object entry : enabledLanguages) {
				this.enabledLanguages.add(LanguageCode.valueOf(((String) entry)));
			}
		}
		if (json.has("ttsLanguages")) {
			JSONArray ttsLanguages = json.getJSONArray("ttsLanguages");
			for (Object entry : ttsLanguages) {
				this.ttsLanguages.add(LanguageCode.valueOf(((String) entry)));
			}
		}
		if (json.has("publishBulletins") && !json.isNull("publishBulletins"))
			this.publishBulletins = json.getBoolean("publishBulletins");
		if (json.has("publishBlogs") && !json.isNull("publishBlogs"))
			this.publishBlogs = json.getBoolean("publishBlogs");
		if (json.has("createCaamlV5") && !json.isNull("createCaamlV5"))
			this.createCaamlV5 = json.getBoolean("createCaamlV5");
		if (json.has("createCaamlV6") && !json.isNull("createCaamlV6"))
			this.createCaamlV6 = json.getBoolean("createCaamlV6");
		if (json.has("createJson") && !json.isNull("createJson"))
			this.createJson = json.getBoolean("createJson");
		if (json.has("createMaps") && !json.isNull("createMaps"))
			this.createMaps = json.getBoolean("createMaps");
		if (json.has("createPdf") && !json.isNull("createPdf"))
			this.createPdf = json.getBoolean("createPdf");
		if (json.has("createSimpleHtml") && !json.isNull("createSimpleHtml"))
			this.createSimpleHtml = json.getBoolean("createSimpleHtml");
		if (json.has("sendEmails") && !json.isNull("sendEmails"))
			this.sendEmails = json.getBoolean("sendEmails");
		if (json.has("sendTelegramMessages") && !json.isNull("sendTelegramMessages"))
			this.sendTelegramMessages = json.getBoolean("sendTelegramMessages");
		if (json.has("sendWhatsAppMessages") && !json.isNull("sendWhatsAppMessages"))
			this.sendWhatsAppMessages = json.getBoolean("sendWhatsAppMessages");
		if (json.has("sendPushNotifications") && !json.isNull("sendPushNotifications"))
			this.sendPushNotifications = json.getBoolean("sendPushNotifications");
		if (json.has("enableMediaFile") && !json.isNull("enableMediaFile"))
			this.enableMediaFile = json.getBoolean("enableMediaFile");
		if (json.has("enableAvalancheProblemCornices") && !json.isNull("enableAvalancheProblemCornices"))
			this.enableAvalancheProblemCornices = json.getBoolean("enableAvalancheProblemCornices");
		if (json.has("enableAvalancheProblemNoDistinctAvalancheProblem") && !json.isNull("enableAvalancheProblemNoDistinctAvalancheProblem"))
			this.enableAvalancheProblemNoDistinctAvalancheProblem = json.getBoolean("enableAvalancheProblemNoDistinctAvalancheProblem");
		if (json.has("enableDangerSources") && !json.isNull("enableDangerSources"))
			this.enableDangerSources = json.getBoolean("enableDangerSources");
		if (json.has("enableObservations") && !json.isNull("enableObservations"))
			this.enableObservations = json.getBoolean("enableObservations");
		if (json.has("enableModelling") && !json.isNull("enableModelling"))
			this.enableModelling = json.getBoolean("enableModelling");
		if (json.has("enableWeatherbox") && !json.isNull("enableWeatherbox"))
			this.enableWeatherbox = json.getBoolean("enableWeatherbox");
		if (json.has("showMatrix") && !json.isNull("showMatrix"))
			this.showMatrix = json.getBoolean("showMatrix");
		if (json.has("enableStrategicMindset") && !json.isNull("enableStrategicMindset"))
			this.enableStrategicMindset = json.getBoolean("enableStrategicMindset");
		if (json.has("enableStressLevel") && !json.isNull("enableStressLevel"))
			this.enableStressLevel = json.getBoolean("enableStressLevel");
		if (json.has("serverInstance") && !json.isNull("serverInstance"))
			this.serverInstance = new ServerInstance(json.getJSONObject("serverInstance"), regionFunction);
		if (json.has("pdfColor") && !json.isNull("pdfColor"))
			this.pdfColor = json.getString("pdfColor");
		if (json.has("emailColor") && !json.isNull("emailColor"))
			this.emailColor = json.getString("emailColor");
		if (json.has("pdfMapYAmPm") && !json.isNull("pdfMapYAmPm"))
			this.pdfMapYAmPm = json.getInt("pdfMapYAmPm");
		if (json.has("pdfMapYFd") && !json.isNull("pdfMapYFd"))
			this.pdfMapYFd = json.getInt("pdfMapYFd");
		if (json.has("pdfMapWidthAmPm") && !json.isNull("pdfMapWidthAmPm"))
			this.pdfMapWidthAmPm = json.getInt("pdfMapWidthAmPm");
		if (json.has("pdfMapWidthFd") && !json.isNull("pdfMapWidthFd"))
			this.pdfMapWidthFd = json.getInt("pdfMapWidthFd");
		if (json.has("pdfMapHeight") && !json.isNull("pdfMapHeight"))
			this.pdfMapHeight = json.getInt("pdfMapHeight");
		if (json.has("pdfFooterLogo") && !json.isNull("pdfFooterLogo"))
			this.pdfFooterLogo = json.getBoolean("pdfFooterLogo");
		if (json.has("pdfFooterLogoColorPath") && !json.isNull("pdfFooterLogoColorPath"))
			this.pdfFooterLogoColorPath = json.getString("pdfFooterLogoColorPath");
		if (json.has("pdfFooterLogoBwPath") && !json.isNull("pdfFooterLogoBwPath"))
			this.pdfFooterLogoBwPath = json.getString("pdfFooterLogoBwPath");
		if (json.has("mapXmax") && !json.isNull("mapXmax"))
			this.mapXmax = json.getInt("mapXmax");
		if (json.has("mapXmin") && !json.isNull("mapXmin"))
			this.mapXmin = json.getInt("mapXmin");
		if (json.has("mapYmax") && !json.isNull("mapYmax"))
			this.mapYmax = json.getInt("mapYmax");
		if (json.has("mapYmin") && !json.isNull("mapYmin"))
			this.mapYmin = json.getInt("mapYmin");
		if (json.has("simpleHtmlTemplateName") && !json.isNull("simpleHtmlTemplateName"))
			this.simpleHtmlTemplateName = json.getString("simpleHtmlTemplateName");
		if (json.has("geoDataDirectory") && !json.isNull("geoDataDirectory"))
			this.geoDataDirectory = json.getString("geoDataDirectory");
		if (json.has("mapLogoColorPath") && !json.isNull("mapLogoColorPath"))
			this.mapLogoColorPath = json.getString("mapLogoColorPath");
		if (json.has("mapLogoBwPath") && !json.isNull("mapLogoBwPath"))
			this.mapLogoBwPath = json.getString("mapLogoBwPath");
		if (json.has("mapLogoPosition"))
			this.mapLogoPosition = Position.fromString(json.getString("mapLogoPosition"));
		if (json.has("mapCenterLat"))
			this.mapCenterLat = json.getDouble("mapCenterLat");
		if (json.has("mapCenterLng"))
			this.mapCenterLng = json.getDouble("mapCenterLng");
		if (json.has("imageColorbarColorPath") && !json.isNull("imageColorbarColorPath"))
			this.imageColorbarColorPath = json.getString("imageColorbarColorPath");
		if (json.has("imageColorbarBwPath") && !json.isNull("imageColorbarBwPath"))
			this.imageColorbarBwPath = json.getString("imageColorbarBwPath");
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

	public boolean isEnableWeatherbox() {
		return enableWeatherbox;
	}

	public void setEnableWeatherbox(boolean enableWeatherbox) {
		this.enableWeatherbox = enableWeatherbox;
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

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		json.put("id", getId());
		json.put("microRegions", getMicroRegions());
		if (subRegions != null && subRegions.size() > 0) {
			JSONArray jsonSubRegions = new JSONArray();
			for (Region subRegion : subRegions) {
				jsonSubRegions.put(subRegion.getId());
			}
			json.put("subRegions", jsonSubRegions);
		} else {
			json.put("subRegions", new JSONArray());
		}
		if (superRegions != null && superRegions.size() > 0) {
			JSONArray jsonSuperRegions = new JSONArray();
			for (Region superRegion : superRegions) {
				jsonSuperRegions.put(superRegion.getId());
			}
			json.put("superRegions", jsonSuperRegions);
		} else {
			json.put("superRegions", new JSONArray());
		}
		if (neighborRegions != null && neighborRegions.size() > 0) {
			JSONArray jsonNeighborRegions = new JSONArray();
			for (Region neighborRegion : neighborRegions) {
				jsonNeighborRegions.put(neighborRegion.getId());
			}
			json.put("neighborRegions", jsonNeighborRegions);
		} else {
			json.put("neighborRegions", new JSONArray());
		}
		if (enabledLanguages != null && enabledLanguages.size() > 0) {
			JSONArray enabledLanguages = new JSONArray();
			for (LanguageCode language : this.enabledLanguages) {
				enabledLanguages.put(language.toString());
			}
			json.put("enabledLanguages", enabledLanguages);
		}
		if (ttsLanguages != null && ttsLanguages.size() > 0) {
			JSONArray ttsLanguages = new JSONArray();
			for (LanguageCode language : this.ttsLanguages) {
				ttsLanguages.put(language.toString());
			}
			json.put("ttsLanguages", ttsLanguages);
		}
		json.put("publishBulletins", isPublishBulletins());
		json.put("publishBlogs", isPublishBlogs());
		json.put("createCaamlV5", isCreateCaamlV5());
		json.put("createCaamlV6", isCreateCaamlV6());
		json.put("createJson", isCreateJson());
		json.put("createMaps", isCreateMaps());
		json.put("createPdf", isCreatePdf());
		json.put("createSimpleHtml", isCreateSimpleHtml());
		json.put("sendEmails", isSendEmails());
		json.put("sendTelegramMessages", isSendTelegramMessages());
		json.put("sendWhatsAppMessages", isSendWhatsAppMessages());
		json.put("sendPushNotifications", isSendPushNotifications());
		json.put("enableMediaFile", isEnableMediaFile());
		json.put("enableAvalancheProblemCornices", isEnableAvalancheProblemCornices());
		json.put("enableAvalancheProblemNoDistinctAvalancheProblem", isEnableAvalancheProblemNoDistinctAvalancheProblem());
		json.put("enableDangerSources", isEnableDangerSources());
		json.put("enableObservations", isEnableObservations());
		json.put("enableModelling", isEnableModelling());
		json.put("enableWeatherbox", isEnableWeatherbox());
		json.put("showMatrix", isShowMatrix());
		json.put("enableStrategicMindset", isEnableStrategicMindset());
		json.put("enableStressLevel", isEnableStressLevel());
		if (getServerInstance() != null) {
			json.put("serverInstance", getServerInstance().toJSON());
		}
		json.put("pdfColor", getPdfColor());
		json.put("emailColor", getEmailColor());
		json.put("pdfMapYAmPm", getPdfMapYAmPm());
		json.put("pdfMapYFd", getPdfMapYFd());
		json.put("pdfMapWidthAmPm", getPdfMapWidthAmPm());
		json.put("pdfMapWidthFd", getPdfMapWidthFd());
		json.put("pdfMapHeight", getPdfMapHeight());
		json.put("pdfFooterLogo", isPdfFooterLogo());
		json.put("pdfFooterLogoColorPath", getPdfFooterLogoColorPath());
		json.put("pdfFooterLogoBwPath", getPdfFooterLogoBwPath());
		json.put("mapXmax", getMapXmax());
		json.put("mapXmin", getMapXmin());
		json.put("mapYmax", getMapYmax());
		json.put("mapYmin", getMapYmin());
		json.put("simpleHtmlTemplateName", getSimpleHtmlTemplateName());
		json.put("geoDataDirectory", getGeoDataDirectory());
		json.put("mapLogoColorPath", getMapLogoColorPath());
		json.put("mapLogoBwPath", getMapLogoBwPath());
		json.put("mapLogoPosition", getMapLogoPosition().toString());
		json.put("mapCenterLat", getMapCenterLat());
		json.put("mapCenterLng", getMapCenterLng());
		json.put("imageColorbarColorPath", getImageColorbarColorPath());
		json.put("imageColorbarBwPath", getImageColorbarBwPath());

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
	public String toString() {
		return id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

    public static Region readRegion(URL resource) throws UncheckedIOException {
		try {
			final String validRegionStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
			return new Region(new JSONObject(validRegionStringFromResource), Region::new);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }

	@JsonIgnore
	public boolean isCreateAudioFiles() {
		return superRegions == null || superRegions.isEmpty();
	}
}

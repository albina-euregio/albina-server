// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Position;
import eu.albina.model.enumerations.TextPart;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * This class holds all information about one region.
 *
 * @author Norbert Lanzanasto
 *
 */
@Entity
@Table(name = "regions")
@Serdeable
@Introspected(excludedAnnotations = {JsonIgnore.class})
public class Region implements PersistentObject {

	@Id
	@Schema(description = "Region ID")
	@Column(name = "ID", length = 191)
	private String id;

	@Schema(description = "Number of micro regions")
	@Column(name = "MICRO_REGIONS")
	private int microRegions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="region_hierarchy",
	 joinColumns=@JoinColumn(name="SUPER_REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="SUB_REGION_ID")
	)
	@JsonIgnore
	private Set<Region> subRegions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="region_hierarchy",
	 joinColumns=@JoinColumn(name="SUB_REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="SUPER_REGION_ID")
	)
	@JsonIgnore
	private Set<Region> superRegions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="region_neighbors",
	 joinColumns=@JoinColumn(name="REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="NEIGHBOR_REGION_ID")
	)
	@JsonIgnore
	private Set<Region> neighborRegions;

	@OneToMany(mappedBy = "region", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@Schema(description = "Language configuration")
	private Set<RegionLanguageConfiguration> languageConfigurations;

	@Schema(description = "Enabled languages")
	@Column(name = "ENABLED_LANGUAGES", columnDefinition = LanguageCode.Converter.COLUMN_DEFINITION)
	@Convert(converter = LanguageCode.Converter.class)
	private Set<LanguageCode> enabledLanguages;

	@Schema(description = "Text-to-speech languages")
	@Column(name = "TTS_LANGUAGES", columnDefinition = LanguageCode.Converter.COLUMN_DEFINITION)
	@Convert(converter = LanguageCode.Converter.class)
	private Set<LanguageCode> ttsLanguages;

	@Schema(description = "Publish avalanche forecast")
	@Column(name = "PUBLISH_BULLETINS")
	private boolean publishBulletins;

	@Schema(description = "Publish blog posts")
	@Column(name = "PUBLISH_BLOGS")
	private boolean publishBlogs;

	@Schema(description = "Create CAAML v6")
	@Column(name = "CREATE_CAAML_V6")
	private boolean createCaamlV6;

	@Schema(description = "Create JSON")
	@Column(name = "CREATE_JSON")
	private boolean createJson;

	@Schema(description = "Create maps")
	@Column(name = "CREATE_MAPS")
	private boolean createMaps;

	@Schema(description = "Create PDF")
	@Column(name = "CREATE_PDF")
	private boolean createPdf;

	@Schema(description = "Create simple HTML")
	@Column(name = "CREATE_SIMPLE_HTML")
	private boolean createSimpleHtml;

	@Schema(description = "Send emails")
	@Column(name = "SEND_EMAILS")
	private boolean sendEmails;

	@Schema(description = "Send telegram messages")
	@Column(name = "SEND_TELEGRAM_MESSAGES")
	private boolean sendTelegramMessages;

	@Schema(description = "Send WhatsApp messages")
	@Column(name = "SEND_WHATSAPP_MESSAGES")
	private boolean sendWhatsAppMessages;

	@Schema(description = "Send push notifications")
	@Column(name = "SEND_PUSH_NOTIFICATIONS")
	private boolean sendPushNotifications;

	@Schema(description = "Enable media file")
	@Column(name = "ENABLE_MEDIA_FILE")
	private boolean enableMediaFile;

	@Schema(description = "Enable avalanche problem CORNICES")
	@Column(name = "ENABLE_AVALANCHE_PROBLEM_CORNICES")
	private boolean enableAvalancheProblemCornices;

	@Schema(description = "Enable avalanche problem NO DISTINCT AVALANCHE PROBLEM")
	@Column(name = "ENABLE_AVALANCHE_PROBLEM_NO_DISTINCT_AVALANCHE_PROBLEM")
	private boolean enableAvalancheProblemNoDistinctAvalancheProblem;

	@Schema(description = "Enable danger sources")
	@Column(name = "ENABLE_DANGER_SOURCES")
	private boolean enableDangerSources;

	@Schema(description = "Enable observations")
	@Column(name = "ENABLE_OBSERVATIONS")
	private boolean enableObservations;

	@Schema(description = "Enable incidents")
	@Column(name = "ENABLE_INCIDENTS")
	private boolean enableIncidents;

	@Schema(description = "Enable modelling")
	@Column(name = "ENABLE_MODELLING")
	private boolean enableModelling;

	@Schema(description = "Enable LINEA export")
	@Column(name = "ENABLE_LINEA_EXPORT")
	private boolean enableLineaExport;

	@Schema(description = "Enable weather")
	@Column(name = "ENABLE_ICON")
	private boolean enableIcon;

	@Schema(description = "Textfields for bulletins to be entered using textcat")
	@Column(name = "ENABLED_TEXTCAT_FIELDS", columnDefinition = TextPart.Converter.COLUMN_DEFINITION)
	@Convert(converter = TextPart.Converter.class)
	private Set<TextPart> enabledTextcatFields;

	@Schema(description = "Editable textfields instead of textcat for bulletins")
	@Column(name = "ENABLED_EDITABLE_FIELDS", columnDefinition = TextPart.Converter.COLUMN_DEFINITION)
	@Convert(converter = TextPart.Converter.class)
	private Set<TextPart> enabledEditableFields;

	@Schema(description = "Show matrix")
	@Column(name = "SHOW_MATRIX")
	private boolean showMatrix;

	@Schema(description = "Enable strategic mindset")
	@Column(name = "ENABLE_STRATEGIC_MINDSET")
	private boolean enableStrategicMindset;

	@Schema(description = "Enable stress level")
	@Column(name = "ENABLE_STRESS_LEVEL")
	private boolean enableStressLevel;

	@Schema(description = "PDF color")
	@Column(name = "PDF_COLOR", length = 191)
	private String pdfColor;

	@Schema(description = "Email color")
	@Column(name = "EMAIL_COLOR", length = 191)
	private String emailColor;

	@Schema(description = "Y for PDF map (am/pm)")
	@Column(name = "PDF_MAP_Y_AM_PM")
	private int pdfMapYAmPm;

	@Schema(description = "Y for PDF map (fd)")
	@Column(name = "PDF_MAP_Y_FD")
	private int pdfMapYFd;

	@Schema(description = "Map width for PDF (am/pm)")
	@Column(name = "PDF_MAP_WIDTH_AM_PM")
	private int pdfMapWidthAmPm;

	@Schema(description = "Map width for PDF (fd)")
	@Column(name = "PDF_MAP_WIDTH_FD")
	private int pdfMapWidthFd;

	@Schema(description = "Map height for PDF")
	@Column(name = "PDF_MAP_HEIGHT")
	private int pdfMapHeight;

	@Schema(description = "Logo for PDF footer")
	@Column(name = "PDF_FOOTER_LOGO")
	private boolean pdfFooterLogo;

	@Schema(description = "Logo for PDF footer (color)")
	@Column(name = "PDF_FOOTER_LOGO_COLOR_PATH", length = 191)
	private String pdfFooterLogoColorPath;

	@Schema(description = "Logo for PDF footer (bw)")
	@Column(name = "PDF_FOOTER_LOGO_BW_PATH", length = 191)
	private String pdfFooterLogoBwPath;

	@Schema(description = "Geodata directory")
	@Column(name = "GEO_DATA_DIRECTORY", length = 191)
	private String geoDataDirectory;

	@Schema(description = "Logo position for map")
	@Enumerated(EnumType.STRING)
	@Column(name = "MAP_LOGO_POSITION", length = 191)
	private Position mapLogoPosition;

	@Schema(description = "Colorbar (color)")
	@Column(name = "IMAGE_COLORBAR_COLOR_PATH", length = 191)
	private String imageColorbarColorPath;

	@Schema(description = "Colorbar (b/w)")
	@Column(name = "IMAGE_COLORBAR_BW_PATH", length = 191)
	private String imageColorbarBwPath;

	@Schema(description = "Default language for language dependent configuration")
	@Enumerated(EnumType.STRING)
	@Column(name = "DEFAULT_LANG", length = 191)
	private LanguageCode defaultLang;

	@Schema(description = "Logo for PDF (color)")
	@Column(name = "LOGO_PATH", length = 191)
	private String logoPath;

	@Schema(description = "Logo for PDF (bw)")
	@Column(name = "LOGO_BW_PATH", columnDefinition = "LONGBLOB")
	private String logoBwPath;

	@Schema(description = "Image URL for coat of arms")
	@Column(name = "COAT_OF_ARMS", columnDefinition = "LONGBLOB")
	private String coatOfArms;

	@Schema(description = "URL to static avalanche files")
	@Column(name = "STATIC_URL", length = 191)
	private String staticUrl;

	@Schema(description = "URL to server images")
	@Column(name = "SERVER_IMAGES_URL", length = 191)
	private String serverImagesUrl;

	@Schema(description = "URL to education content")
	@Column(name = "EDUCATION_URL", length = 191)
	private String educationUrl;

	@Schema(description = "URL to AWSOME modelling configuration")
	@Column(name = "AWSOME_URL", length = 191)
	private String awsomeUrl;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
		this.superRegions = new HashSet<>();
		this.subRegions = new HashSet<>();
		this.neighborRegions = new HashSet<>();
		this.languageConfigurations = new HashSet<>();
		this.enabledLanguages = new HashSet<>();
		this.ttsLanguages = new HashSet<>();
	}

	public Region(String id) {
		super();
		this.id = id;
	}

	public void updateFromJSON(String json, ObjectMapper objectMapper) throws IOException {
		JsonNode node = objectMapper.readValue(json, JsonNode.class);
		objectMapper.updateValueFromTree(this, node);
	}

	public void fixLanguageConfigurations() {
		if (languageConfigurations == null) {
			return;
		}
		for (RegionLanguageConfiguration languageConfiguration : languageConfigurations) {
			languageConfiguration.setRegion(this);
		}
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

	@JsonProperty("subRegions")
	@Schema(description = "ID of sub regions")
	public Set<String> getSubRegionsString() {
		return subRegions.stream().map(Region::getId).collect(Collectors.toSet());
	}

	@JsonProperty("subRegions")
	public void setSubRegionsString(Set<String> subRegions) {
		this.subRegions = subRegions.stream().map(Region::new).collect(Collectors.toSet());
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

	@JsonProperty("superRegions")
	@Schema(description = "ID of super regions")
	public Set<String> getSuperRegionsString() {
		return superRegions.stream().map(Region::getId).collect(Collectors.toSet());
	}

	@JsonProperty("superRegions")
	public void setSuperRegionsString(Set<String> superRegions) {
		this.superRegions = superRegions.stream().map(Region::new).collect(Collectors.toSet());
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

	@JsonProperty("neighborRegions")
	@Schema(description = "ID of neighbouring regions")
	public Set<String> getNeighborRegionsString() {
		return neighborRegions.stream().map(Region::getId).collect(Collectors.toSet());
	}

	@JsonProperty("neighborRegions")
	public void setNeighborRegionsString(Set<String> neighborRegions) {
		this.neighborRegions = neighborRegions.stream().map(Region::new).collect(Collectors.toSet());
	}

	public void setNeighborRegions(Set<Region> neighborRegions) {
		this.neighborRegions = neighborRegions;
	}

	public void addNeighborRegion(Region neighborRegion) {
		this.neighborRegions.add(neighborRegion);
	}

	public Set<RegionLanguageConfiguration> getLanguageConfigurations() {
		return languageConfigurations;
	}

	@JsonIgnore
	public Optional<RegionLanguageConfiguration> getDefaultLanguageConfiguration() {
		return getLanguageConfiguration(defaultLang);
	}

	public Optional<RegionLanguageConfiguration> getLanguageConfiguration(LanguageCode lang) {
		if (languageConfigurations == null) {
			return Optional.empty();
		}
		return languageConfigurations.stream()
			.filter(config -> config.getLang() == lang)
			.findFirst();
	}
	private <T> T getFromLanguageConfig(
		LanguageCode languageCode,
		Function<RegionLanguageConfiguration, T> extractor,
		T fallbackValue) {

		return getLanguageConfiguration(languageCode)
			.map(extractor)
			.orElseGet(() -> getDefaultLanguageConfiguration()
				.map(extractor)
				.orElse(fallbackValue));
	}

	public String getWebsiteName(LanguageCode languageCode) {
		return getFromLanguageConfig(languageCode, RegionLanguageConfiguration::getWebsiteName, "");
	}

	public String getWarningServiceEmail(LanguageCode languageCode) {
		return getFromLanguageConfig(languageCode, RegionLanguageConfiguration::getWarningServiceEmail, "");
	}

	public String getWarningServiceName(LanguageCode languageCode) {
		return getFromLanguageConfig(languageCode, RegionLanguageConfiguration::getWarningServiceName, "");
	}

	public String getWebsiteUrl(LanguageCode languageCode) {
		return getFromLanguageConfig(languageCode, RegionLanguageConfiguration::getUrl, "").replaceAll("/$", "");
	}

	public String getWebsiteUrlWithDate(LanguageCode lang, HasValidityDate avalancheReport) {
		String url = getFromLanguageConfig(lang, RegionLanguageConfiguration::getUrlWithDate, "");
		String date = avalancheReport.getValidityDateString();
		return String.format(url, date);
	}

	public String getImprintLink(LanguageCode lang) {
		return String.format("%s/more/imprint", getWebsiteUrl(lang));
	}

	public void setLanguageConfigurations(Set<RegionLanguageConfiguration> languageConfigurations) {
		this.languageConfigurations = languageConfigurations;
	}

	public void addLanguageConfiguration(RegionLanguageConfiguration languageConfiguration) {
		this.languageConfigurations.add(languageConfiguration);
	}

	public void setStaticUrl(String staticUrl) {
		this.staticUrl = staticUrl;
	}

	public String getStaticUrl() {
		return staticUrl == null ? null : staticUrl.replaceAll("/$", "");
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

	public boolean isEnableIcon() {
		return enableIcon;
	}

	public void setEnableIcon(boolean enableIcon) {
		this.enableIcon = enableIcon;
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

	public String getGeoDataDirectory() {
		return geoDataDirectory;
	}

	public void setGeoDataDirectory(String geoDataDirectory) {
		this.geoDataDirectory = geoDataDirectory;
	}

	public Position getMapLogoPosition() {
		return mapLogoPosition;
	}

	public void setMapLogoPosition(Position mapLogoPosition) {
		this.mapLogoPosition = mapLogoPosition;
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

	public boolean isEnableIncidents() {
		return enableIncidents;
	}

	public void setEnableIncidents(boolean enableIncidents) {
		this.enableIncidents = enableIncidents;
	}

	public boolean isEnableModelling() {
		return enableModelling;
	}

	public Set<TextPart> getEnabledTextcatFields() {
		return enabledTextcatFields;
	}

	public void setEnabledTextcatFields(Set<TextPart> enabledTextcatFields) {
		this.enabledTextcatFields = enabledTextcatFields;
	}

	public void setEnableModelling(boolean enableModelling) {
		this.enableModelling = enableModelling;
	}

	public Set<TextPart> getEnabledEditableFields() {
		return enabledEditableFields;
	}

	public void setEnabledEditableFields(Set<TextPart> enabledEditableFields) {
		this.enabledEditableFields = enabledEditableFields;
	}

	public boolean isEnableLineaExport() {
		return enableLineaExport;
	}

	public void setEnableLineaExport(boolean enableLineaExport) {
		this.enableLineaExport = enableLineaExport;
	}

	public LanguageCode getDefaultLang() { return defaultLang; }

	public void setDefaultLang(LanguageCode defaultLang) {
		this.defaultLang = defaultLang;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public void setLogoBwPath(String logoBw) {
		this.logoBwPath = logoBw;
	}

	public String getLogoBwPath() {
		return logoBwPath;
	}

	public String getCoatOfArms() {
		return coatOfArms;
	}

	public void setCoatOfArms(String coatOfArms) {
		this.coatOfArms = coatOfArms;
	}

	public String getServerImagesUrl() {
		return serverImagesUrl;
	}

	public void setServerImagesUrl(String serverImagesUrl) {
		this.serverImagesUrl = serverImagesUrl;
	}

	public String getEducationUrl() {
		return educationUrl;
	}

	public void setEducationUrl(String educationUrl) {
		this.educationUrl = educationUrl;
	}

	public String getAwsomeUrl() {
		return awsomeUrl;
	}

	public void setAwsomeUrl(String awsomeUrl) {
		this.awsomeUrl = awsomeUrl;
	}

	public boolean affects(String regionId) {
		if (regionId.startsWith(this.getId()))
			return true;

		return subRegions.stream().anyMatch(subRegion -> regionId.startsWith(subRegion.getId()));
	}

	public boolean isForeign(String regionId) {
		return !affects(regionId);
	}

	@JsonIgnore
	public boolean isEnableGeneralHeadline() {
		return Objects.requireNonNullElse(enabledTextcatFields, Set.of()).contains(TextPart.generalHeadlineComment)
			|| Objects.requireNonNullElse(enabledEditableFields, Set.of()).contains(TextPart.generalHeadlineComment);
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
}

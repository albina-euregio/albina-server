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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.albina.controller.RegionController;
import eu.albina.map.Position;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "MICRO_REGIONS")
	private int microRegions;

	@ManyToMany
	@JoinTable(name="super_region_sub_region",
	 joinColumns=@JoinColumn(name="SUPER_REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="SUB_REGION_ID")
	)
	private List<Region> subRegions;

	@ManyToMany
	@JoinTable(name="super_region_sub_region",
	 joinColumns=@JoinColumn(name="SUB_REGION_ID"),
	 inverseJoinColumns=@JoinColumn(name="SUPER_REGION_ID")
	)
	private List<Region> superRegions;

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

	@Column(name = "CREATE_STATIC_WIDGET")
	private boolean createStaticWidget;

	@Column(name = "CREATE_SIMPLE_HTML")
	private boolean createSimpleHtml;

	@Column(name = "SEND_EMAILS")
	private boolean sendEmails;

	@Column(name = "SEND_TELEGRAM_MESSAGES")
	private boolean sendTelegramMessages;

	@Column(name = "SEND_PUSH_NOTIFICATIONS")
	private boolean sendPushNotifications;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVER_INSTANCE_ID")
	private ServerInstance serverInstance;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "rasterFilePath", column = @Column(name = "THUMBNAIL_RASTER_FILE_PATH")),
		@AttributeOverride(name = "countriesShapeFilePath", column = @Column(name = "THUMBNAIL_COUNTRIES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "provincesShapeFilePath", column = @Column(name = "THUMBNAIL_PROVINCES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "microRegionsShapeFilePath", column = @Column(name = "THUMBNAIL_MICRO_REGIONS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "riversShapeFilePath", column = @Column(name = "THUMBNAIL_RIVERS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "lakesShapeFilePath", column = @Column(name = "THUMBNAIL_LAKES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "citiesShapeFilePath", column = @Column(name = "THUMBNAIL_CITIES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "peaksShapeFilePath", column = @Column(name = "THUMBNAIL_PEAKS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "namesPShapeFilePath", column = @Column(name = "THUMBNAIL_NAMES_P_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "namesLShapeFilePath", column = @Column(name = "THUMBNAIL_NAMES_L_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "regionShapeFilePath", column = @Column(name = "THUMBNAIL_REGION_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "ppShapeFilePath", column = @Column(name = "THUMBNAIL_PP_SHAPE_FILE_PATH")) })
	private MapProductionConfiguration thumbnailMapConfig;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "rasterFilePath", column = @Column(name = "STANDARD_RASTER_FILE_PATH")),
		@AttributeOverride(name = "countriesShapeFilePath", column = @Column(name = "STANDARD_COUNTRIES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "provincesShapeFilePath", column = @Column(name = "STANDARD_PROVINCES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "microRegionsShapeFilePath", column = @Column(name = "STANDARD_MICRO_REGIONS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "riversShapeFilePath", column = @Column(name = "STANDARD_RIVERS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "lakesShapeFilePath", column = @Column(name = "STANDARD_LAKES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "citiesShapeFilePath", column = @Column(name = "STANDARD_CITIES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "peaksShapeFilePath", column = @Column(name = "STANDARD_PEAKS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "namesPShapeFilePath", column = @Column(name = "STANDARD_NAMES_P_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "namesLShapeFilePath", column = @Column(name = "STANDARD_NAMES_L_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "regionShapeFilePath", column = @Column(name = "STANDARD_REGION_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "ppShapeFilePath", column = @Column(name = "STANDARD_PP_SHAPE_FILE_PATH")) })
	private MapProductionConfiguration standardMapConfig;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "rasterFilePath", column = @Column(name = "OVERLAY_RASTER_FILE_PATH")),
		@AttributeOverride(name = "countriesShapeFilePath", column = @Column(name = "OVERLAY_COUNTRIES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "provincesShapeFilePath", column = @Column(name = "OVERLAY_PROVINCES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "microRegionsShapeFilePath", column = @Column(name = "OVERLAY_MICRO_REGIONS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "riversShapeFilePath", column = @Column(name = "OVERLAY_RIVERS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "lakesShapeFilePath", column = @Column(name = "OVERLAY_LAKES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "citiesShapeFilePath", column = @Column(name = "OVERLAY_CITIES_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "peaksShapeFilePath", column = @Column(name = "OVERLAY_PEAKS_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "namesPShapeFilePath", column = @Column(name = "OVERLAY_NAMES_P_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "namesLShapeFilePath", column = @Column(name = "OVERLAY_NAMES_L_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "regionShapeFilePath", column = @Column(name = "OVERLAY_REGION_SHAPE_FILE_PATH")),
		@AttributeOverride(name = "ppShapeFilePath", column = @Column(name = "OVERLAY_PP_SHAPE_FILE_PATH")) })
	private MapProductionConfiguration overlayMapConfig;

	// AT-07, IT-32-BZ, IT-32-TN, EUREGIO: RGB(0, 172, 251)
	// ES-CT-L: RGB(0xa2, 0x0d, 0x2d)
	@Column(name = "PDF_COLOR")
	private String pdfColor;

	// EUREGIO, ARAN: 130
	// AT-07, IT-32-BZ, IT-32-TN: 130
	@Column(name = "PDF_MAP_Y_AM_PM")
	private int pdfMapYAmPm;

	// EUREGIO, ARAN: 250
	// AT-07, IT-32-BZ, IT-32-TN: 290
	@Column(name = "PDF_MAP_Y_FD")
	private int pdfMapYFd;

	// EUREGIO, ARAN: 270
	// AT-07, IT-32-BZ, IT-32-TN: 400
	@Column(name = "PDF_MAP_WIDTH_AM_PM")
	private int pdfMapWidthAmPm;

	// EUREGIO, ARAN: 420
	// AT-07, IT-32-BZ, IT-32-TN: 500
	@Column(name = "PDF_MAP_WIDTH_FD")
	private int pdfMapWidthFd;

	// EUREGIO, ARAN: 270
	// AT-07, IT-32-BZ, IT-32-TN: 400/3*2
	@Column(name = "PDF_MAP_HEIGHT")
	private int pdfMapHeight;

	// EUREGIO, AT-07, IT-32-BZ, IT-32-TN: true
	// ARAN: false
	@Column(name = "PDF_FOOTER_LOGO")
	private boolean pdfFooterLogo;

	// EUREGIO, AT-07, IT-32-BZ, IT-32-TN: images/logo/color/euregio.png
	// ARAN: -
	@Column(name = "PDF_FOOTER_LOGO_COLOR_PATH")
	private String pdfFooterLogoColorPath;

	// EUREGIO, AT-07, IT-32-BZ, IT-32-TN: images/logo/grey/euregio.png
	// ARAN: -
	@Column(name = "PDF_FOOTER_LOGO_BW_PATH")
	private String pdfFooterLogoBwPath;

	// EUREGIO, AT-07, IT-32-BZ, IT-32-TN: images/logo/color/euregio.png
	// ARAN: -
	@Column(name = "STATIC_WIDGET_SECONDARY_LOGO_COLOR_PATH")
	private String staticWidgetSecondaryLogoColorPath;

	// EUREGIO 1464000
	// ES-CT-L 120500
	// AT-07 1452000
	// IT-32-BZ 1400000
	// IT-32-TN 1358000
	@Column(name = "MAP_X_MAX")
	private int mapXmax;

	// EUREGIO 1104000
	// ES-CT-L 66200
	// AT-07 1116000
	// IT-32-BZ 1145000
	// IT-32-TN 1133000
	@Column(name = "MAP_X_MIN")
	private int mapXmin;

	// EUREGIO 6047000
	// ES-CT-L 5266900
	// AT-07 6053000
	// IT-32-BZ 5939000
	// IT-32-TN 5842000
	@Column(name = "MAP_Y_MAX")
	private int mapYmax;

	// EUREGIO 5687000
	// ES-CT-L 5215700
	// AT-07 5829000
	// IT-32-BZ 5769000
	// IT-32-TN 5692000
	@Column(name = "MAP_Y_MIN")
	private int mapYmin;

	// EUREGIO simple-bulletin.min.html
	// ES-CT-L simple-bulletin.aran.html
	// AT-07 simple-bulletin.min.html
	// IT-32-BZ simple-bulletin.min.html
	// IT-32-TN simple-bulletin.min.html
	@Column(name = "SIMPLE_HTML_TEMPLATE_NAME")
	private String simpleHtmlTemplateName;


	// EUREGIO geodata.Euregio/
	// ES-CT-L geodata.Aran/
	// AT-07 geodata.Euregio/
	// IT-32-BZ geodata.Euregio/
	// IT-32-TN geodata.Euregio/
	@Column(name = "GEO_DATA_DIRECTORY")
	private String geoDataDirectory;

	// EUREGIO images/logo/color/euregio_map.png
	// ES-CT-L images/logo/color/lauegi_map.png
	// AT-07 images/logo/color/euregio_map.png
	// IT-32-BZ images/logo/color/euregio_map.png
	// IT-32-TN images/logo/color/euregio_map.png
	@Column(name = "MAP_LOGO_COLOR_PATH")
	private String mapLogoColorPath;

	// EUREGIO images/logo/grey/euregio_map.png
	// ES-CT-L images/logo/grey/lauegi_map.png
	// AT-07 images/logo/grey/euregio_map.png
	// IT-32-BZ images/logo/grey/euregio_map.png
	// IT-32-TN images/logo/grey/euregio_map.png
	@Column(name = "MAP_LOGO_BW_PATH")
	private String mapLogoBwPath;

	// EUREGIO bottomright
	// ES-CT-L topright
	// AT-07 
	// IT-32-BZ 
	// IT-32-TN 
	@Enumerated(EnumType.STRING)
	@Column(name = "MAP_LOGO_POSITION")
	private Position mapLogoPosition;

	// ARAN: images/logo/color/colorbar.Aran.gif
	// EUREGIO, AT-07, IT-32-BZ, IT-32-TN: images/logo/color/colorbar.gif
	@Column(name = "IMAGE_COLORBAR_COLOR_PATH")
	private String imageColorbarColorPath;

	// ARAN, EUREGIO, AT-07, IT-32-BZ, IT-32-TN: images/logo/grey/colorbar.gif
	@Column(name = "IMAGE_COLORBAR_BW_PATH")
	private String imageColorbarBwPath;

	/**
	 * Default constructor. Initializes all collections of the region.
	 */
	public Region() {
		this.superRegions = new ArrayList<Region>();
		this.subRegions = new ArrayList<Region>();
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

	public Region(JSONObject json) {
		this();
		if (json.has("id") && !json.isNull("id"))
			this.id = json.getString("id");
		if (json.has("microRegions") && !json.isNull("microRegions"))
			this.microRegions = json.getInt("microRegions");
		if (json.has("subRegions")) {
			JSONArray subRegions = json.getJSONArray("subRegions");
			for (Object entry : subRegions) {
				this.subRegions.add(RegionController.getInstance().getRegion((String) entry));
			}
		}
		if (json.has("superRegions")) {
			JSONArray superRegions = json.getJSONArray("superRegions");
			for (Object entry : superRegions) {
				this.superRegions.add(RegionController.getInstance().getRegion((String) entry));
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
		if (json.has("createStaticWidget") && !json.isNull("createStaticWidget"))
			this.createStaticWidget = json.getBoolean("createStaticWidget");
		if (json.has("createSimpleHtml") && !json.isNull("createSimpleHtml"))
			this.createSimpleHtml = json.getBoolean("createSimpleHtml");
		if (json.has("sendEmails") && !json.isNull("sendEmails"))
			this.sendEmails = json.getBoolean("sendEmails");
		if (json.has("sendTelegramMessages") && !json.isNull("sendTelegramMessages"))
			this.sendTelegramMessages = json.getBoolean("sendTelegramMessages");
		if (json.has("sendPushNotifications") && !json.isNull("sendPushNotifications"))
			this.sendPushNotifications = json.getBoolean("sendPushNotifications");
		if (json.has("serverInstance") && !json.isNull("serverInstance"))
			this.serverInstance = new ServerInstance(json.getJSONObject("serverInstance"));
		if (json.has("thumbnailMapConfig") && !json.isNull("thumbnailMapConfig"))
			this.thumbnailMapConfig = new MapProductionConfiguration(json.getJSONObject("thumbnailMapConfig"));
		if (json.has("standardMapConfig") && !json.isNull("standardMapConfig"))
			this.standardMapConfig = new MapProductionConfiguration(json.getJSONObject("standardMapConfig"));
		if (json.has("overlayMapConfig") && !json.isNull("overlayMapConfig"))
			this.overlayMapConfig = new MapProductionConfiguration(json.getJSONObject("overlayMapConfig"));
		if (json.has("pdfColor") && !json.isNull("pdfColor"))
			this.pdfColor = json.getString("pdfColor");
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
		if (json.has("staticWidgetSecondaryLogoColorPath") && !json.isNull("staticWidgetSecondaryLogoColorPath"))
			this.staticWidgetSecondaryLogoColorPath = json.getString("staticWidgetSecondaryLogoColorPath");
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
		if (json.has("logoPosition"))
			this.mapLogoPosition = Position.fromString(json.getString("logoPosition"));
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

	public List<Region> getSuperRegions() {
		return superRegions;
	}

	public void setSuperRegions(List<Region> superRegions) {
		this.superRegions = superRegions;
	}

	public void addSuperRegion(Region superRegion) {
		this.superRegions.add(superRegion);
	}

	public List<Region> getSubRegions() {
		return subRegions;
	}

	public void setSubRegions(List<Region> subRegions) {
		this.subRegions = subRegions;
	}

	public void addSubRegion(Region subRegion) {
		this.subRegions.add(subRegion);
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

	public boolean isCreateStaticWidget() {
		return createStaticWidget;
	}

	public void setCreateStaticWidget(boolean createStaticWidget) {
		this.createStaticWidget = createStaticWidget;
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

	public boolean isSendPushNotifications() {
		return sendPushNotifications;
	}

	public void setSendPushNotifications(boolean sendPushNotifications) {
		this.sendPushNotifications = sendPushNotifications;
	}

	public ServerInstance getServerInstance() {
		return serverInstance;
	}

	public void setServerInstance(ServerInstance serverInstance) {
		this.serverInstance = serverInstance;
	}

	public MapProductionConfiguration getThumbnailMapConfig() {
		return thumbnailMapConfig;
	}

	public void setThumbnailMapConfig(MapProductionConfiguration thumbnailMapConfig) {
		this.thumbnailMapConfig = thumbnailMapConfig;
	}

	public MapProductionConfiguration getStandardMapConfig() {
		return standardMapConfig;
	}

	public void setStandardMapConfig(MapProductionConfiguration standardMapConfig) {
		this.standardMapConfig = standardMapConfig;
	}

	public MapProductionConfiguration getOverlayMapConfig() {
		return overlayMapConfig;
	}

	public void setOverlayMapConfig(MapProductionConfiguration overlayMapConfig) {
		this.overlayMapConfig = overlayMapConfig;
	}

	public String getPdfColor() {
		return pdfColor;
	}

	public void setPdfColor(String pdfColor) {
		this.pdfColor = pdfColor;
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

	public String getStaticWidgetSecondaryLogoColorPath() {
		return staticWidgetSecondaryLogoColorPath;
	}

	public void setStaticWidgetSecondaryLogoColorPath(String staticWidgetSecondaryLogoColorPath) {
		this.staticWidgetSecondaryLogoColorPath = staticWidgetSecondaryLogoColorPath;
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

	public Position getLogoPosition() {
		return mapLogoPosition;
	}

	public void setLogoPosition(Position logoPosition) {
		this.mapLogoPosition = logoPosition;
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
		}
		if (superRegions != null && superRegions.size() > 0) {
			JSONArray jsonSuperRegions = new JSONArray();
			for (Region superRegion : superRegions) {
				jsonSuperRegions.put(superRegion.getId());
			}
			json.put("superRegions", jsonSuperRegions);
		}
		json.put("publishBulletins", isPublishBulletins());
		json.put("publishBlogs", isPublishBlogs());
		json.put("createCaamlV5", isCreateCaamlV5());
		json.put("createCaamlV6", isCreateCaamlV6());
		json.put("createJson", isCreateJson());
		json.put("createMaps", isCreateMaps());
		json.put("createPdf", isCreatePdf());
		json.put("createStaticWidget", isCreateStaticWidget());
		json.put("createSimpleHtml", isCreateSimpleHtml());
		json.put("sendEmails", isSendEmails());
		json.put("sendTelegramMessages", isSendTelegramMessages());
		json.put("sendPushNotifications", isSendPushNotifications());
		json.put("serverInstance", getServerInstance().toJSON());
		json.put("thumbnailMapConfig", getThumbnailMapConfig().toJSON());
		json.put("standardMapConfig", getStandardMapConfig().toJSON());
		json.put("overlayMapConfig", getOverlayMapConfig().toJSON());
		json.put("pdfColor", getPdfColor());
		json.put("pdfMapYAmPm", getPdfMapYAmPm());
		json.put("pdfMapYFd", getPdfMapYFd());
		json.put("pdfMapWidthAmPm", getPdfMapWidthAmPm());
		json.put("pdfMapWidthFd", getPdfMapWidthFd());
		json.put("pdfMapHeight", getPdfMapHeight());
		json.put("pdfFooterLogo", isPdfFooterLogo());
		json.put("pdfFooterLogoColorPath", getPdfFooterLogoColorPath());
		json.put("pdfFooterLogoBwPath", getPdfFooterLogoBwPath());
		json.put("staticWidgetSecondaryLogoColorPath", getStaticWidgetSecondaryLogoColorPath());
		json.put("mapXmax", getMapXmax());
		json.put("mapXmin", getMapXmin());
		json.put("mapYmax", getMapXmax());
		json.put("mapYmin", getMapXmin());
		json.put("simpleHtmlTemplateName", getSimpleHtmlTemplateName());
		json.put("geoDataDirectory", getGeoDataDirectory());
		json.put("mapLogoColorPath", getMapLogoColorPath());
		json.put("mapLogoBwPath", getMapLogoBwPath());
		json.put("logoPosition", this.mapLogoPosition.toString());
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
	public int hashCode() {
		return Objects.hash(id);
	}

	public static Region readRegion(final URL resource) throws IOException {
		final String string = Resources.toString(resource, StandardCharsets.UTF_8);
		return new Region(new JSONObject(string));
	}

}

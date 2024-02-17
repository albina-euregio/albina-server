package eu.albina.util;

import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Period;
import java.util.List;

/**
 * Returns URLs (descriptions) for various bulletin elements
 */
public interface LinkUtil {

	static String getWebsiteUrl(LanguageCode lang, Region region) {
		String url = lang.getBundleString("website.url", region);
		return url.replaceAll("/$", "");
	}

	static String getStaticContentUrl(LanguageCode lang, Region region) {
		String url = lang.getBundleString("website.static.url", region);
		return url.replaceAll("/$", "");
	}

	static String getSimpleHtmlUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String htmlDirectory = Paths.get(serverInstance.getHtmlDirectory()).getFileName().toString();
		return String.format("%s/%s", getStaticContentUrl(lang, region), htmlDirectory);
	}

	static String getMediaFileUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String mediaFileDirectory = Paths.get(serverInstance.getMediaPath()).getFileName().toString();
		return String.format("%s/%s/%s/%s", getStaticContentUrl(lang, region), mediaFileDirectory, region.getId(), lang);
	}

	static String getMapsUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String mapsDirectory = Paths.get(serverInstance.getMapsPath()).getFileName().toString();
		return String.format("%s/%s", getStaticContentUrl(lang, region), mapsDirectory);
	}

	static String getPdfUrl(LanguageCode lang, AvalancheReport avalancheReport) {
		String pdfDirectory = Paths.get(avalancheReport.getServerInstance().getPdfDirectory()).getFileName().toString();
		return String.format("%s/%s", getStaticContentUrl(lang, avalancheReport.getRegion()), pdfDirectory);
	}

	static String getBulletinUrl(AvalancheReport avalancheReport, LanguageCode lang) {
		String date = avalancheReport.getValidityDateString();
		return String.format("%s/bulletin/%s", getWebsiteUrl(lang, avalancheReport.getRegion()), date);
	}

	static String getPdfLink(AvalancheReport avalancheReport, LanguageCode lang) {
		String date = avalancheReport.getValidityDateString();
		String region = avalancheReport.getRegion().getId();
		return String.format("%s/%s/%s_%s_%s.pdf", getPdfUrl(lang, avalancheReport), date, date, region, lang);
	}

	static String getDangerPatternLink(LanguageCode lang, Region region, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			getWebsiteUrl(lang, region), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheProblemLink(LanguageCode lang, Region region,
											eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		return String.format("%s/education/avalanche-problems#%s",
			getWebsiteUrl(lang, region), avalancheProblem.toCaamlv6String());
	}

	static String getImprintLink(LanguageCode lang, Region region) {
		return String.format("%s/more/imprint", getWebsiteUrl(lang, region));
	}

	static String getExtFileMapDescription(LanguageCode lang, String type, String region) {
		String regionName = lang.getRegionName(region);
		String timeString = lang.getBundleString("daytime." + type);
		return MessageFormat.format(lang.getBundleString("ext-file.map.description"), regionName, timeString);
	}

	static String getExtFileOverlayDescription(LanguageCode lang, String type) {
		String timeString = lang.getBundleString("daytime." + type);
		return MessageFormat.format(lang.getBundleString("ext-file.overlay.description"), timeString);
	}

	static String getExtFileRegionsDescription(LanguageCode lang, String type) {
		String timeString = lang.getBundleString("daytime." + type);
		return MessageFormat.format(lang.getBundleString("ext-file.regions.description"), timeString);
	}

	static String getExtFilePdfDescription(LanguageCode lang, String region) {
		String regionName = lang.getRegionName(region);
		return "PDF " + regionName;
	}

	static String getSocialMediaAttachmentUrl(AvalancheReport avalancheReport, LanguageCode lang) {
		String validityDate = avalancheReport.getValidityDateString();
		String publicationTime = avalancheReport.getPublicationTimeString();
		Region region = avalancheReport.getRegion();
		ServerInstance serverInstance1 = avalancheReport.getServerInstance();
		return String.format("%s/%s/%s/%s",
			getMapsUrl(lang, region, serverInstance1), validityDate, publicationTime, MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false));
	}

	static String createHtmlLink(String text, String url) {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}

	static String getBulletinLink(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, Period offset, ServerInstance serverInstance) {
		return String.format("%s/%s/%s%s.html",
			getSimpleHtmlUrl(lang, region, serverInstance),
			AlbinaUtil.getValidityDateString(bulletins, offset),
			region != null && !region.getId().isEmpty() ? region.getId() + "_" : "",
			lang.toString());
	}
}

package eu.albina.util;

import eu.albina.controller.RegionController;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;

import java.nio.file.Paths;
import java.text.MessageFormat;
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
		return String.format("%s/%s/%s", getStaticContentUrl(lang, region), mediaFileDirectory, region.getId());
	}

	static String getMapsUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String mapsDirectory = Paths.get(serverInstance.getMapsPath()).getFileName().toString();
		return String.format("%s/%s", getStaticContentUrl(lang, region), mapsDirectory);
	}

	static String getPdfUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String pdfDirectory = Paths.get(serverInstance.getPdfDirectory()).getFileName().toString();
		return String.format("%s/%s", getStaticContentUrl(lang, region), pdfDirectory);
	}

	static String getAvalancheReportFullBlogUrl(LanguageCode lang, Region region) {
		return String.format("%s/blog/", getWebsiteUrl(lang, region));
	}

	static String getBulletinUrl(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return String.format("%s/bulletin/%s", getWebsiteUrl(lang, region), date);
	}

	static String getPdfLink(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, ServerInstance serverInstance) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return String.format("%s/%s/%s_%s_%s.pdf", getPdfUrl(lang, region, serverInstance), date, date, region.getId(), lang);
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
		String regionName = RegionController.getInstance().getRegionName(lang, region);
		String timeString = AlbinaUtil.getDaytimeString(lang, type);
		return MessageFormat.format(lang.getBundleString("ext-file.map.description"), regionName, timeString);
	}

	static String getExtFileOverlayDescription(LanguageCode lang, String type) {
		String timeString = AlbinaUtil.getDaytimeString(lang, type);
		return MessageFormat.format(lang.getBundleString("ext-file.overlay.description"), timeString);
	}

	static String getExtFileRegionsDescription(LanguageCode lang, String type) {
		String timeString = AlbinaUtil.getDaytimeString(lang, type);
		return MessageFormat.format(lang.getBundleString("ext-file.regions.description"), timeString);
	}

	static String getExtFilePdfDescription(LanguageCode lang, String region) {
		String regionName = RegionController.getInstance().getRegionName(lang, region);
		return "PDF " + regionName;
	}

	static String getSocialMediaAttachmentUrl(Region region, LanguageCode lang, List<AvalancheBulletin> bulletins, ServerInstance serverInstance) {
		String validityDate = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		return String.format("%s/%s/%s/%s",
			getMapsUrl(lang, region, serverInstance), validityDate, publicationTime, MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false));
	}

	static String createHtmlLink(String text, String url) {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}
}

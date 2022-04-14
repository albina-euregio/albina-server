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

	static String getWebsite(LanguageCode lang, Region region) {
		String url = lang.getBundleString("website.url", region);
		return url.replaceAll("/$", "");
	}

	static String getSimpleHtmlUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String htmlDirectory = Paths.get(serverInstance.getHtmlDirectory()).getFileName().toString();
		return String.format("%s/%s", getWebsite(lang, region), htmlDirectory);
	}

	static String getMapsUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String mapsDirectory = Paths.get(serverInstance.getMapsPath()).getFileName().toString();
		return String.format("%s/%s", getWebsite(lang, region), mapsDirectory);
	}

	static String getPdfUrl(LanguageCode lang, Region region, ServerInstance serverInstance) {
		String pdfDirectory = Paths.get(serverInstance.getPdfDirectory()).getFileName().toString();
		return String.format("%s/%s", getWebsite(lang, region), pdfDirectory);
	}

	static String getAvalancheReportFullBlogUrl(LanguageCode lang, Region region) {
		return String.format("%s/blog/", getWebsite(lang, region));
	}

	static String getBulletinUrl(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return String.format("%s/bulletin/%s", getWebsite(lang, region), date);
	}

	static String getPdfLink(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, ServerInstance serverInstance) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return String.format("%s/%s/%s_%s_%s.pdf", getPdfUrl(lang, region, serverInstance), date, date, region.getId(), lang);
	}

	static String getDangerPatternLink(LanguageCode lang, Region region, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			getWebsite(lang, region), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheSituationLink(LanguageCode lang, Region region,
											eu.albina.model.enumerations.AvalancheSituation avalancheSituation) {
		return String.format("%s/education/avalanche-problems#%s",
			getWebsite(lang, region), avalancheSituation.toCaamlv6String());
	}

	static String getImprintLink(LanguageCode lang, Region region) {
		return String.format("%s/more/imprint", getWebsite(lang, region));
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
}

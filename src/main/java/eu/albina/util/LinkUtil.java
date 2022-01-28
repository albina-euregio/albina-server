package eu.albina.util;

import com.google.common.base.Strings;

import eu.albina.controller.ServerInstanceController;
import eu.albina.map.DaytimeDependency;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.LanguageCode;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

/**
 * Returns URLs (descriptions) for various bulletin elements
 */
public interface LinkUtil {

	static String getWebsite(LanguageCode lang) {
		String url = GlobalVariables.serverWebsiteUrl;
		if (Strings.isNullOrEmpty(url)) {
			url = lang.getBundleString("avalanche-report.url");
		}
		return url;
	}

	static String getSimpleHtmlUrl(LanguageCode lang) {
		String htmlDirectory = Paths.get(ServerInstanceController.getInstance().getLocalServerInstance().getHtmlDirectory()).getFileName().toString();
		return String.format("%s/%s", getWebsite(lang), htmlDirectory);
	}

	static String getMapsUrl(LanguageCode lang) {
		String mapsDirectory = Paths.get(ServerInstanceController.getInstance().getLocalServerInstance().getMapsPath()).getFileName().toString();
		return String.format("%s/%s", getWebsite(lang), mapsDirectory);
	}

	static String getPdfUrl(LanguageCode lang) {
		String pdfDirectory = Paths.get(ServerInstanceController.getInstance().getLocalServerInstance().getPdfDirectory()).getFileName().toString();
		return String.format("%s/%s", getWebsite(lang), pdfDirectory);
	}

	static String getAvalancheReportFullBlogUrl(LanguageCode lang) {
		return String.format("%s/blog/", getWebsite(lang));
	}

	static String getBulletinUrl(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return String.format("%s/bulletin/%s", getWebsite(lang), date);
	}

	static String getPdfLink(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return String.format("%s/%s/%s_%s_%s.pdf", getPdfUrl(lang), date, date, region.getId(), lang);
	}

	static String getDangerPatternLink(LanguageCode lang, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			getWebsite(lang), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheSituationLink(LanguageCode lang,
											eu.albina.model.enumerations.AvalancheSituation avalancheSituation) {
		return String.format("%s/education/avalanche-problems#%s",
			getWebsite(lang), avalancheSituation.toCaamlv6String());
	}

	static String getImprintLink(LanguageCode lang) {
		return String.format("%s/more/imprint", getWebsite(lang));
	}

	static String getExtFileMapDescription(LanguageCode lang, String type, String region) {
		String regionName = AlbinaUtil.getRegionName(lang, region);
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
		String regionName = AlbinaUtil.getRegionName(lang, region);
		return "PDF " + regionName;
	}

	static String getSocialMediaAttachmentUrl(LanguageCode lang, List<AvalancheBulletin> bulletins) {
		String validityDate = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		return String.format("%s/%s/%s/%s",
			getMapsUrl(lang), validityDate, publicationTime, MapUtil.getOverviewMapFilename("", DaytimeDependency.fd, false));
	}
}

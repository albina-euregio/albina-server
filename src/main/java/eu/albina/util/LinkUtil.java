package eu.albina.util;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Period;

import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;

/**
 * Returns URLs (descriptions) for various bulletin elements
 */
public interface LinkUtil {

	static String getBulletinUrl(AvalancheReport avalancheReport, LanguageCode lang) {
		String date = avalancheReport.getValidityDateString();
		Region region = avalancheReport.getRegion();
		return String.format(region.getWebsiteUrlWithDate(lang), date);
	}

	static String getPdfLink(AvalancheReport avalancheReport, LanguageCode lang) {
		String date = avalancheReport.getValidityDateString();
		Region region = avalancheReport.getRegion();
		return String.format("%s/%s/%s_%s_%s.pdf", region.getPdfUrl(lang), date, date, region.getId(), lang);
	}

	static String getDangerPatternLink(LanguageCode lang, Region region, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			region.getWebsiteUrl(lang), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheProblemLink(LanguageCode lang, Region region,
											eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		return String.format("%s/education/avalanche-problems#%s",
			region.getWebsiteUrl(lang), avalancheProblem.toCaamlv6String());
	}

	static String getSocialMediaAttachmentUrl(AvalancheReport avalancheReport, LanguageCode lang) {
		String validityDate = avalancheReport.getValidityDateString();
		String publicationTime = avalancheReport.getPublicationTimeString();
		Region region = avalancheReport.getRegion();
		return String.format("%s/%s/%s/%s",
			region.getMapsUrl(lang), validityDate, publicationTime, MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false));
	}

	static String createHtmlLink(String text, String url) {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}

	static String getBulletinLink(AvalancheReport avalancheReport, LanguageCode lang, Region region, Period offset, ServerInstance serverInstance) {
		return String.format("%s/%s/%s%s.html",
			region.getSimpleHtmlUrl(lang),
			avalancheReport.getValidityDateString(offset),
			region != null && !region.getId().isEmpty() ? region.getId() + "_" : "",
			lang.toString());
	}
}

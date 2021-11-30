package eu.albina.util;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.LanguageCode;

import java.text.MessageFormat;
import java.util.List;

/**
 * Returns URLs (descriptions) for various bulletin elements
 */
public interface LinkUtil {

	static String getAvalancheReportSimpleBaseUrl(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + GlobalVariables.avalancheReportSimpleUrl;
	}

	static String getAvalancheReportFullBlogUrl(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + GlobalVariables.avalancheReportBlogUrl;
	}

	static String getBulletinUrl(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		return lang.getBundleString("avalanche-report.url") + GlobalVariables.avalancheReportBulletinUrl + date;
	}

	// REGION
	static String getPdfLink(List<AvalancheBulletin> bulletins, LanguageCode lang, String region) {
		String date = AlbinaUtil.getValidityDateString(bulletins);
		StringBuilder sb = new StringBuilder();
		sb.append(lang.getBundleString("avalanche-report.url"));
		sb.append(GlobalVariables.avalancheReportFilesUrl);
		sb.append(date);
		sb.append("/");
		sb.append(date);
		sb.append("_");
		sb.append(region);
		sb.append("_");
		sb.append(lang.toString());
		sb.append(".pdf");
		return sb.toString();
	}

	static String getDangerPatternLink(LanguageCode lang, DangerPattern dangerPattern) {
		StringBuilder sb = new StringBuilder();
		sb.append(lang.getBundleString("avalanche-report.url"));
		sb.append("/education/danger-patterns#");
		sb.append(DangerPattern.getCAAMLv6String(dangerPattern));
		return sb.toString();
	}

	static String getAvalancheSituationLink(LanguageCode lang,
											eu.albina.model.enumerations.AvalancheSituation avalancheSituation) {
		StringBuilder sb = new StringBuilder();
		sb.append(lang.getBundleString("avalanche-report.url"));
		sb.append("/education/avalanche-problems#");
		sb.append(avalancheSituation.toCaamlv6String());
		return sb.toString();
	}

	static String getImprintLink(LanguageCode lang) {
		return lang.getBundleString("avalanche-report.url") + "/imprint";
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

	static String getSocialMediaAttachmentUrl(List<AvalancheBulletin> bulletins) {
		String validityDate = AlbinaUtil.getValidityDateString(bulletins);
		String publicationTime = AlbinaUtil.getPublicationTime(bulletins);
		return getServerMainUrl() + GlobalVariables.avalancheReportFilesUrl
			+ validityDate + "/" + publicationTime + "/"
			+ AlbinaUtil.getRegionOverviewMapFilename("", "jpg");
	}

	static String getServerMainUrl() {
		return "https://avalanche.report";
	}
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.enumerations.Tendency;

public record EmailUtil(AvalancheReport avalancheReport, LanguageCode lang) {

	public static String createBulletinEmailHtml(AvalancheReport avalancheReport, LanguageCode lang) {
		return new EmailUtil(avalancheReport, lang).createBulletinEmailHtml();
	}

	static String getDangerPatternLink(LanguageCode lang, Region region, DangerPattern dangerPattern) {
		return String.format("%s/education/danger-patterns#%s",
			region.getWebsiteUrl(lang), DangerPattern.getCAAMLv6String(dangerPattern));
	}

	static String getAvalancheProblemLink(LanguageCode lang, Region region,
										  eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		return String.format("%s/education/avalanche-problems#%s",
			region.getWebsiteUrl(lang), avalancheProblem.toStringId());
	}

	String createBulletinEmailHtml() {
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();
		String color = region.getEmailColor();
		String ci = serverImagesUrl + region.getImageColorbarColorPath();
		String website = region.getWebsiteUrlWithDate(lang, avalancheReport);
		String mapsUrl = avalancheReport.getMapsUrl();
		List<AvalancheBulletin> bulletins = avalancheReport.getBulletins();
		boolean daytime = avalancheReport.hasDaytimeDependency();

		String publicationDate = avalancheReport.getPublicationDate(lang);
		String publishedAt = publicationDate.isEmpty() ? "" : lang.getBundleString("published");
		String headline = avalancheReport.getStatus() == BulletinStatus.republished
			? lang.getBundleString("headline.update")
			: lang.getBundleString("headline");
		String textAm = daytime ? lang.getBundleString("valid-time-period.earlier") : "";
		String textPm = daytime ? lang.getBundleString("valid-time-period.later") : "";
		String overview = daytime
			? mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, false)
			: mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, false);
		String overviewPM = daytime
			? mapsUrl + "/" + MapUtil.getOverviewMapFilename(region, DaytimeDependency.pm, false)
			: serverImagesUrl + "/empty.png";
		String widthPM = daytime ? "width=\"600\" " : "";
		String dangerLevel5Style = "background=\"" + serverImagesUrl + "bg_checkered.png"
			+ "\" height=\"10\" width=\"75\" bgcolor=\"#FF0000\"";

		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);

		// head
		pw.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		pw.print("<html>");
		pw.print("<head>");
		pw.print("<meta name=\"viewport\" content=\"width=device-width\"/>");
		pw.print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
		pw.format("<title>%s</title>", lang.getBundleString("headline"));
		pw.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/avalanche-report.css\" >");
		pw.print("<style>/* ------------------------------------- GLOBAL ------------------------------------- */*{margin: 0;padding: 0;text-decoration: none;}*{font-family: \"Open Sans\", sans-serif;color: #565f61;}img{max-width: 100%;}.collapse{margin: 0;padding: 0;}body{-webkit-font-smoothing:antialiased; -webkit-text-size-adjust:none; width: 100%!important; height: 100%;}@media screen and (min-width: 700px){.width-fix{width:100%!important;min-width:100%;}}/* ------------------------------------- ELEMENTS ------------------------------------- */a{color: #2BA6CB;}.btn{text-decoration: none;color: #565f61;/*background-color: #" + color + ";*/padding: 0px 10px;font-weight: bold;margin-right: 10px;text-align: center;cursor: pointer;display: inline-block;}p.callout{padding:15px;background-color:#ECF8FF;margin-bottom: 15px;}.callout a{font-weight:bold;color: #2BA6CB;}table.social{/* padding:15px; */background-color: #ebebeb;}.social .soc-btn{padding: 3px 7px;font-size: 12px;margin-bottom: 10px;text-decoration: none;color: #FFF;font-weight: bold;display: block;text-align: center;}a.fb{background-color: #3B5998!important;}a.tw{background-color: #1daced!important;}a.gp{background-color: #DB4A39!important;}a.ms{background-color: #000!important;}.sidebar .soc-btn{display: block;width: 100%;}/* ------------------------------------- HEADER ------------------------------------- */table.head-wrap{width: 100%;border-bottom: 1px solid #99daff;}table.head-ci{width: 100%;padding: 0px;margin: 0px;}.header.container table td.logo{padding: 15px;}.header.container table td.label{padding: 15px; padding-left:0px;}/* ------------------------------------- BODY ------------------------------------- */table.body-wrap{width: 100%;}/* ------------------------------------- FOOTER ------------------------------------- */table.footer-wrap{width: 100%;clear:both!important;border-top: 1px solid #99daff;padding-top: 15px;}.footer-wrap .container td.content p{border-top: 1px solid rgb(215,215,215);padding-top: 15px;}.footer-wrap .container td.content p{font-size: 10px;font-weight: bold;}.social-media-button{padding-left: 5px;padding-right: 5px;}/* ------------------------------------- TYPOGRAPHY ------------------------------------- */h1,h2,h3,h4,h5,h6{font-family: \"Open Sans\", sans-serif;line-height: 1;margin-bottom: 15px;color: #565f61;}h1, h2{color: #" + color + ";}h1 small, h2 small, h3 small, h4 small, h5 small, h6 small{font-size: 60%;color: #565f61;line-height: 0;text-transform: none;}h1{font-weight:200; font-size: 40px;}h2{font-weight:bold; font-size: 24px;}h3{font-weight:500; font-size: 27px;}h4{font-weight:500; font-size: 23px;}h5{font-weight:900; font-size: 14px;}h6{font-weight:900; font-size: 14px; text-transform: uppercase; color:#444;}.collapse{margin:0!important;}p, ul{font-family: \"Open Sans\", sans-serif;margin-bottom: 10px; font-weight: normal; font-size: 14px; line-height: 1.6;}p.lead{font-size: 24px; margin-bottom: 0px;}p.last{margin-bottom:0px;}ul li{margin-left:5px;list-style-position: inside;}/* ------------------------------------- SIDEBAR ------------------------------------- */ul.sidebar{background:#ebebeb;display:block;list-style-type: none;}ul.sidebar li{display: block; margin:0;}ul.sidebar li a{text-decoration:none;color: #666;padding:10px 16px;/* font-weight:bold; */margin-right:10px;/* text-align:center; */cursor:pointer;border-bottom: 1px solid #777777;border-top: 1px solid #FFFFFF;display:block;margin:0;}ul.sidebar li a.last{border-bottom-width:0px;}ul.sidebar li a h1,ul.sidebar li a h2,ul.sidebar li a h3,ul.sidebar li a h4,ul.sidebar li a h5,ul.sidebar li a h6,ul.sidebar li a p{margin-bottom:0!important;}/* --------------------------------------------------- RESPONSIVENESSNuke it from orbit. It's the only way to be sure. ------------------------------------------------------ *//* Set a max-width, and make it display as block so it will automatically stretch to that width, but will also shrink down on a phone or something */.container{display:block!important;max-width:800px!important;margin:0 auto!important; /* makes it centered */clear:both!important;}/* This should also be a block element, so that it will fill 100% of the .container */.content{padding:0px;max-width:800px;margin:0 auto;display:block;}/* Let's make sure tables in the content area are 100% wide */.content table{width: 100%;}/* Odds and ends */.column{width: 400px;float:left;}.column tr td{padding: 15px;}.column-wrap{padding:0!important; margin:0 auto; max-width:800px!important;}.column table{width:100%;}.social .column{width: 280px;min-width: 279px;float:left;}/* Be sure to place a .clear element after each set of columns, just to be safe */.clear{display: block; clear: both;}/* ------------------------------------------- PHONEFor clients that support media queries.Nothing fancy. -------------------------------------------- */@media only screen and (max-width: 800px){div[class=\"column\"]{width: auto!important; float:none!important;}table.social div[class=\"column\"]{width:auto!important;}}.map-daytime-text{color: #565f61;font-size: 22px;margin-bottom: 5px;text-align: center;}.daytime-text{color: #565f61;font-size: 22px;}.daytime-text-div{padding-top: 5px;}.snowpack{border-left: 5px solid #" + color + ";padding-left: 10px;}.bulletin-content{margin-top: 15px;margin-bottom: 40px;}.tendency{padding-left: 15px;padding-right: 15px;vertical-align: middle;text-align: center;}.danger-patterns{}.danger-pattern{font-size: 12px;color: #565f61;border: 1px solid #565f61; border-radius: 15px;padding-left: 10px;padding-right: 10px;padding-top: 2px;padding-bottom: 2px;margin-right: 5px;display: inline-block;background-color: #FFFFFF;}.detail-map{max-width: 150px;}.tendency-symbol{display: inline-block;max-width: 40px;margin-left: 5px;}.avalanche-problem{max-width: 50px;margin: 5px;}.avalanche-problem-aspects-div{padding: 0;margin: 0;max-height:0;max-width:0;overflow: visible;}.avalanche-problem-aspects-outer-div{height: 60px;width: 60px;}.avalanche-problem-aspects-img{max-width: 60px;max-height: 60px;display: inline-block;}.avalanche-problem-elevation-img{max-width: 80px;max-height: 48px;display: inline-block;}.avalanche-problem-aspects{width: 60px;height: 60px;}.mountain{border-right: 1px solid #e6eef2;}.danger-rating-number{text-align: center;margin-bottom: 0;font-size: 12px;}.danger-rating-text{text-align: center;margin-bottom: 0;font-size: 12px;}</style>");
		pw.print("</head>");
		pw.print("<body bgcolor=\"#FFFFFF\" topmargin=\"0\" leftmargin=\"0\" marginheight=\"0\" marginwidth=\"0\">");

		// header
		pw.print("<table class=\"head-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<img height=\"4\" style=\"width: 100%;\" src=\"" + ci + "\"/>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td class=\"header container\" style=\"padding: 15px;\">");
		pw.print("<div class=\"content\">");
		pw.print("<table bgcolor=\"\" >");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p class=\"lead\">%s</p>", headline);
		pw.format("<h2 style=\"margin-bottom: 5px\">%s</h2>", avalancheReport.getDate(lang));
		pw.format("<p style=\"margin-bottom: 0px; font-size: 12px\">%s<b>%s</b>", publishedAt, publicationDate);
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td align=\"right\">");
		pw.format("<a class=\"btn\" href=\"%s\">", website);
		pw.format("<img width=\"110\" src=\"%s\"/>", serverImagesUrl + region.getLogoPath());
		pw.print("</a>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// overview maps
		pw.print("<table align=\"center\" class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" bgcolor=\"#FFFFFF\">");
		pw.print("<div class=\"content\">");
		pw.print("<table style=\"padding: 15px 0;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 class=\"map-daytime-text\">%s</h2>", textAm);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p style=\"margin-bottom: 0px; text-align: center;\">");
		pw.format("<a href=\"%s\">", website);
		pw.format("<img width=\"600\" style=\"max-width: 600px;\" src=\"%s\"/>", overview);
		pw.print("</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 class=\"map-daytime-text\" style=\"margin-top: 15px;\">%s</h2>", textPm);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p style=\"margin-bottom: 0px; text-align: center;\">");
		pw.format("<a href=\"%s\">", website);
		pw.format("<img %sstyle=\"max-width: 600px;\" src=\"%s\"/>", widthPM, overviewPM);
		pw.print("</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// danger scale
		pw.print("<table align=\"center\" style=\"width: auto; margin-left: auto; margin-right: auto; text-align: center; border-spacing: 0px;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#CCFF66\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#FFFF00\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#FF9900\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table height=\"10\" width=\"75\" bgcolor=\"#FF0000\">");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<table %s>", dangerLevel5Style);
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>1</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>2</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>3</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>4</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<p class=\"danger-rating-number\">");
		pw.print("<b>5</b>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.low.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.moderate.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.considerable.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.high.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<p class=\"danger-rating-text\">%s</p>", DangerRating.very_high.toString(lang.getLocale(), false));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// general headline
		avalancheReport.getGeneralHeadline(lang).ifPresent(generalHeadline -> {
			pw.print("<table bgcolor=\"\" >");
			pw.print("<tr>");
			pw.print("<td>");
			pw.format("<h2 style=\"margin: 24px 0\">%s</h2>", generalHeadline);
			pw.print("</td>");
			pw.print("</tr>");
			pw.print("</table>");
		});

		for (AvalancheBulletin bulletin : bulletins) {
			appendBulletin(pw, bulletin, color);
		}

		// footer
		pw.print("<table class=\"footer-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\">");
		pw.print("<div class=\"content\">");
		pw.print("<table>");
		pw.print("<tr>");
		pw.print("<td align=\"center\">");
		pw.print("<p>");
		pw.format("<a href=\"%s\">%s</a>", region.getImprintLink(lang), lang.getBundleString("email.imprint"));
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td class=\"container\">");
		pw.print("<div class=\"content\">");
		pw.print("<table>");
		pw.print("<tr>");
		pw.print("<td align=\"center\">");
		pw.print("<p>");
		pw.print("<a href=\"{%link_unsubscribe}\">" + lang.getBundleString("email.unsubscribe") + "</a>");
		pw.print("</p>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("<img height=\"4px\" style=\"width: 100%;\" src=\"" + ci + "\"/>");
		pw.print("</body>");
		pw.print("</html>");

		pw.flush();
		return out.toString();
	}

	private void appendBulletin(PrintWriter pw, AvalancheBulletin bulletin, String color) {
		Region region = avalancheReport.getRegion();
		String mapsUrl = avalancheReport.getMapsUrl();
		DangerRating highestDangerRating = bulletin.getHighestDangerRating();
		boolean bulletinDaytime = bulletin.isHasDaytimeDependency();

		String textam = bulletinDaytime ? lang.getBundleString("valid-time-period.earlier") : "";
		String textpm = bulletinDaytime ? lang.getBundleString("valid-time-period.later") : "";
		String stylepm = getPMStyle(bulletinDaytime);
		String stylepmtable = getPMStyleTable(bulletinDaytime);

		AvalancheBulletinDaytimeDescription forenoon = bulletin.getForenoon();
		// when there is no daytime dependency, the afternoon block reuses the forenoon description and its map
		AvalancheBulletinDaytimeDescription afternoon = bulletinDaytime ? bulletin.getAfternoon() : bulletin.getForenoon();
		String mapAm = mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.am, false, MapImageFormat.jpg);
		String mapPm = bulletinDaytime
			? mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.pm, false, MapImageFormat.jpg)
			: mapsUrl + "/" + MapUtil.filename(region, bulletin, DaytimeDependency.am, false, MapImageFormat.jpg);

		// tendency
		String tendencyText = bulletin.getTendency() == null ? "" : bulletin.getTendency().toString(lang.getLocale());
		String tendencySymbol;
		String tendencyDate;
		String serverImagesUrl = region.getServerImagesUrl();
		if (bulletin.getTendency() == Tendency.decreasing) {
			tendencySymbol = serverImagesUrl + "tendency/tendency_decreasing_blue.png";
			tendencyDate = avalancheReport.getTendencyDate(lang);
		} else if (bulletin.getTendency() == Tendency.steady) {
			tendencySymbol = serverImagesUrl + "tendency/tendency_steady_blue.png";
			tendencyDate = avalancheReport.getTendencyDate(lang);
		} else if (bulletin.getTendency() == Tendency.increasing) {
			tendencySymbol = serverImagesUrl + "tendency/tendency_increasing_blue.png";
			tendencyDate = avalancheReport.getTendencyDate(lang);
		} else {
			tendencySymbol = serverImagesUrl + "tendency/empty.png";
			tendencyDate = "";
		}

		pw.print("<table class=\"body-wrap\" bgcolor=\"#FFFFFF\">");
		pw.print("<tr>");
		pw.print("<td class=\"container\" align=\"\" bgcolor=\"#FFFFFF\">");
		pw.print("<div class=\"content bulletin-content\">");
		pw.print("<table style=\"border-spacing: 0px;\">");
		pw.print("<tr>");
		pw.format("<td %s>", getDangerRatingColorStyle(highestDangerRating, region));
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table style=\"border-spacing: 0px;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h2 %s>%s</h2>", getHeadlineStyle(highestDangerRating), highestDangerRating.toString(lang.getLocale(), true));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// forenoon / all-day
		pw.print("<table style=\"margin-top: 10px; padding-left: 15px;\">");
		pw.print("<tr>");
		pw.print("<td class=\"daytime-text-div\">");
		pw.format("<h2 class=\"daytime-text\">%s</h2>", textam);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td style=\"width: 150px; padding-right: 10px;\">");
		pw.format("<img width=\"150\" class=\"detail-map\" src=\"%s\"/>", mapAm);
		pw.print("</td>");
		pw.print("<td>");
		pw.print("<table style=\"border-bottom: 1px solid #e6eef2; padding-bottom: 5px;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<table style=\"width: 0;\">");
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<div style=\"height: 48px;\">");
		pw.print("<div style=\"height: 48px; width: 60px; margin-right: 10px;\">");
		pw.format("<img height=\"48\" width=\"60\" style=\"display: inline-block; margin-bottom: 10px;\" src=\"%s\"/>", dangerRatingSymbol(forenoon));
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td class=\"mountain\">");
		pw.print("<div style=\"height: 48px;\">");
		pw.print("<div style=\"height: 48px; margin-right: 10px;\">");
		pw.print("<p style=\"height: 48px; display: inline-block; font-size: 12px; padding-top: 18px;\">");
		pw.format("<b>%s</b>", dangerRatingElevation(forenoon));
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td class=\"tendency\">");
		pw.print("<table>");
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<p style=\"text-align: left; font-weight: 900; margin-bottom: 10px;\">%s</p>", tendencyText);
		pw.format("<p style=\"text-align: left; margin-bottom: 0;\">%s</p>", tendencyDate);
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<img class=\"tendency-symbol\" src=\"%s\"/>", tendencySymbol);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// forenoon avalanche problems
		appendAvalancheProblem(pw, forenoon.getAvalancheProblem1(), true, "");
		appendAvalancheProblem(pw, forenoon.getAvalancheProblem2(), false, "");
		appendAvalancheProblem(pw, forenoon.getAvalancheProblem3(), false, "");
		appendAvalancheProblem(pw, forenoon.getAvalancheProblem4(), false, "");
		appendAvalancheProblem(pw, forenoon.getAvalancheProblem5(), false, "");

		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// afternoon
		pw.format("<div %s>", stylepm);
		pw.format("<table style=\"padding-left: 15px;\" %s>", stylepmtable);
		pw.print("<tr>");
		pw.print("<td class=\"daytime-text-div\">");
		pw.format("<h2 class=\"daytime-text\">%s</h2>", textpm);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("<tr>");
		pw.print("<td style=\"width: 150px;\">");
		pw.format("<img width=\"150\" class=\"detail-map\" src=\"%s\"/>", mapPm);
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<table style=\"border-bottom: 1px solid #e6eef2; padding-bottom: 5px;\" %s>", stylepmtable);
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<table style=\"width: 0;\" %s>", stylepmtable);
		pw.print("<tr>");
		pw.print("<td>");
		pw.print("<div style=\"height: 48px;\">");
		pw.print("<div style=\"height: 48px; width: 60px; margin-right: 10px;\">");
		pw.format("<img height=\"48\" width=\"60\" style=\"display: inline-block; margin-bottom: 10px;\" src=\"%s\"/>", dangerRatingSymbol(afternoon));
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td class=\"mountain\">");
		pw.print("<div style=\"height: 48px;\">");
		pw.print("<div style=\"height: 48px; margin-right: 10px;\">");
		pw.print("<p style=\"height: 48px; display: inline-block; font-size: 12px; padding-top: 18px;\">");
		pw.format("<b>%s</b>", dangerRatingElevation(afternoon));
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td class=\"tendency\">");
		pw.format("<table %s>", stylepmtable);
		pw.print("<tr>");
		pw.print("<td>");
		pw.format("<h5 style=\"text-align: left; margin-bottom: 10px;\">%s</h5>", tendencyText);
		pw.format("<h5 style=\"text-align: left; font-weight: 100;\">%s</h5>", tendencyDate);
		pw.print("</td>");
		pw.print("<td>");
		pw.format("<img class=\"tendency-symbol\" src=\"%s\"/>", tendencySymbol);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// afternoon avalanche problems
		String pmTableAttr = " " + stylepmtable;
		appendAvalancheProblem(pw, afternoon.getAvalancheProblem1(), true, pmTableAttr);
		appendAvalancheProblem(pw, afternoon.getAvalancheProblem2(), false, pmTableAttr);
		appendAvalancheProblem(pw, afternoon.getAvalancheProblem3(), false, pmTableAttr);
		appendAvalancheProblem(pw, afternoon.getAvalancheProblem4(), false, pmTableAttr);
		appendAvalancheProblem(pw, afternoon.getAvalancheProblem5(), false, pmTableAttr);

		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("<table style=\"padding-left: 15px;\">");
		pw.print("<tr>");
		pw.print("<td style=\"vertical-align: top; padding-top: 10px;\">");
		pw.format("<h4>%s</h4>", bulletin.getAvActivityHighlightsIn(lang).orElse(""));
		pw.format("<p class=\"\">%s</p>", bulletin.getAvActivityCommentIn(lang).orElse(""));
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");

		// snowpack structure / danger patterns / synopsis / tendency
		DangerPattern dangerPattern1 = bulletin.getDangerPattern1();
		DangerPattern dangerPattern2 = bulletin.getDangerPattern2();
		Optional<String> snowpackStructureComment = bulletin.getSnowpackStructureCommentIn(lang);
		Optional<String> snowpackStructureHighlights = bulletin.getSnowpackStructureHighlightsIn(lang);
		Optional<String> tendencyComment = bulletin.getTendencyCommentIn(lang);
		Optional<String> synopsisComment = bulletin.getSynopsisCommentIn(lang);
		boolean hasSnowpackSection = dangerPattern1 != null || dangerPattern2 != null
			|| snowpackStructureComment.isPresent() || snowpackStructureHighlights.isPresent()
			|| tendencyComment.isPresent();
		boolean hasStructure = dangerPattern1 != null || dangerPattern2 != null
			|| snowpackStructureComment.isPresent() || snowpackStructureHighlights.isPresent();
		boolean hasDangerPatterns = dangerPattern1 != null || dangerPattern2 != null;

		String snowpackStructureHeadline = hasStructure ? lang.getBundleString("headline.snowpack") : "";
		String snowpackStructureCommentText = hasStructure ? snowpackStructureComment.orElse("") : "";
		String dangerPatternsHeadline = hasStructure && hasDangerPatterns ? lang.getBundleString("headline.danger-patterns") : "";
		String dangerPattern1Text = hasStructure && hasDangerPatterns && dangerPattern1 != null ? dangerPattern1.toString(lang.getLocale()) : "";
		String dangerPatternLink1 = hasStructure && hasDangerPatterns && dangerPattern1 != null ? getDangerPatternLink(lang, region, dangerPattern1) : "";
		String dangerPatternStyle1 = getDangerPatternStyle(hasStructure && hasDangerPatterns && dangerPattern1 != null);
		String dangerPattern2Text = hasStructure && hasDangerPatterns && dangerPattern2 != null ? dangerPattern2.toString(lang.getLocale()) : "";
		String dangerPatternLink2 = hasStructure && hasDangerPatterns && dangerPattern2 != null ? getDangerPatternLink(lang, region, dangerPattern2) : "";
		String dangerPatternStyle2 = getDangerPatternStyle(hasStructure && hasDangerPatterns && dangerPattern2 != null);
		String tendencyHeadline = tendencyComment.isPresent() ? lang.getBundleString("headline.tendency") : "";
		String tendencyCommentText = tendencyComment.orElse("");

		pw.format("<table %s>", getSnowpackStyle(hasSnowpackSection));
		pw.print("<tr>");
		pw.print("<td style=\"background-color: #" + color + "; width: 10px; min-width: 10px; height: 100%;\"></td>");
		pw.print("<td style=\"vertical-align: top; padding: 15px;\">");
		pw.format("<h4 style=\"padding-top: 5px;\">%s</h4>", snowpackStructureHeadline);
		pw.format("<h5 style=\"margin-right: 5px; display: inline-block\">%s</h5>", dangerPatternsHeadline);
		pw.format("<a href=\"%s\" target=\"_blank\">", dangerPatternLink1);
		pw.format("<p %s>%s</p>", dangerPatternStyle1, dangerPattern1Text);
		pw.print("</a>");
		pw.format("<a href=\"%s\" target=\"_blank\">", dangerPatternLink2);
		pw.format("<p %s>%s</p>", dangerPatternStyle2, dangerPattern2Text);
		pw.print("</a>");
		pw.format("<p class=\"\">%s</p>", snowpackStructureCommentText);
		if (hasSnowpackSection && synopsisComment.isPresent()) {
			pw.format("<h4 style=\"padding-top: 15px;\">%s</h4>", lang.getBundleString("headline.synopsis"));
			pw.format("<p class=\"\">%s</p>", synopsisComment.get());
		}
		pw.format("<h4 style=\"padding-top: 15px;\">%s</h4>", tendencyHeadline);
		pw.format("<p class=\"\">%s</p>", tendencyCommentText);
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
	}

	private void appendAvalancheProblem(PrintWriter pw, AvalancheProblem problem, boolean first, String tableExtraAttr) {
		if (problem == null || problem.getAvalancheProblem() == null) {
			return;
		}
		Region region = avalancheReport.getRegion();
		String serverImagesUrl = region.getServerImagesUrl();

		String symbol = serverImagesUrl + "avalanche_problems/color/" + problem.getAvalancheProblem().toStringId() + ".png";
		String text = problem.getAvalancheProblem().toString(lang.getLocale());
		String link = getAvalancheProblemLink(lang, region, problem.getAvalancheProblem());
		String aspects = serverImagesUrl + Aspect.getSymbolPath(problem.getAspects(), false);

		String elevationSymbol;
		String limitAbove;
		String limitBelow;
		if (problem.getTreelineHigh() || problem.getElevationHigh() > 0) {
			if (problem.getTreelineLow() || problem.getElevationLow() > 0) {
				// elevation high and low set
				elevationSymbol = serverImagesUrl + "elevation/color/levels_middle_two.png";
				limitAbove = problem.getTreelineLow()
					? lang.getBundleString("elevation.treeline.capitalized")
					: problem.getElevationLow() + "m";
				limitBelow = problem.getTreelineHigh()
					? lang.getBundleString("elevation.treeline.capitalized")
					: problem.getElevationHigh() + "m";
			} else {
				// elevation high set
				elevationSymbol = serverImagesUrl + "elevation/color/levels_below.png";
				limitAbove = "";
				limitBelow = problem.getTreelineHigh()
					? lang.getBundleString("elevation.treeline.capitalized")
					: problem.getElevationHigh() + "m";
			}
		} else if (problem.getTreelineLow() || problem.getElevationLow() > 0) {
			// elevation low set
			elevationSymbol = serverImagesUrl + "elevation/color/levels_above.png";
			limitAbove = problem.getTreelineLow()
				? lang.getBundleString("elevation.treeline.capitalized")
				: problem.getElevationLow() + "m";
			limitBelow = "";
		} else {
			// no elevation set
			elevationSymbol = serverImagesUrl + "elevation/color/levels_all.png";
			limitAbove = "";
			limitBelow = "";
		}

		String margin = first ? "margin: 5px 5px 0 5px" : "margin-left: 5px; margin-top: 0px";
		pw.format("<table style=\"%s; width: 0;\"%s>", margin, tableExtraAttr);
		pw.print("<tr>");
		pw.print("<td style=\"margin: 0 5px; width: 70px; text-align: center;\">");
		pw.format("<a href=\"%s\" target=\"_blank\">", link);
		pw.format("<img width=\"50\" class=\"avalanche-problem\" src=\"%s\"/>", symbol);
		pw.print("</a>");
		pw.format("<p style=\"margin-bottom: 0px; font-size: 12px; line-height: 1.0;\">%s</p>", text);
		pw.print("</td>");
		pw.print("<td style=\"margin: 0 5px; width: 70px;\">");
		pw.print("<div class=\"avalanche-problem-aspects-outer-div\">");
		pw.print("<div class=\"avalanche-problem-aspects-div\">");
		pw.print("<div class=\"avalanche-problem-aspects\">");
		pw.format("<img width=\"60\" class=\"avalanche-problem-aspects-img\" src=\"%s\"/>", aspects);
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("<td style=\"width: 100px; margin: 0 5px;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.print("<div style=\"max-height: 0; max-width: 0; overflow: visible;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.format("<img height=\"48\" class=\"avalanche-problem-elevation-img\" src=\"%s\"/>", elevationSymbol);
		pw.print("</div>");
		pw.print("</div>");
		pw.print("<div style=\"max-height: 0; max-width: 0; overflow: visible;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.print("<p style=\"width: 100px; height: 48px; display: inline-block; font-size: 12px; margin-top: 24px; margin-left: 68px;\">");
		pw.format("<b>%s</b>", limitAbove);
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("<div style=\"max-height: 0; max-width: 0; overflow: visible;\">");
		pw.print("<div style=\"width: 100px; height: 48px;\">");
		pw.print("<p style=\"width: 100px; height: 48px; display: inline-block; font-size: 12px; margin-top: 7px; margin-left: 68px;\">");
		pw.format("<b>%s</b>", limitBelow);
		pw.print("</p>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</div>");
		pw.print("</td>");
		pw.print("</tr>");
		pw.print("</table>");
	}

	private String dangerRatingSymbol(AvalancheBulletinDaytimeDescription daytimeBulletin) {
		String serverImagesUrl = avalancheReport.getRegion().getServerImagesUrl();
		if ((daytimeBulletin.dangerRating(false) == null
				|| daytimeBulletin.dangerRating(false) == DangerRating.missing
				|| daytimeBulletin.dangerRating(false) == DangerRating.no_rating)
				&& (daytimeBulletin.dangerRating(true) == null
						|| daytimeBulletin.dangerRating(true) == DangerRating.missing
						|| daytimeBulletin.dangerRating(true) == DangerRating.no_rating)) {
			return serverImagesUrl + "warning_pictos/color/level_0_0.png";
		} else {
			return serverImagesUrl + "warning_pictos/color/level_" + daytimeBulletin.getWarningLevelId() + ".png";
		}
	}

	private String dangerRatingElevation(AvalancheBulletinDaytimeDescription daytimeBulletin) {
		if (daytimeBulletin.isHasElevationDependency()
				&& (daytimeBulletin.dangerRating(true) != daytimeBulletin.dangerRating(false))) {
			if (daytimeBulletin.getTreeline())
				return lang.getBundleString("elevation.treeline.capitalized");
			else if (daytimeBulletin.getElevation() > 0)
				return daytimeBulletin.getElevation() + "m";
			else
				return "";
		} else
			return "";
	}

	private static String getDangerRatingColorStyle(DangerRating dangerRating, Region region) {
		if (dangerRating.equals(DangerRating.very_high)) {
			return "background=\"" + region.getServerImagesUrl() + "bg_checkered.png"
					+ "\" height=\"100%\" width=\"10px\" bgcolor=\"#FF0000\"";
		} else
			return "style=\"background-color: " + dangerRating.getColor()
					+ "; height: 100%; width: 10px; min-width: 10px; padding: 0px; margin: 0px;\"";
	}

	private static String getHeadlineStyle(DangerRating dangerRating) {
		if (dangerRating.equals(DangerRating.low) || dangerRating.equals(DangerRating.moderate)) {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
					+ "#565F61" + "; background-color: " + dangerRating.getColor() + ";\"";
		} else {
			return "style=\"margin: 0; padding: 0; padding-left: 15px; text-decoration: none; font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', 'Helvetica Neue', Helvetica, Arial, 'Lucida Grande', sans-serif; line-height: 1.6; margin-bottom: 0px; font-weight: bold; font-size: 24px; color: "
					+ dangerRating.getColor() + ";\"";
		}
	}

	private static String getDangerPatternStyle(boolean b) {
		if (b)
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; margin-bottom: 10px; font-weight: normal; line-height: 1.6; font-size: 12px; color: #565f61; border: 1px solid #565f61; border-radius: 15px; padding-left: 10px; padding-right: 10px; padding-top: 2px; padding-bottom: 2px; margin-right: 5px; display: inline-block; background-color: #FFFFFF;\"";
		else
			return "";
	}

	private static String getSnowpackStyle(boolean b) {
		if (!b)
			return "style=\"overflow: hidden; float: left; display: none !important; line-height: 0px; height: 0px; border-spacing: 0px;\"";
		else
			return "style=\"padding: 0px; border-spacing: 0px; width: 100%; background-color: #f6fafc;\"";
	}

	private static String getPMStyle(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"display:none;width:0px;max-height:0px;overflow:hidden;mso-hide:all;height:0;font-size:0;max-height:0;line-height:0;margin:0 auto;\"";
		else
			return "style=\"margin: 0; padding: 0; text-decoration: none; font-family: 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif; color: #565f61; width: 100%; margin-top: 10px; border-top: 1px solid #e6eef2; padding-top: 10px;\"";
	}

	private static String getPMStyleTable(boolean daytimeDependency) {
		if (!daytimeDependency)
			return "style=\"mso-hide: all;\"";
		else
			return "";
	}
}

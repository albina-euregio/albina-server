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
package eu.albina.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheReport;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;
import eu.albina.util.HibernateUtil;

/**
 * Controller for statistics.
 * 
 * @author Norbert Lanzanasto
 *
 */
public class StatisticsController {

	private static Logger logger = LoggerFactory.getLogger(StatisticsController.class);

	private static StatisticsController instance = null;

	/**
	 * Private constructor.
	 */
	private StatisticsController() {
	}

	/**
	 * Returns the {@code RegionController} object associated with the current Java
	 * application.
	 * 
	 * @return the {@code RegionController} object associated with the current Java
	 *         application.
	 */
	public static StatisticsController getInstance() {
		if (instance == null) {
			instance = new StatisticsController();
		}
		return instance;
	}

	/**
	 * Return a CSV string with all bulletin information from {@code startDate}
	 * until {@code endDate} in {@code lang} for {@code region}.
	 * 
	 * @param startDate
	 *            the start date of the desired time period
	 * @param endDate
	 *            the end date of the desired time period
	 * @param lang
	 *            the desired language
	 * @param region
	 *            the desired region
	 * @return a CSV string with all bulletin information from {@code startDate}
	 *         until {@code endDate} in {@code lang}
	 */
	public String getDangerRatingStatistics(DateTime startDate, DateTime endDate, LanguageCode lang, String region) {

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		// get latest reports
		Collection<AvalancheReport> reports = AvalancheReportController.getInstance().getPublicReports(startDate,
				endDate, region);

		// get bulletins from report json
		List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
		for (AvalancheReport avalancheReport : reports) {
			JSONArray jsonArray = new JSONArray(avalancheReport.getJsonString());
			for (Object object : jsonArray)
				if (object instanceof JSONObject)
					bulletins.add(new AvalancheBulletin((JSONObject) object));
		}

		transaction.commit();
		entityManager.close();

		return getCsvString(lang, bulletins);
	}

	/**
	 * Return a CSV string with all bulletin information from {@code startDate}
	 * until {@code endDate} in {@code lang} for whole EUREGIO.
	 * 
	 * @param startDate
	 *            the start date of the desired time period
	 * @param endDate
	 *            the end date of the desired time period
	 * @param lang
	 *            the desired language
	 * @return a CSV string with all bulletin information from {@code startDate}
	 *         until {@code endDate} in {@code lang}
	 */
	public String getDangerRatingStatistics(DateTime startDate, DateTime endDate, LanguageCode lang) {

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
		for (String region : GlobalVariables.regionsEuregio) {
			// get latest reports
			Collection<AvalancheReport> reports = AvalancheReportController.getInstance().getPublicReports(startDate,
					endDate, region);

			// get bulletins from report json
			for (AvalancheReport avalancheReport : reports) {
				try {
					JSONArray jsonArray = new JSONArray(avalancheReport.getJsonString());
					for (Object object : jsonArray)
						if (object instanceof JSONObject)
							bulletins.add(new AvalancheBulletin((JSONObject) object));
				} catch (JSONException e) {
					logger.warn("Error parsing report JSON.");
				}
			}
		}

		transaction.commit();
		entityManager.close();

		return getCsvString(lang, bulletins);
	}

	/**
	 * Return a CSV string representing all {@code bulletins} in {@code lang}.
	 * 
	 * @param lang
	 *            the desired language
	 * @param bulletins
	 *            the bulletins that should be included in the CSV string
	 * @return a CSV string representing all {@code bulletins} in {@code lang}
	 */
	public String getCsvString(LanguageCode lang, List<AvalancheBulletin> bulletins) {
		// sort bulletins by validity
		Collections.sort(bulletins, new AvalancheBulletinSortByValidity());

		StringBuilder sb = new StringBuilder();

		// add header
		sb.append("Date");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Daytime");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Region");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Subregion");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerRatingBelow");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerRatingAbove");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerRatingElevation");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1ElevationAbove");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1ElevationBelow");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectN");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectNE");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectE");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectSE");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectS");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectSW");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectW");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1AspectNW");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2ElevationAbove");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2ElevationBelow");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectN");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectNE");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectE");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectSE");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectS");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectSW");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectW");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2AspectNW");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Tendency");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerPattern1");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerPattern2");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvActivityHighlight");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvActivityComment");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("SnowpackStructureComment");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("TendencyComment");
		sb.append(GlobalVariables.csvLineBreak);
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			addCsvLines(sb, avalancheBulletin, false, lang);
			if (avalancheBulletin.isHasDaytimeDependency())
				addCsvLines(sb, avalancheBulletin, true, lang);
		}

		return sb.toString();
	}

	/**
	 * Add a CSV string to a {@code StringBuilder} instance representing the
	 * {@code avalancheBulletin} in {@code lang}.
	 * 
	 * @param sb
	 *            the string builder instance the new string should be added to
	 * @param avalancheBulletin
	 *            the bulletin that should be added to the {@code StringBuilder}
	 *            instance
	 * @param isAfternoon
	 *            true if the afternoon information of the {@code avalancheBulletin}
	 *            should be used
	 * @param lang
	 *            the desired language
	 */
	private void addCsvLines(StringBuilder sb, AvalancheBulletin avalancheBulletin, boolean isAfternoon,
			LanguageCode lang) {
		AvalancheBulletinDaytimeDescription daytimeDescription;
		if (!isAfternoon)
			daytimeDescription = avalancheBulletin.getForenoon();
		else
			daytimeDescription = avalancheBulletin.getAfternoon();
		for (String region : avalancheBulletin.getPublishedRegions()) {
			sb.append(avalancheBulletin.getValidityDateString());
			sb.append(GlobalVariables.csvDeliminator);
			if (isAfternoon)
				sb.append("PM");
			else
				sb.append("AM");
			sb.append(GlobalVariables.csvDeliminator);
			int i = region.lastIndexOf("-");
			String[] r = { region.substring(0, i), region.substring(i + 1) };
			sb.append(r[0]);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(r[1]);
			sb.append(GlobalVariables.csvDeliminator);
			if (!avalancheBulletin.isHasElevationDependency())
				sb.append(daytimeDescription.getDangerRatingAbove().toString());
			else
				sb.append(daytimeDescription.getDangerRatingBelow().toString());
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(daytimeDescription.getDangerRatingAbove().toString());
			sb.append(GlobalVariables.csvDeliminator);
			if (!avalancheBulletin.isHasElevationDependency())
				sb.append(GlobalVariables.notAvailableString);
			else {
				if (avalancheBulletin.getTreeline())
					sb.append(GlobalVariables.getTreelineString(LanguageCode.en));
				else
					sb.append(avalancheBulletin.getElevation());
			}
			sb.append(GlobalVariables.csvDeliminator);

			addCsvAvalancheSituation(sb, daytimeDescription.getAvalancheSituation1());
			addCsvAvalancheSituation(sb, daytimeDescription.getAvalancheSituation2());

			if (avalancheBulletin.getTendency() != null)
				sb.append(avalancheBulletin.getTendency().toString());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);

			if (avalancheBulletin.getDangerPattern1() != null)
				sb.append(avalancheBulletin.getDangerPattern1().toString());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getDangerPattern2() != null)
				sb.append(avalancheBulletin.getDangerPattern2().toString());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);

			if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null)
				sb.append(avalancheBulletin.getAvActivityHighlightsIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getAvActivityCommentIn(lang) != null)
				sb.append(avalancheBulletin.getAvActivityCommentIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
				sb.append(avalancheBulletin.getSnowpackStructureCommentIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getTendencyCommentIn(lang) != null)
				sb.append(avalancheBulletin.getTendencyCommentIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvLineBreak);
		}
	}

	/**
	 * Add a CSV string to a {@code StringBuilder} instance representing the
	 * {@code avalancheSituation}.
	 * 
	 * @param sb
	 *            the string builder instance the new string should be added to
	 * @param avalancheSituation
	 *            the {@code AvalancheSituation} that should be added to the
	 *            {@code StringBuilder} instance
	 */
	private void addCsvAvalancheSituation(StringBuilder sb, AvalancheSituation avalancheSituation) {
		if (avalancheSituation != null && avalancheSituation.getAvalancheSituation() != null) {
			sb.append(avalancheSituation.getAvalancheSituation().toStringId());
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheSituation.getTreelineLow())
				sb.append(GlobalVariables.getTreelineString(LanguageCode.en));
			else {
				if (avalancheSituation.getElevationLow() <= 0)
					sb.append(GlobalVariables.notAvailableString);
				else
					sb.append(avalancheSituation.getElevationLow());
			}
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheSituation.getTreelineHigh())
				sb.append(GlobalVariables.getTreelineString(LanguageCode.en));
			else {
				if (avalancheSituation.getElevationHigh() <= 0)
					sb.append(GlobalVariables.notAvailableString);
				else
					sb.append(avalancheSituation.getElevationHigh());
			}
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheSituation.getAspects() != null && !avalancheSituation.getAspects().isEmpty()) {
				if (avalancheSituation.getAspects().contains(Aspect.N))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.NE))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.E))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.SE))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.S))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.SW))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.W))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation.getAspects().contains(Aspect.NW))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
			} else {
				for (int i = 0; i < 8; i++) {
					sb.append(GlobalVariables.notAvailableString);
					sb.append(GlobalVariables.csvDeliminator);
				}
			}
		} else {
			for (int i = 0; i < 11; i++) {
				sb.append(GlobalVariables.notAvailableString);
				sb.append(GlobalVariables.csvDeliminator);
			}
		}
	}
}

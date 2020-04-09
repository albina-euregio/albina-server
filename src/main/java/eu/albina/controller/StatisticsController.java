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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheReport;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.MatrixInformation;
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
	 * @param extended
	 *            add textcat ids, matrix information and author if {@code true}
	 * @return a CSV string with all bulletin information from {@code startDate}
	 *         until {@code endDate} in {@code lang}
	 */
	public String getDangerRatingStatistics(DateTime startDate, DateTime endDate, LanguageCode lang, String region,
			boolean extended) {

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		// get latest reports
		Collection<AvalancheReport> reports = AvalancheReportController.getInstance().getPublicReports(startDate,
				endDate, region);

		// get bulletins from report json
		List<AvalancheBulletin> bulletins = getPublishedBulletinsFromReports(reports);

		List<AvalancheBulletin> mergedBulletins = mergeBulletins(bulletins);

		transaction.commit();
		entityManager.close();

		return getCsvString(lang, mergedBulletins, extended);
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
	 * @param extended
	 *            add textcat ids, matrix information and author if {@code true}
	 * @return a CSV string with all bulletin information from {@code startDate}
	 *         until {@code endDate} in {@code lang}
	 */
	public String getDangerRatingStatistics(DateTime startDate, DateTime endDate, LanguageCode lang, boolean extended) {

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();

		List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
		for (String region : GlobalVariables.regionsEuregio) {
			// get latest reports
			Collection<AvalancheReport> reports = AvalancheReportController.getInstance().getPublicReports(startDate,
					endDate, region);
			// get bulletins from report json
			bulletins.addAll(getPublishedBulletinsFromReports(reports));
		}

		List<AvalancheBulletin> mergedBulletins = mergeBulletins(bulletins);

		transaction.commit();
		entityManager.close();

		return getCsvString(lang, mergedBulletins, extended);
	}

	private List<AvalancheBulletin> mergeBulletins(List<AvalancheBulletin> bulletins) {
		Map<String, AvalancheBulletin> resultMap = new HashMap<String, AvalancheBulletin>();
		int revision = 1;

		for (AvalancheBulletin bulletin : bulletins) {
			if (resultMap.containsKey(bulletin.getId())) {
				// merge bulletins with same id
				if (resultMap.get(bulletin.getId()).equals(bulletin)) {
					for (String publishedRegion : bulletin.getPublishedRegions())
						resultMap.get(bulletin.getId()).addPublishedRegion(publishedRegion);
					for (String savedRegion : bulletin.getSavedRegions())
						resultMap.get(bulletin.getId()).addSavedRegion(savedRegion);
					for (String suggestedRegion : bulletin.getSuggestedRegions())
						resultMap.get(bulletin.getId()).addSuggestedRegion(suggestedRegion);
				} else {
					List<AvalancheBulletin> newList = new ArrayList<AvalancheBulletin>();
					for (String bulletinId : resultMap.keySet()) {
						if (bulletinId.startsWith(bulletin.getId())) {
							if (resultMap.get(bulletinId).equals(bulletin)) {
								for (String publishedRegion : bulletin.getPublishedRegions())
									resultMap.get(bulletinId).addPublishedRegion(publishedRegion);
								for (String savedRegion : bulletin.getSavedRegions())
									resultMap.get(bulletin.getId()).addSavedRegion(savedRegion);
								for (String suggestedRegion : bulletin.getSuggestedRegions())
									resultMap.get(bulletin.getId()).addSuggestedRegion(suggestedRegion);
							} else {
								bulletin.setId(bulletin.getId() + "_" + revision);
								revision++;
								newList.add(bulletin);
							}
						}
					}
					for (AvalancheBulletin avalancheBulletin : newList) {
						resultMap.put(avalancheBulletin.getId(), avalancheBulletin);
					}
				}
			} else
				resultMap.put(bulletin.getId(), bulletin);
		}

		return new ArrayList<AvalancheBulletin>(resultMap.values());
	}

	private List<AvalancheBulletin> getPublishedBulletinsFromReports(Collection<AvalancheReport> reports) {
		List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
		for (AvalancheReport avalancheReport : reports) {
			try {
				if (avalancheReport.getJsonString() == null || avalancheReport.getJsonString().isEmpty()) {
					StringBuilder sb = new StringBuilder();
					sb.append("JSON string empty: ");
					if (avalancheReport.getDate() != null) {
						sb.append(avalancheReport.getDate()
								.toString(GlobalVariables.getPublicationDateTimeFormatter(LanguageCode.en)));
						sb.append(", ");
					}
					sb.append(avalancheReport.getRegion());
					logger.warn(sb.toString());
				}
				JSONArray jsonArray = new JSONArray(avalancheReport.getJsonString());
				for (Object object : jsonArray) {
					if (object instanceof JSONObject) {
						AvalancheBulletin bulletin = new AvalancheBulletin((JSONObject) object);
						// only add bulletins with published regions
						if (bulletin.getPublishedRegions() != null && !bulletin.getPublishedRegions().isEmpty())
							bulletins.add(bulletin);
					}
				}
			} catch (JSONException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("Error parsing report JSON: ");
				if (avalancheReport.getDate() != null) {
					sb.append(avalancheReport.getDate()
							.toString(GlobalVariables.getPublicationDateTimeFormatter(LanguageCode.en)));
					sb.append(", ");
				}
				sb.append(avalancheReport.getRegion());
				logger.warn(sb.toString());
			}
		}
		return bulletins;
	}

	/**
	 * Return a CSV string representing all {@code bulletins} in {@code lang}.
	 *
	 * @param lang
	 *            the desired language
	 * @param bulletins
	 *            the bulletins that should be included in the CSV string
	 * @param extended
	 *            add textcat ids, matrix information and author if {@code true}
	 * @return a CSV string representing all {@code bulletins} in {@code lang}
	 */
	public String getCsvString(LanguageCode lang, List<AvalancheBulletin> bulletins, boolean extended) {
		// sort bulletins by validity
		bulletins.sort(Comparator.comparing(AvalancheBulletin::getValidFrom));

		StringBuilder sb = new StringBuilder();

		// add header
		sb.append("BulletinId");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Date");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Daytime");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Region");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("Subregion");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerRatingBelow");
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowArtificialDangerRating");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowArtificialAvalancheSize");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowArtificialAvalancheReleaseProbability");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowArtificialHazardSiteDistribution");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowNaturalDangerRating");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowNaturalAvalancheReleaseProbability");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingBelowNaturalHazardSiteDistribution");
		}
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("DangerRatingAbove");
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveArtificialDangerRating");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveArtificialAvalancheSize");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveArtificialAvalancheReleaseProbability");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveArtificialHazardSiteDistribution");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveNaturalDangerRating");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveNaturalAvalancheReleaseProbability");
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("DangerRatingAboveNaturalHazardSiteDistribution");
		}
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
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("AvActivityHighlightIdsDe");
		}
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvActivityComment");
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("AvActivityCommentIdsDe");
		}
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("SnowpackStructureComment");
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("SnowpackStructureCommentIdsDe");
		}
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("TendencyComment");
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("TendencyCommentIdsDe");
		}
		if (extended) {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append("Author");
		}
		sb.append(GlobalVariables.csvLineBreak);
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			addCsvLines(sb, avalancheBulletin, false, lang, extended);
			if (avalancheBulletin.isHasDaytimeDependency())
				addCsvLines(sb, avalancheBulletin, true, lang, extended);
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
	 * @param extended
	 *            add textcat ids, matrix information and author if {@code true}
	 * @param lang
	 *            the desired language
	 */
	private void addCsvLines(StringBuilder sb, AvalancheBulletin avalancheBulletin, boolean isAfternoon,
			LanguageCode lang, boolean extended) {
		AvalancheBulletinDaytimeDescription daytimeDescription;
		if (!isAfternoon)
			daytimeDescription = avalancheBulletin.getForenoon();
		else
			daytimeDescription = avalancheBulletin.getAfternoon();
		for (String region : avalancheBulletin.getPublishedRegions()) {
			sb.append(avalancheBulletin.getId());
			sb.append(GlobalVariables.csvDeliminator);
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
			if (!avalancheBulletin.isHasElevationDependency()) {
				sb.append(daytimeDescription.getDangerRatingAbove().toString());
				if (extended)
					addMatrixInformation(sb, daytimeDescription.getMatrixInformationAbove());
			} else {
				sb.append(daytimeDescription.getDangerRatingBelow().toString());
				if (extended)
					addMatrixInformation(sb, daytimeDescription.getMatrixInformationBelow());
			}
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(daytimeDescription.getDangerRatingAbove().toString());
			if (extended)
				addMatrixInformation(sb, daytimeDescription.getMatrixInformationAbove());
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
			if (extended) {
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheBulletin.getAvActivityHighlightsTextcat() != null)
					sb.append(avalancheBulletin.getAvActivityHighlightsTextcat());
				else
					sb.append(GlobalVariables.notAvailableString);
			}
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getAvActivityCommentIn(lang) != null)
				sb.append(avalancheBulletin.getAvActivityCommentIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			if (extended) {
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheBulletin.getAvActivityCommentTextcat() != null)
					sb.append(avalancheBulletin.getAvActivityCommentTextcat());
				else
					sb.append(GlobalVariables.notAvailableString);
			}
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
				sb.append(avalancheBulletin.getSnowpackStructureCommentIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			if (extended) {
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheBulletin.getSnowpackStructureCommentTextcat() != null)
					sb.append(avalancheBulletin.getSnowpackStructureCommentTextcat());
				else
					sb.append(GlobalVariables.notAvailableString);
			}
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getTendencyCommentIn(lang) != null)
				sb.append(avalancheBulletin.getTendencyCommentIn(lang));
			else
				sb.append(GlobalVariables.notAvailableString);
			if (extended) {
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheBulletin.getTendencyCommentTextcat() != null)
					sb.append(avalancheBulletin.getTendencyCommentTextcat());
				else
					sb.append(GlobalVariables.notAvailableString);
			}
			if (extended) {
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheBulletin.getUser() != null && avalancheBulletin.getUser().getName() != null)
					sb.append(avalancheBulletin.getUser().getName());
				else
					sb.append(GlobalVariables.notAvailableString);
			}
			sb.append(GlobalVariables.csvLineBreak);
		}
	}

	private void addMatrixInformation(StringBuilder sb, MatrixInformation matrixInformation) {
		if (matrixInformation != null) {
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getArtificialDangerRating() != null)
				sb.append(matrixInformation.getArtificialDangerRating());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getArtificialAvalancheSize() != null)
				sb.append(matrixInformation.getArtificialAvalancheSize());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getArtificialAvalancheReleaseProbability() != null)
				sb.append(matrixInformation.getArtificialAvalancheReleaseProbability());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getArtificialHazardSiteDistribution() != null)
				sb.append(matrixInformation.getArtificialHazardSiteDistribution());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getNaturalDangerRating() != null)
				sb.append(matrixInformation.getNaturalDangerRating());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getNaturalAvalancheReleaseProbability() != null)
				sb.append(matrixInformation.getNaturalAvalancheReleaseProbability());
			else
				sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			if (matrixInformation.getNaturalHazardSiteDistribution() != null)
				sb.append(matrixInformation.getNaturalHazardSiteDistribution());
			else
				sb.append(GlobalVariables.notAvailableString);
		} else {
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(GlobalVariables.notAvailableString);
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

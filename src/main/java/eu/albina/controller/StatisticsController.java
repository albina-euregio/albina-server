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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.MatrixInformation;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;

/**
 * Controller for statistics.
 *
 * @author Norbert Lanzanasto
 *
 */
public class StatisticsController {

	static String csvDeliminator = ";";

	static String csvLineBreak = "\n";

	static String notAvailableString = "N/A";

	private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

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
	 * @param duplicateBulletinForenoon
	 *            add two lines for each bulletin that has no daytime dependency if
	 *            {@code true}
	 * @return a CSV string with all bulletin information from {@code startDate}
	 *         until {@code endDate} in {@code lang}
	 */
	public String getDangerRatingStatistics(Instant startDate, Instant endDate, LanguageCode lang, Region region,
			boolean extended, boolean duplicateBulletinForenoon, boolean obsoleteMatrix) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			// get latest reports
			Collection<AvalancheReport> reports = AvalancheReportController.getInstance().getPublicReports(startDate,
					endDate, region);

			// get bulletins from report json
			List<AvalancheBulletin> bulletins = getPublishedBulletinsFromReports(reports, lang);

			List<AvalancheBulletin> mergedBulletins = mergeBulletins(bulletins);

			return getCsvString(lang, mergedBulletins, extended, duplicateBulletinForenoon, obsoleteMatrix);
		});
	}

	/**
	 * Return a CSV string with all bulletin information from {@code startDate}
	 * until {@code endDate} in {@code lang} for {@code regions}.
	 *
	 * @param startDate
	 *            the start date of the desired time period
	 * @param endDate
	 *            the end date of the desired time period
	 * @param lang
	 *            the desired language
	 * @param regions
	 * 			  the desired regions
	 * @param extended
	 *            add textcat ids, matrix information and author if {@code true}
	 * @param duplicateBulletinForenoon
	 *            add two lines for each bulletin that has no daytime dependency if
	 *            {@code true}
	 * @return a CSV string with all bulletin information from {@code startDate}
	 *         until {@code endDate} in {@code lang}
	 */
	public String getDangerRatingStatistics(Instant startDate, Instant endDate, LanguageCode lang, List<Region> regions, boolean extended,
			boolean duplicateBulletinForenoon, boolean obsoleteMatrix) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			List<AvalancheBulletin> bulletins = regions.stream()
				.map(region -> AvalancheReportController.getInstance().getPublicReports(startDate,
					endDate, region)).flatMap(reports -> getPublishedBulletinsFromReports(reports, lang).stream())
				.collect(Collectors.toList());
			// get latest reports
			// get bulletins from report json

			List<AvalancheBulletin> mergedBulletins = mergeBulletins(bulletins);

			return getCsvString(lang, mergedBulletins, extended, duplicateBulletinForenoon, obsoleteMatrix);
		});
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

	private List<AvalancheBulletin> getPublishedBulletinsFromReports(Collection<AvalancheReport> reports, LanguageCode lang) {
		List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
		for (AvalancheReport avalancheReport : reports) {
			try {
				if (avalancheReport.getJsonString() == null || avalancheReport.getJsonString().isEmpty()) {
					logger.warn("JSON string empty: {}, {}", avalancheReport.getDate(), avalancheReport.getRegion());
				}
				JSONArray jsonArray = new JSONArray(avalancheReport.getJsonString());
				for (Object object : jsonArray) {
					if (object instanceof JSONObject) {
						AvalancheBulletin bulletin = new AvalancheBulletin((JSONObject) object, UserController.getInstance()::getUser);
						// only add bulletins with published regions
						if (bulletin.getPublishedRegions() != null && !bulletin.getPublishedRegions().isEmpty())
							bulletins.add(bulletin);
					}
				}
			} catch (JSONException e) {
				logger.warn("Error parsing report JSON: {}, {}", avalancheReport.getDate(), avalancheReport.getRegion());
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
	 * @param duplicateBulletinForenoon
	 *            add two lines for each bulletin that has no daytime dependency if
	 *            {@code true}
	 * @return a CSV string representing all {@code bulletins} in {@code lang}
	 */
	public String getCsvString(LanguageCode lang, List<AvalancheBulletin> bulletins, boolean extended,
			boolean duplicateBulletinForenoon, boolean obsoleteMatrix) {
		// sort bulletins by validity
		bulletins.sort(Comparator.comparing(AvalancheBulletin::getValidFrom));

		StringBuilder sb = new StringBuilder();

		// add header
		sb.append("BulletinId");
		sb.append(csvDeliminator);
		sb.append("Date");
		sb.append(csvDeliminator);
		sb.append("Daytime");
		sb.append(csvDeliminator);
		sb.append("Microregion");
		sb.append(csvDeliminator);
		sb.append("DangerRatingBelow");
		sb.append(csvDeliminator);
		sb.append("DangerRatingAbove");
		sb.append(csvDeliminator);
		sb.append("DangerRatingElevation");
		sb.append(csvDeliminator);

		String[] problems = {"AvalancheProblem1", "AvalancheProblem2", "AvalancheProblem3", "AvalancheProblem4", "AvalancheProblem5"}; 
		for (String problem : problems) {
			sb.append(problem);
			sb.append(csvDeliminator);
			sb.append(problem + "ElevationAbove");
			sb.append(csvDeliminator);
			sb.append(problem + "ElevationBelow");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectN");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectNE");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectE");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectSE");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectS");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectSW");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectW");
			sb.append(csvDeliminator);
			sb.append(problem + "AspectNW");
			if (extended) {
				if (obsoleteMatrix) {
					sb.append(csvDeliminator);
					sb.append(problem + "ArtificialDangerRating");
					sb.append(csvDeliminator);
					sb.append(problem + "ArtificialAvalancheSize");
					sb.append(csvDeliminator);
					sb.append(problem + "ArtificialAvalancheReleaseProbability");
					sb.append(csvDeliminator);
					sb.append(problem + "ArtificialHazardSiteDistribution");
					sb.append(csvDeliminator);
					sb.append(problem + "NaturalDangerRating");
					sb.append(csvDeliminator);
					sb.append(problem + "NaturalAvalancheReleaseProbability");
					sb.append(csvDeliminator);
					sb.append(problem + "NaturalHazardSiteDistribution");
				} else {
					sb.append(csvDeliminator);
					sb.append(problem + "DangerRating");
					sb.append(csvDeliminator);
					sb.append(problem + "DangerRatingModificator");
					sb.append(csvDeliminator);
					sb.append(problem + "SnowpackStability");
					sb.append(csvDeliminator);
					sb.append(problem + "SnowpackStabilityValue");
					sb.append(csvDeliminator);
					sb.append(problem + "Frequency");
					sb.append(csvDeliminator);
					sb.append(problem + "FrequencyValue");
					sb.append(csvDeliminator);
					sb.append(problem + "AvalancheSize");
					sb.append(csvDeliminator);
					sb.append(problem + "AvalancheSizeValue");
				}
			}
			sb.append(csvDeliminator);	
		}
		sb.append("Tendency");
		sb.append(csvDeliminator);
		sb.append("DangerPattern1");
		sb.append(csvDeliminator);
		sb.append("DangerPattern2");
		sb.append(csvDeliminator);
		sb.append("AvActivityHighlight");
		if (extended) {
			sb.append(csvDeliminator);
			sb.append("AvActivityHighlightIdsDe");
		}
		sb.append(csvDeliminator);
		sb.append("AvActivityComment");
		if (extended) {
			sb.append(csvDeliminator);
			sb.append("AvActivityCommentIdsDe");
		}
		sb.append(csvDeliminator);
		sb.append("SnowpackStructureComment");
		if (extended) {
			sb.append(csvDeliminator);
			sb.append("SnowpackStructureCommentIdsDe");
		}
		sb.append(csvDeliminator);
		sb.append("TendencyComment");
		if (extended) {
			sb.append(csvDeliminator);
			sb.append("TendencyCommentIdsDe");
		}
		if (extended) {
			sb.append(csvDeliminator);
			sb.append("Author");
		}
		sb.append(csvLineBreak);
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			addCsvLines(sb, avalancheBulletin, false, lang, extended, false, obsoleteMatrix);
			if (avalancheBulletin.isHasDaytimeDependency()) {
				addCsvLines(sb, avalancheBulletin, true, lang, extended, false, obsoleteMatrix);
			} else if (duplicateBulletinForenoon) {
				addCsvLines(sb, avalancheBulletin, false, lang, extended, true, obsoleteMatrix);
			}
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
			LanguageCode lang, boolean extended, boolean duplicateBulletinForenoon, boolean obsoleteMatrix) {
		AvalancheBulletinDaytimeDescription daytimeDescription;
		if (!isAfternoon || duplicateBulletinForenoon)
			daytimeDescription = avalancheBulletin.getForenoon();
		else
			daytimeDescription = avalancheBulletin.getAfternoon();
		for (String region : avalancheBulletin.getPublishedRegions()) {
			sb.append(avalancheBulletin.getId());
			sb.append(csvDeliminator);
			sb.append(avalancheBulletin.getValidityDateString());
			sb.append(csvDeliminator);
			if (isAfternoon || duplicateBulletinForenoon)
				sb.append("PM");
			else
				sb.append("AM");
			sb.append(csvDeliminator);
			sb.append(region);
			sb.append(csvDeliminator);
			if (!daytimeDescription.isHasElevationDependency()) {
				sb.append(daytimeDescription.dangerRating(true).toString());
			} else {
				sb.append(daytimeDescription.dangerRating(false).toString());
			}
			sb.append(csvDeliminator);
			sb.append(daytimeDescription.dangerRating(true).toString());
			sb.append(csvDeliminator);
			if (!daytimeDescription.isHasElevationDependency())
				sb.append(notAvailableString);
			else {
				if (daytimeDescription.getTreeline())
					sb.append(lang.getBundleString("elevation.treeline"));
				else
					sb.append(daytimeDescription.getElevation());
			}
			sb.append(csvDeliminator);

			addCsvAvalancheProblem(sb, daytimeDescription.getAvalancheProblem1(), extended, lang, obsoleteMatrix);
			addCsvAvalancheProblem(sb, daytimeDescription.getAvalancheProblem2(), extended, lang, obsoleteMatrix);
			addCsvAvalancheProblem(sb, daytimeDescription.getAvalancheProblem3(), extended, lang, obsoleteMatrix);
			addCsvAvalancheProblem(sb, daytimeDescription.getAvalancheProblem4(), extended, lang, obsoleteMatrix);
			addCsvAvalancheProblem(sb, daytimeDescription.getAvalancheProblem5(), extended, lang, obsoleteMatrix);

			if (avalancheBulletin.getTendency() != null)
				sb.append(avalancheBulletin.getTendency().toString());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);

			if (avalancheBulletin.getDangerPattern1() != null)
				sb.append(avalancheBulletin.getDangerPattern1().toString());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (avalancheBulletin.getDangerPattern2() != null)
				sb.append(avalancheBulletin.getDangerPattern2().toString());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);

			if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null)
				sb.append(StringEscapeUtils.escapeCsv(avalancheBulletin.getAvActivityHighlightsIn(lang)));
			else
				sb.append(notAvailableString);
			if (extended) {
				sb.append(csvDeliminator);
				if (avalancheBulletin.getAvActivityHighlightsTextcat() != null)
					sb.append(avalancheBulletin.getAvActivityHighlightsTextcat());
				else
					sb.append(notAvailableString);
			}
			sb.append(csvDeliminator);
			if (avalancheBulletin.getAvActivityCommentIn(lang) != null)
				sb.append(StringEscapeUtils.escapeCsv(avalancheBulletin.getAvActivityCommentIn(lang)));
			else
				sb.append(notAvailableString);
			if (extended) {
				sb.append(csvDeliminator);
				if (avalancheBulletin.getAvActivityCommentTextcat() != null)
					sb.append(avalancheBulletin.getAvActivityCommentTextcat());
				else
					sb.append(notAvailableString);
			}
			sb.append(csvDeliminator);
			if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
				sb.append(StringEscapeUtils.escapeCsv(avalancheBulletin.getSnowpackStructureCommentIn(lang)));
			else
				sb.append(notAvailableString);
			if (extended) {
				sb.append(csvDeliminator);
				if (avalancheBulletin.getSnowpackStructureCommentTextcat() != null)
					sb.append(avalancheBulletin.getSnowpackStructureCommentTextcat());
				else
					sb.append(notAvailableString);
			}
			sb.append(csvDeliminator);
			if (avalancheBulletin.getTendencyCommentIn(lang) != null)
				sb.append(StringEscapeUtils.escapeCsv(avalancheBulletin.getTendencyCommentIn(lang)));
			else
				sb.append(notAvailableString);
			if (extended) {
				sb.append(csvDeliminator);
				if (avalancheBulletin.getTendencyCommentTextcat() != null)
					sb.append(avalancheBulletin.getTendencyCommentTextcat());
				else
					sb.append(notAvailableString);
			}
			if (extended) {
				sb.append(csvDeliminator);
				if (avalancheBulletin.getUser() != null && avalancheBulletin.getUser().getName() != null)
					sb.append(avalancheBulletin.getUser().getName());
				else
					sb.append(notAvailableString);
			}
			sb.append(csvLineBreak);
		}
	}

	private void addMatrixInformation(StringBuilder sb, MatrixInformation matrixInformation) {
		if (matrixInformation != null) {
			if (matrixInformation.getArtificialDangerRating() != null)
				sb.append(matrixInformation.getArtificialDangerRating());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (matrixInformation.getArtificialAvalancheSize() != null)
				sb.append(matrixInformation.getArtificialAvalancheSize());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (matrixInformation.getArtificialAvalancheReleaseProbability() != null)
				sb.append(matrixInformation.getArtificialAvalancheReleaseProbability());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (matrixInformation.getArtificialHazardSiteDistribution() != null)
				sb.append(matrixInformation.getArtificialHazardSiteDistribution());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (matrixInformation.getNaturalDangerRating() != null)
				sb.append(matrixInformation.getNaturalDangerRating());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (matrixInformation.getNaturalAvalancheReleaseProbability() != null)
				sb.append(matrixInformation.getNaturalAvalancheReleaseProbability());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (matrixInformation.getNaturalHazardSiteDistribution() != null)
				sb.append(matrixInformation.getNaturalHazardSiteDistribution());
			else
				sb.append(notAvailableString);
		} else {
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
		}
	}

	private void addEawsMatrixInformation(StringBuilder sb, EawsMatrixInformation eawsMatrixInformation) {
		if (eawsMatrixInformation != null) {
			if (eawsMatrixInformation.getDangerRating() != null)
				sb.append(eawsMatrixInformation.getDangerRating());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (eawsMatrixInformation.getDangerRatingModificator() != null)
				sb.append(eawsMatrixInformation.getDangerRatingModificator());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			if (eawsMatrixInformation.getSnowpackStability() != null)
				sb.append(eawsMatrixInformation.getSnowpackStability());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(eawsMatrixInformation.getSnowpackStabilityValue());
			sb.append(csvDeliminator);
			if (eawsMatrixInformation.getFrequency() != null)
				sb.append(eawsMatrixInformation.getFrequency());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(eawsMatrixInformation.getFrequencyValue());
			sb.append(csvDeliminator);
			if (eawsMatrixInformation.getAvalancheSize() != null)
				sb.append(eawsMatrixInformation.getAvalancheSize());
			else
				sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(eawsMatrixInformation.getAvalancheSizeValue());
		} else {
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
			sb.append(csvDeliminator);
			sb.append(notAvailableString);
		}
	}

	/**
	 * Add a CSV string to a {@code StringBuilder} instance representing the
	 * {@code avalancheProblem}.
	 *  @param sb
	 *            the string builder instance the new string should be added to
	 * @param avalancheProblem
	 *            the {@code AvalancheProblem} that should be added to the
	 *            {@code StringBuilder} instance
	 * @param lang
	 */
	private void addCsvAvalancheProblem(StringBuilder sb, AvalancheProblem avalancheProblem, boolean extended, LanguageCode lang, boolean obsoleteMatrix) {
		if (avalancheProblem != null && avalancheProblem.getAvalancheProblem() != null) {
			sb.append(avalancheProblem.getAvalancheProblem().toStringId());
			sb.append(csvDeliminator);
			if (avalancheProblem.getTreelineLow())
				sb.append(lang.getBundleString("elevation.treeline"));
			else {
				if (avalancheProblem.getElevationLow() <= 0)
					sb.append(notAvailableString);
				else
					sb.append(avalancheProblem.getElevationLow());
			}
			sb.append(csvDeliminator);
			if (avalancheProblem.getTreelineHigh())
				sb.append(lang.getBundleString("elevation.treeline"));
			else {
				if (avalancheProblem.getElevationHigh() <= 0)
					sb.append(notAvailableString);
				else
					sb.append(avalancheProblem.getElevationHigh());
			}
			sb.append(csvDeliminator);
			if (avalancheProblem.getAspects() != null && !avalancheProblem.getAspects().isEmpty()) {
				if (avalancheProblem.getAspects().contains(Aspect.N))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.NE))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.E))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.SE))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.S))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.SW))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.W))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
				if (avalancheProblem.getAspects().contains(Aspect.NW))
					sb.append("1");
				else
					sb.append("0");
				sb.append(csvDeliminator);
			} else {
				for (int i = 0; i < 8; i++) {
					sb.append(notAvailableString);
					sb.append(csvDeliminator);
				}
			}
			if (extended) {
				if (obsoleteMatrix) {
					addMatrixInformation(sb, avalancheProblem.getMatrixInformation());
					sb.append(csvDeliminator);
				} else {
					addEawsMatrixInformation(sb, avalancheProblem.getEawsMatrixInformation());
					sb.append(csvDeliminator);
				}
			}
		} else {
			for (int i = 0; i < 11; i++) {
				sb.append(notAvailableString);
				sb.append(csvDeliminator);
			}
			if (extended) {
				if (obsoleteMatrix) {
					for (int i = 0; i < 7; i++) {
						sb.append(notAvailableString);
						sb.append(csvDeliminator);
					}
				} else {
					for (int i = 0; i < 8; i++) {
						sb.append(notAvailableString);
						sb.append(csvDeliminator);
					}
				}
			}
		}
	}
}

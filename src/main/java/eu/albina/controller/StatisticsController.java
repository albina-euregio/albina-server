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
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.HibernateException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.albina.exception.AlbinaException;
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

	// private static Logger logger =
	// LoggerFactory.getLogger(StatisticsController.class);

	private static StatisticsController instance = null;

	private StatisticsController() {
	}

	public static StatisticsController getInstance() {
		if (instance == null) {
			instance = new StatisticsController();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public String getDangerRatingStatistics(DateTime startDate, DateTime endDate, LanguageCode lang)
			throws AlbinaException {

		EntityManager entityManager = HibernateUtil.getInstance().getEntityManagerFactory().createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			// get latest reports
			List<AvalancheReport> reports = new ArrayList<AvalancheReport>();
			reports = entityManager.createQuery(HibernateUtil.queryGetLatestReports)
					.setParameter("startDate", startDate).setParameter("endDate", endDate).getResultList();

			// get bulletins from report json
			List<AvalancheBulletin> bulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheReport avalancheReport : reports) {
				JSONArray jsonArray = new JSONArray(avalancheReport.getJsonString());
				for (Object object : jsonArray) {
					if (object instanceof JSONObject) {
						bulletins.add(new AvalancheBulletin((JSONObject) object));
					}
				}
			}

			transaction.commit();

			return getCsvString(lang, bulletins);
		} catch (HibernateException he) {
			if (transaction != null)
				transaction.rollback();
			throw new AlbinaException(he.getMessage());
		} finally {
			entityManager.close();
		}
	}

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
		sb.append("AvalancheProblem1ElevationLow");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem1ElevationHigh");
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
		sb.append("AvalancheProblem2ElevationLow");
		sb.append(GlobalVariables.csvDeliminator);
		sb.append("AvalancheProblem2ElevationHigh");
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
				sb.append("0");
			else
				sb.append(avalancheBulletin.getElevation());
			sb.append(GlobalVariables.csvDeliminator);

			addCsvAvalancheSituation(sb, daytimeDescription.getAvalancheSituation1());
			addCsvAvalancheSituation(sb, daytimeDescription.getAvalancheSituation2());

			if (avalancheBulletin.getTendency() != null)
				sb.append(avalancheBulletin.getTendency().toString());
			sb.append(GlobalVariables.csvDeliminator);

			if (avalancheBulletin.getDangerPattern1() != null)
				sb.append(avalancheBulletin.getDangerPattern1().toString());
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getDangerPattern2() != null)
				sb.append(avalancheBulletin.getDangerPattern2().toString());
			sb.append(GlobalVariables.csvDeliminator);

			if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null)
				sb.append(avalancheBulletin.getAvActivityHighlightsIn(lang));
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getAvActivityCommentIn(lang) != null)
				sb.append(avalancheBulletin.getAvActivityCommentIn(lang));
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null)
				sb.append(avalancheBulletin.getSnowpackStructureCommentIn(lang));
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheBulletin.getTendencyCommentIn(lang) != null)
				sb.append(avalancheBulletin.getTendencyCommentIn(lang));
			sb.append(GlobalVariables.csvLineBreak);
		}
	}

	private void addCsvAvalancheSituation(StringBuilder sb, AvalancheSituation avalancheSituation1) {
		if (avalancheSituation1 != null) {
			if (avalancheSituation1.getAvalancheSituation() != null)
				sb.append(avalancheSituation1.getAvalancheSituation().toStringId());
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(avalancheSituation1.getElevationLow());
			sb.append(GlobalVariables.csvDeliminator);
			sb.append(avalancheSituation1.getElevationHigh());
			sb.append(GlobalVariables.csvDeliminator);
			if (avalancheSituation1.getAspects() != null && !avalancheSituation1.getAspects().isEmpty()) {
				if (avalancheSituation1.getAspects().contains(Aspect.N))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.NE))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.E))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.SE))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.S))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.SW))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.W))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
				if (avalancheSituation1.getAspects().contains(Aspect.NW))
					sb.append("1");
				else
					sb.append("0");
				sb.append(GlobalVariables.csvDeliminator);
			} else {
				for (int i = 0; i < 8; i++)
					sb.append(GlobalVariables.csvDeliminator);
			}
		} else {
			for (int i = 0; i < 11; i++) {
				sb.append(GlobalVariables.csvDeliminator);
			}
		}
	}
}

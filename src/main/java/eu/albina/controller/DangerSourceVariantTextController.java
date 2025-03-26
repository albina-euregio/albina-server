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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantText;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.util.HibernateUtil;


/**
 * Controller for danger sources variant texts.
 *
 * @author Norbert Lanzanasto
 *
 */
public class DangerSourceVariantTextController {

	private static Logger logger = LoggerFactory.getLogger(DangerSourceVariantTextController.class);

	private static DangerSourceVariantTextController instance = null;

	// phrase "Gefahrenstellen05§an_Expositionen"
	// placeholder: {"curlyName":"Gefahrenstellen05§an_Expositionen","line":3,"args":{"vor_allem_an":{"curlyName":"vor_allem_an","line":0},"Expo1":{"curlyName":"Expo1","line":2},"steilen":{"curlyName":"steilen","line":1},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":1},"und_Expo":{"curlyName":"und_Expo","line":7}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects1 = Map.of(
		Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW), "{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":4,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":1}}}",
		Set.of(Aspect.NW, Aspect.N, Aspect.NE), "{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":5,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":1}}}",
		Set.of(Aspect.SE, Aspect.S, Aspect.SW), "{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":4,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":1}}}"
	);

	// phrase "Hangart1"
	// placeholder: {"curlyName":"Hangart1","line":2,"args":{"an_steilen":{"curlyName":"an_steilen","line":1},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":1},"und_Expo":{"curlyName":"und_Expo","line":7}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects2 = Map.of(
		Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW), "{\"curlyName\":\"Hangart1\",\"line\":7}",
		Set.of(Aspect.NW, Aspect.N, Aspect.NE), "{\"curlyName\":\"Hangart1\",\"line\":3,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1}}}",
		Set.of(Aspect.SE, Aspect.S, Aspect.SW), "{\"curlyName\":\"Hangart1\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1}}}"
	);

	// phrase "Expo"
	// placeholder: {"curlyName":"Expo","line":2}
	private static final Map<Aspect, String> textcatSubstitutionsAspect1 = Map.of(
		Aspect.N, "{\"curlyName\":\"Expo\",\"line\":0}",
		Aspect.NE, "{\"curlyName\":\"Expo\",\"line\":1}",
		Aspect.E, "{\"curlyName\":\"Expo\",\"line\":2}",
		Aspect.SE, "{\"curlyName\":\"Expo\",\"line\":3}",
		Aspect.S, "{\"curlyName\":\"Expo\",\"line\":4}",
		Aspect.SW, "{\"curlyName\":\"Expo\",\"line\":5}",
		Aspect.W, "{\"curlyName\":\"Expo\",\"line\":6}",
		Aspect.NW, "{\"curlyName\":\"Expo\",\"line\":7}"
	);
	
	// phrase "Komma_Expo"
	// placeholder: {"curlyName":"Komma_Expo","line":1}
	private static final Map<Aspect, String> textcatSubstitutionsAspect2 = Map.of(
		Aspect.N, "{\"curlyName\":\"Komma_Expo\",\"line\":1}",
		Aspect.NE, "{\"curlyName\":\"Komma_Expo\",\"line\":2}",
		Aspect.E, "{\"curlyName\":\"Komma_Expo\",\"line\":3}",
		Aspect.SE, "{\"curlyName\":\"Komma_Expo\",\"line\":4}",
		Aspect.S, "{\"curlyName\":\"Komma_Expo\",\"line\":5}",
		Aspect.SW, "{\"curlyName\":\"Komma_Expo\",\"line\":6}",
		Aspect.W, "{\"curlyName\":\"Komma_Expo\",\"line\":7}",
		Aspect.NW, "{\"curlyName\":\"Komma_Expo\",\"line\":8}"
	);
	
	// phrase "und_Expo"
	// placeholder: {"curlyName":"und_Expo","line":7}
	private static final Map<Aspect, String> textcatSubstitutionsAspect3 = Map.of(
		Aspect.N, "{\"curlyName\":\"und_Expo\",\"line\":1}",
		Aspect.NE, "{\"curlyName\":\"und_Expo\",\"line\":2}",
		Aspect.E, "{\"curlyName\":\"und_Expo\",\"line\":3}",
		Aspect.SE, "{\"curlyName\":\"und_Expo\",\"line\":4}",
		Aspect.S, "{\"curlyName\":\"und_Expo\",\"line\":5}",
		Aspect.SW, "{\"curlyName\":\"und_Expo\",\"line\":6}",
		Aspect.W, "{\"curlyName\":\"und_Expo\",\"line\":7}",
		Aspect.NW, "{\"curlyName\":\"und_Expo\",\"line\":8}"
	);
	
	// phrase "Höhe_m"
	// placeholder: {"curlyName":"Höhe_m","line":0}
	private static final Map<Integer, String> textcatSubstitutionsElevation1 = Map.ofEntries(
		Map.entry(400, "{\"curlyName\":\"Höhe_m\",\"line\":0}"),
		Map.entry(500, "{\"curlyName\":\"Höhe_m\",\"line\":1}"),
		Map.entry(600, "{\"curlyName\":\"Höhe_m\",\"line\":2}"),
		Map.entry(700, "{\"curlyName\":\"Höhe_m\",\"line\":3}"),
		Map.entry(800, "{\"curlyName\":\"Höhe_m\",\"line\":4}"),
		Map.entry(900, "{\"curlyName\":\"Höhe_m\",\"line\":5}"),
		Map.entry(1000, "{\"curlyName\":\"Höhe_m\",\"line\":6}"),
		Map.entry(1100, "{\"curlyName\":\"Höhe_m\",\"line\":7}"),
		Map.entry(1200, "{\"curlyName\":\"Höhe_m\",\"line\":8}"),
		Map.entry(1300, "{\"curlyName\":\"Höhe_m\",\"line\":9}"),
		Map.entry(1400, "{\"curlyName\":\"Höhe_m\",\"line\":10}"),
		Map.entry(1500, "{\"curlyName\":\"Höhe_m\",\"line\":11}"),
		Map.entry(1600, "{\"curlyName\":\"Höhe_m\",\"line\":12}"),
		Map.entry(1700, "{\"curlyName\":\"Höhe_m\",\"line\":13}"),
		Map.entry(1800, "{\"curlyName\":\"Höhe_m\",\"line\":14}"),
		Map.entry(1900, "{\"curlyName\":\"Höhe_m\",\"line\":15}"),
		Map.entry(2000, "{\"curlyName\":\"Höhe_m\",\"line\":16}"),
		Map.entry(2100, "{\"curlyName\":\"Höhe_m\",\"line\":17}"),
		Map.entry(2200, "{\"curlyName\":\"Höhe_m\",\"line\":18}"),
		Map.entry(2300, "{\"curlyName\":\"Höhe_m\",\"line\":19}"),
		Map.entry(2400, "{\"curlyName\":\"Höhe_m\",\"line\":20}"),
		Map.entry(2500, "{\"curlyName\":\"Höhe_m\",\"line\":21}"),
		Map.entry(2600, "{\"curlyName\":\"Höhe_m\",\"line\":22}"),
		Map.entry(2700, "{\"curlyName\":\"Höhe_m\",\"line\":23}"),
		Map.entry(2800, "{\"curlyName\":\"Höhe_m\",\"line\":24}"),
		Map.entry(2900, "{\"curlyName\":\"Höhe_m\",\"line\":25}"),
		Map.entry(3000, "{\"curlyName\":\"Höhe_m\",\"line\":26}"),
		Map.entry(3100, "{\"curlyName\":\"Höhe_m\",\"line\":27}"),
		Map.entry(3200, "{\"curlyName\":\"Höhe_m\",\"line\":28}"),
		Map.entry(3300, "{\"curlyName\":\"Höhe_m\",\"line\":29}"),
		Map.entry(3400, "{\"curlyName\":\"Höhe_m\",\"line\":30}"),
		Map.entry(3500, "{\"curlyName\":\"Höhe_m\",\"line\":31}"),
		Map.entry(3600, "{\"curlyName\":\"Höhe_m\",\"line\":32}"),
		Map.entry(3700, "{\"curlyName\":\"Höhe_m\",\"line\":33}"),
		Map.entry(3800, "{\"curlyName\":\"Höhe_m\",\"line\":34}"),
		Map.entry(3900, "{\"curlyName\":\"Höhe_m\",\"line\":35}"),
		Map.entry(4000, "{\"curlyName\":\"Höhe_m\",\"line\":36}"),
		Map.entry(4100, "{\"curlyName\":\"Höhe_m\",\"line\":37}"),
		Map.entry(4200, "{\"curlyName\":\"Höhe_m\",\"line\":38}"),
		Map.entry(4300, "{\"curlyName\":\"Höhe_m\",\"line\":39}"),
		Map.entry(4400, "{\"curlyName\":\"Höhe_m\",\"line\":40}"),
		Map.entry(4500, "{\"curlyName\":\"Höhe_m\",\"line\":41}")
	);
	
	/**
	 * Private constructor.
	 */
	private DangerSourceVariantTextController() {
	}

	/**
	 * Returns the {@code DangerSourceVariantTextController} object associated with the
	 * current Java application.
	 *
	 * @return the {@code DangerSourceVariantTextController} object associated with the
	 *         current Java application.
	 */
	public static DangerSourceVariantTextController getInstance() {
		if (instance == null) {
			instance = new DangerSourceVariantTextController();
		}
		return instance;
	}

	/**
	 * Returns all danger source variant texts for the given {@code avalanche type}.
	 *
	 * @param avalancheType
	 *            the type of avalanche
	 * @return all danger source variant texts for the given avalanche type
	 */
	private List<DangerSourceVariantText> getDangerSourceVariantTexts(AvalancheType avalancheType) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			return entityManager.createQuery(HibernateUtil.queryGetDangerSourceVariantTexts, DangerSourceVariantText.class)
				.setParameter("avalancheType", avalancheType).getResultList();
		});
	}

	public DangerSourceVariantText getDangerSourceVariantText(DangerSourceVariant dangerSourceVariant) {
		List<DangerSourceVariantText> result = getDangerSourceVariantTexts(dangerSourceVariant.getAvalancheType());
		switch (dangerSourceVariant.getAvalancheType()) {
			case glide:
				return result.stream()
					.filter(text -> dangerSourceVariant.getGlidingSnowActivity() == null || text.getGlidingSnowActivity() == dangerSourceVariant.getGlidingSnowActivity())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null || text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null || text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
					.findFirst().orElse(null);
			case loose:
				return result.stream()
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null || text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability() == null || text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null || text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
					.findFirst().orElse(null);
			case slab:
				return result.stream()
					.filter(text -> text.getAvalancheProblem() == dangerSourceVariant.deriveAvalancheProblem())
					.filter(text -> dangerSourceVariant.getHasDaytimeDependency() == null || text.getHasDaytimeDependency() == dangerSourceVariant.getHasDaytimeDependency())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null || text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability() == null || text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability())
					.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null || text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
					.findFirst().orElse(null);
			default:
				return null;
		}
	}

	// map of placeholder texts
	//   key: phrase name
	//   value: placeholder regex
	private static final Map<String, String> textcatPlaceholder = Map.ofEntries(
		Map.entry("Gefahrenstellen05§an_Expositionen", "\\{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":3,\"args\":\\{\"vor_allem_an\":\\{\"curlyName\":\"vor_allem_an\",\"line\":0},\"Expo1\":\\{\"curlyName\":\"Expo1\",\"line\":2},\"steilen\":\\{\"curlyName\":\"steilen\",\"line\":1},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":7}}}"),
		Map.entry("Hangart1", "\\{\"curlyName\":\"Hangart1\",\"line\":2,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":7}}}"),
		Map.entry("Expo", "\\{\"curlyName\":\"Expo\",\"line\":2}"),
		Map.entry("Komma_Expo", "\\{\"curlyName\":\"Komma_Expo\",\"line\":1}"),
		Map.entry("und_Expo", "\\{\"curlyName\":\"und_Expo\",\"line\":7}"),
		Map.entry("Höhe_Höhenlage", "\\{\"curlyName\":\"Höhe_Höhenlage\",\"line\":1,\"args\":\\{\"Höhe_m\":\\{\"curlyName\":\"Höhe_m\",\"line\":0}}}"),
		Map.entry("Höhenlage3", "\\{\"curlyName\":\"Höhenlage3\",\"line\":1,\"args\":\\{\"und_vor_allem\":\\{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":\\{\"curlyName\":\"Höhe_m\",\"line\":0}}}"),
		Map.entry("Schwachschichten", "\\{\"curlyName\":\"Schwachschichten\",\"line\":0}"),
		Map.entry("Gefahrenstellen02§und_erkennbar.", "\\{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":1,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":1}}}"),
		Map.entry("Auslösung07§möglich.", "\\{\"curlyName\":\"Auslösung07§möglich.\",\"line\":1}"),
		Map.entry("Hangart1", "\\{\"curlyName\":\"Hangart1\",\"line\":9}"),
		Map.entry("Zeit", "\\{\"curlyName\":\"Zeit\",\"line\":0}"),
		Map.entry("Zeit_optional1", "\\{\"curlyName\":\"Zeit_optional1\",\"line\":0}"),
		Map.entry("an_steilen", "\\{\"curlyName\":\"an_steilen\",\"line\":1}"),
		Map.entry("Alarmzeichen", "\\{\"curlyName\":\"Alarmzeichen\",\"line\":0}"),
		Map.entry("Triebschnee_Beschreibung1", "\\{\"curlyName\":\"Triebschnee_Beschreibung1\",\"line\":22}")
	);

	public String getTextForDangerSourceVariant(DangerSourceVariant dangerSourceVariant, DangerSourceVariantText dangerSourceVariantText) {
		if (dangerSourceVariantText == null) {
			logger.warn("No danger source variant text found!");
			return "";
		}

		// ASPECTS
		String result = dangerSourceVariantText.getTextcat();
		Set<Aspect> aspects = dangerSourceVariant.getAspects();
		if (aspects != null && !aspects.isEmpty()) {
			// phrase: Gefahrenstellen05§an_Expositionen
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects1.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen05§an_Expositionen"), entry.getValue());
					break;
				}
			}
			// phrase: Hangart1
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects2.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("Hangart1"), entry.getValue());
					break;
				}
			}
			// phrase: Expo
			List<Aspect> sortedAspects = Aspect.sortAspects(aspects);
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect1.entrySet()) {
				if (entry.getKey() == sortedAspects.get(0)) {
					result = result.replaceAll(textcatPlaceholder.get("Expo"), entry.getValue());
					break;
				}
			}
			// phrase: Komma_Expo
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect2.entrySet()) {
				if (sortedAspects.size() > 2) {
					if (entry.getKey() == sortedAspects.get(1)) {
						result = result.replaceAll(textcatPlaceholder.get("Komma_Expo"), entry.getValue());
						break;
					}
				} else {
					result = result.replaceAll(textcatPlaceholder.get("Komma_Expo"), "{\"curlyName\":\"Komma_Expo\",\"line\":0}");
					break;
				}
			}
			// phrase: und_Expo
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect3.entrySet()) {
				if (sortedAspects.size() > 1) {
					if (entry.getKey() == sortedAspects.get(1)) {
						result = result.replaceAll(textcatPlaceholder.get("und_Expo"), entry.getValue());
						break;
					}
				} else {
					result = result.replaceAll(textcatPlaceholder.get("und_Expo"), "{\"curlyName\":\"und_Expo\",\"line\":0}");
					break;
				}
			}
		}
		
		// ELEVATION
		// phrase: Höhe_Höhenlage
		if (dangerSourceVariant.getElevationHigh() != null || dangerSourceVariant.getTreelineHigh()) {
			if (dangerSourceVariant.getElevationHigh() != null) {
				// below [m]
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":2,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
			} else {
				// below treeline
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":12}");
			}
		} else if (dangerSourceVariant.getElevationLow() != null || dangerSourceVariant.getTreelineLow()) {
			if (dangerSourceVariant.getElevationLow() != null) {
				// above [m]
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":1,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
			} else {
				// above treeline
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":13}");
			}
		}
		// phrase: Höhenlage3
		if (dangerSourceVariant.getElevationHigh() != null) {
			// below [m]
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"), "{\"curlyName\":\"Höhenlage3\",\"line\":2,\"args\":{\"und_vor_allem\":{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else if (dangerSourceVariant.getElevationLow() != null) {
			// above [m]
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"), "{\"curlyName\":\"Höhenlage3\",\"line\":1,\"args\":{\"und_vor_allem\":{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		}
		// phrase: Höhe_m
		if (dangerSourceVariant.getElevationHigh() != null) {
			for (Map.Entry<Integer, String> entry : textcatSubstitutionsElevation1.entrySet()) {
				if (dangerSourceVariant.getElevationHigh() == entry.getKey().intValue()) {
					result = result.replaceAll(textcatPlaceholder.get("Höhe_m"), entry.getValue());
					break;
				}
			}
		}
		if (dangerSourceVariant.getElevationLow() != null) {
			for (Map.Entry<Integer, String> entry : textcatSubstitutionsElevation1.entrySet()) {
				if (dangerSourceVariant.getElevationLow() == entry.getKey().intValue()) {
					result = result.replaceAll(textcatPlaceholder.get("Höhe_m"), entry.getValue());
					break;
				}
			}
		}
		
		// POSITION OF THE WEAK LAYER
		// phrase: Schwachschichten
		switch (dangerSourceVariant.getWeakLayerPosition()) {
			case upper:
				result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"), "{\"curlyName\":\"Schwachschichten\",\"line\":8}");
				break;
			case lower:
				result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"), "{\"curlyName\":\"Schwachschichten\",\"line\":6}");
				break;
			case middle:
				result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"), "{\"curlyName\":\"Schwachschichten\",\"line\":0}");
				break;
			case ground:
				result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"), "{\"curlyName\":\"Schwachschichten\",\"line\":5}");
				break;
			default:
				result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"), "{\"curlyName\":\"Schwachschichten\",\"line\":0}");
				break;
		}

		// RECOGNIZABILITY
		// phrase: Gefahrenstellen02§und_erkennbar.
		switch (dangerSourceVariant.getDangerSpotRecognizability()) {
			case very_easy:
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":1,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":1}}}");
				break;
			case easy:
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":1,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":1}}}");
				break;
			case hard:
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":2}");
				break;
			case very_hard:
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":2}");
				break;
			default:
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":0,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":1}}}");
				break;
		}

		// TODO replace placeholders with actual values
		// REMOTE TRIGGERING
		// WETNESS (missing in XLS)
		// DANGER PEAK
		// STEEPNESS
		// DANGER SIGNS
		
		return result;
	}
}

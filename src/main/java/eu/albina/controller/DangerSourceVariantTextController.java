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
		null, "{\"curlyName\":\"Komma_Expo\",\"line\":0}",
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
		null, "{\"curlyName\":\"und_Expo\",\"line\":0}",
		Aspect.N, "{\"curlyName\":\"und_Expo\",\"line\":1}",
		Aspect.NE, "{\"curlyName\":\"und_Expo\",\"line\":2}",
		Aspect.E, "{\"curlyName\":\"und_Expo\",\"line\":3}",
		Aspect.SE, "{\"curlyName\":\"und_Expo\",\"line\":4}",
		Aspect.S, "{\"curlyName\":\"und_Expo\",\"line\":5}",
		Aspect.SW, "{\"curlyName\":\"und_Expo\",\"line\":6}",
		Aspect.W, "{\"curlyName\":\"und_Expo\",\"line\":7}",
		Aspect.NW, "{\"curlyName\":\"und_Expo\",\"line\":8}"
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

	private DangerSourceVariantText getDangerSourceVariantText(DangerSourceVariant dangerSourceVariant) {
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

	public String getTextForDangerSourceVariant(DangerSourceVariant dangerSourceVariant) {
		DangerSourceVariantText dangerSourceVariantText = getDangerSourceVariantText(dangerSourceVariant);
		if (dangerSourceVariantText == null) {
			logger.warn("No danger source variant text found for: {}", dangerSourceVariant);
			return "";
		}

		// TODO replace placeholders with actual values
		// aspects
		// elevation
		// position of the weak layer
		// recognizability
		// remote triggering
		// wetness (missing in XLS)
		// danger peak
		// steepness
		// danger signs

		String result = dangerSourceVariantText.getTextcat();
		Set<Aspect> aspects = dangerSourceVariant.getAspects();
		if (aspects != null && !aspects.isEmpty()) {
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects1.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll("{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":3,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"Expo1\":{\"curlyName\":\"Expo1\",\"line\":2},\"steilen\":{\"curlyName\":\"steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}", entry.getValue());
					break;
				}
			}
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects2.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll("{\"curlyName\":\"Hangart1\",\"line\":2,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}", entry.getValue());
					break;
				}
			}
			List<Aspect> sortedAspects = Aspect.sortAspects(aspects);
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect1.entrySet()) {
				if (entry.getKey() == sortedAspects.get(0)) {
					result = result.replaceAll("{\"curlyName\":\"Expo\",\"line\":2}", entry.getValue());
					break;
				}
			}
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect2.entrySet()) {
				if (sortedAspects.size() > 2) {
					if (entry.getKey() == sortedAspects.get(1)) {
						result = result.replaceAll("{\"curlyName\":\"Komma_Expo\",\"line\":1}", entry.getValue());
						break;
					}
				} else {
					if (entry.getKey() == null) {
						result = result.replaceAll("{\"curlyName\":\"Komma_Expo\",\"line\":1}", entry.getValue());
						break;
					}
				}
			}
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect3.entrySet()) {
				if (sortedAspects.size() > 1) {
					if (entry.getKey() == sortedAspects.get(1)) {
						result = result.replaceAll("{\"curlyName\":\"und_Expo\",\"line\":7}", entry.getValue());
						break;
					}
				} else {
					if (entry.getKey() == null) {
						result = result.replaceAll("{\"curlyName\":\"und_Expo\",\"line\":7}", entry.getValue());
						break;
					}
				}
			}
		}

		return dangerSourceVariantText.getTextcat();
	}
}

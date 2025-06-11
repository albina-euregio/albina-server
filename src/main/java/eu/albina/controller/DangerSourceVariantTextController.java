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
import eu.albina.model.enumerations.DangerSign;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.Recognizability;
import eu.albina.model.enumerations.TerrainType;
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
	// placeholder: {"curlyName":"Gefahrenstellen05§an_Expositionen","line":3,"args":{"vor_allem_an":{"curlyName":"vor_allem_an","line":0},"Expo1":{"curlyName":"Expo1","line":2},"steilen":{"curlyName":"steilen","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects1 = Map.of(
		Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW), "{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":1}",
		Set.of(Aspect.NW, Aspect.N, Aspect.NE), "{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":5,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":0}}}",
		Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W), "{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":4,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":0}}}"
	);

	// phrase "es_warum_Ort_wann"
	// placeholder: {"curlyName":"es_warum_Ort_wann","line":27,"args":{"an_steilen":{"curlyName":"an_steilen","line":0},"vor_allem":{"curlyName":"vor_allem","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects2 = Map.of(
		Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW), "{\"es_warum_Ort_wann\":{\"curlyName\":\"es_warum_Ort_wann\",\"line\":32}",
		Set.of(Aspect.NW, Aspect.N, Aspect.NE), "{\"curlyName\":\"es_warum_Ort_wann\",\"line\":28,\"args\":{\"vor_allem\":{\"curlyName\":\"vor_allem\",\"line\":0},\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
		Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W), "{\"curlyName\":\"es_warum_Ort_wann\",\"line\":29,\"args\":{\"vor_allem\":{\"curlyName\":\"vor_allem\",\"line\":0},\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}"
	);

	// phrase "Hangart1"
	// placeholder: {"curlyName":"Hangart1","line":2,"args":{"an_steilen":{"curlyName":"an_steilen","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects3 = Map.of(
		Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW), "{\"curlyName\":\"Hangart1\",\"line\":7}",
		Set.of(Aspect.NW, Aspect.N, Aspect.NE), "{\"curlyName\":\"Hangart1\",\"line\":3,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
		Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W), "{\"curlyName\":\"Hangart1\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}"
	);

	// phrase "Hangart_Höhe2"
	// placeholder: {"curlyName":"Hangart_Höhe2","line":2,"args":{"an_steilen":{"curlyName":"an_steilen","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects4 = Map.of(
		Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":1,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
		Set.of(Aspect.NW, Aspect.N, Aspect.NE), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":3,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
		Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}"
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
	// placeholder: {"curlyName":"Komma_Expo","line":0}
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
	// placeholder: {"curlyName":"und_Expo","line":0}
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
	
	// phrase "Alarmzeichen"
	// placeholder: {"curlyName":"Alarmzeichen","line":0}, {"curlyName":"Altschnee03§sowie_Alarmzeichen","line":0,"args":{"Alarmzeichen2":{"curlyName":"Alarmzeichen2","line":0}}}
	private static final Map<Set<DangerSign>, String> textcatSubstitutionsDangerSigns = Map.of(
		Set.of(DangerSign.whumpfing, DangerSign.shooting_cracks, DangerSign.fresh_avalanches), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}",
		Set.of(DangerSign.whumpfing, DangerSign.fresh_avalanches), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}",
		Set.of(DangerSign.whumpfing, DangerSign.shooting_cracks), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}",
		Set.of(DangerSign.shooting_cracks, DangerSign.fresh_avalanches), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}",
		Set.of(DangerSign.whumpfing), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":1,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}",
		Set.of(DangerSign.shooting_cracks), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":3,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}",
		Set.of(DangerSign.fresh_avalanches), "{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":1},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":1},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":7}}}"
	);

	// phrase "Hangart1"
	// placeholder: {"curlyName":"Hangart1","line":9}, {"curlyName":"Dies02§sowie","line":0}, {"curlyName":"Hangart1°1","line":0}
	private static final Map<Set<TerrainType>, String> textcatSubstitutionsTerrainTypes = Map.ofEntries(
		Map.entry(Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack, TerrainType.gullies_and_bowls, TerrainType.little_used_backcountry_terrain), "{\"curlyName\":\"Hangart1\",\"line\":23}, {\"curlyName\":\"Dies02§sowie\",\"line\":1}, {\"curlyName\":\"Hangart1°1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}}"),
		Map.entry(Set.of(TerrainType.adjacent_to_ridgelines, TerrainType.gullies_and_bowls), "{\"curlyName\":\"Hangart1\",\"line\":15}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.adjacent_to_ridgelines, TerrainType.pass_areas), "{\"curlyName\":\"Hangart1\",\"line\":14}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.gullies_and_bowls, TerrainType.behind_abrupt_changes_in_the_terrain), "{\"curlyName\":\"Hangart1\",\"line\":19}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.gullies_and_bowls, TerrainType.transitions_from_a_shallow_to_a_deep_snowpack), "{\"curlyName\":\"Hangart1\",\"line\":23}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack, TerrainType.little_used_backcountry_terrain), "{\"curlyName\":\"Hangart1\",\"line\":22}, {\"curlyName\":\"Dies02§sowie\",\"line\":1}, {\"curlyName\":\"Hangart1°1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}}"),
		Map.entry(Set.of(TerrainType.base_of_rock_walls, TerrainType.behind_abrupt_changes_in_the_terrain), "{\"curlyName\":\"Hangart1\",\"line\":18}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.adjacent_to_ridgelines), "{\"curlyName\":\"Hangart1\",\"line\":9}, , {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.distant_from_ridgelines), "{\"curlyName\":\"Hangart1\",\"line\":12}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.pass_areas), "{\"curlyName\":\"Hangart1\",\"line\":13}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.gullies_and_bowls), "{\"curlyName\":\"Hangart1\",\"line\":16}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.base_of_rock_walls), "{\"curlyName\":\"Hangart1\",\"line\":17}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.little_used_backcountry_terrain), "{\"curlyName\":\"Hangart1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}"),
		Map.entry(Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack), "{\"curlyName\":\"Hangart1\",\"line\":22}, {\"curlyName\":\"Dies02§sowie\",\"line\":0}, {\"curlyName\":\"Hangart1°1\",\"line\":0}")
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
		if (dangerSourceVariant == null || dangerSourceVariant.getAvalancheType() == null) {
			return null;
		}
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
		Map.entry("Gefahrenstellen05§an_Expositionen", "\\{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":3,\"args\":\\{\"vor_allem_an\":\\{\"curlyName\":\"vor_allem_an\",\"line\":0},\"Expo1\":\\{\"curlyName\":\"Expo1\",\"line\":2},\"steilen\":\\{\"curlyName\":\"steilen\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
		Map.entry("es_warum_Ort_wann", "\\{\"curlyName\":\"es_warum_Ort_wann\",\"line\":27,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":0\\},\"vor_allem\":\\{\"curlyName\":\"vor_allem\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
		Map.entry("Hangart1", "\\{\"curlyName\":\"Hangart1\",\"line\":2,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
		Map.entry("Hangart_Höhe2", "\\{\"curlyName\":\"Hangart_Höhe2\",\"line\":2,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
		Map.entry("Expo", "\\{\"curlyName\":\"Expo\",\"line\":2\\}"),
		Map.entry("Komma_Expo", "\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\}"),
		Map.entry("und_Expo", "\\{\"curlyName\":\"und_Expo\",\"line\":0\\}"),
		Map.entry("Höhe_Höhenlage", "\\{\"curlyName\":\"Höhe_Höhenlage\",\"line\":0\\}"),
		Map.entry("Höhenlage3", "\\{\"curlyName\":\"Höhenlage3\",\"line\":0\\}"),
		Map.entry("oberhalb_von_Höhe_optional", "\\{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":0\\}"),
		Map.entry("Höhe_m", "\\{\"curlyName\":\"Höhe_m\",\"line\":0\\}"),
		Map.entry("Schwachschichten", "\\{\"curlyName\":\"Schwachschichten\",\"line\":0\\}"),
		Map.entry("Gefahrenstellen02§und_erkennbar.", "\\{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":0,\"args\":\\{\"aber_und\":\\{\"curlyName\":\"aber_und\",\"line\":0\\}\\}\\}"),
		Map.entry("aber_und", "\\{\"curlyName\":\"aber_und\",\"line\":0\\}"),
		Map.entry("Auslösung07§möglich.", "\\{\"curlyName\":\"Auslösung07§möglich.\",\"line\":1\\}"),
		Map.entry("Geländeformen", "\\{\"curlyName\":\"Hangart1\",\"line\":9\\}, \\{\"curlyName\":\"Dies02§sowie\",\"line\":0\\}, \\{\"curlyName\":\"Hangart1°1\",\"line\":0\\}"),
		Map.entry("trockene", "\\{\"curlyName\":\"Lawinenart5\",\"line\":12\\}"),
		Map.entry("Zeit", "\\{\"curlyName\":\"Zeit\",\"line\":0\\}"),
		Map.entry("Zeit_optional1", "\\{\"curlyName\":\"Zeit_optional1\",\"line\":0\\}"),
		Map.entry("an_steilen", "\\{\"curlyName\":\"an_steilen\",\"line\":0\\}"),
		Map.entry("steilen", "\\{\"curlyName\":\"steilen\",\"line\":0\\}"),
		Map.entry("möglich.", "\\{\"curlyName\":\"wieviele\",\"line\":0\\}, \\{\"curlyName\":\"Auslösung01§möglich\",\"line\":0\\}"),
		Map.entry("Alarmzeichen", "\\{\"curlyName\":\"Alarmzeichen\",\"line\":0\\}, \\{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0,\"args\":\\{\"Alarmzeichen2\":\\{\"curlyName\":\"Alarmzeichen2\",\"line\":0\\}\\}\\}")
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
			// phrase: es_warum_Ort_wann
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects2.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("es_warum_Ort_wann"), entry.getValue());
					break;
				}
			}
			// phrase: Hangart1
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects3.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("Hangart1"), entry.getValue());
					break;
				}
			}
			// phrase: Hangart_Höhe2
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects4.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("Hangart_Höhe2"), entry.getValue());
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
		Boolean treelineHigh = dangerSourceVariant.getTreelineHigh();
		Boolean treelineLow = dangerSourceVariant.getTreelineLow();
		if (dangerSourceVariant.getElevationHigh() != null || (treelineHigh != null && treelineHigh)) {
			if (dangerSourceVariant.getElevationHigh() != null) {
				// below [m]
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":2,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
			} else {
				// below treeline
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":12}");
			}
		} else if (dangerSourceVariant.getElevationLow() != null || (treelineLow != null && treelineLow)) {
			if (dangerSourceVariant.getElevationLow() != null) {
				// above [m]
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":1,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
			} else {
				// above treeline
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":13}");
			}
		} else {
			// no elevation information
			result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"), "{\"curlyName\":\"Höhe_Höhenlage\",\"line\":0}");
		}

		// phrase: Höhenlage3
		if (dangerSourceVariant.getElevationHigh() != null) {
			// below [m]
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"), "{\"curlyName\":\"Höhenlage3\",\"line\":2,\"args\":{\"und_vor_allem\":{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else if (dangerSourceVariant.getElevationLow() != null) {
			// above [m]
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"), "{\"curlyName\":\"Höhenlage3\",\"line\":1,\"args\":{\"und_vor_allem\":{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else {
			// no elevation information
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"), "{\"curlyName\":\"Höhenlage3\",\"line\":0}");
		}
		
		// phrase: oberhalb_von_Höhe_optional
		if (dangerSourceVariant.getElevationHigh() != null) {
			// below [m]
			result = result.replaceAll(textcatPlaceholder.get("oberhalb_von_Höhe_optional"), "{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":2,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else if (dangerSourceVariant.getElevationLow() != null) {
			// above [m]
			result = result.replaceAll(textcatPlaceholder.get("oberhalb_von_Höhe_optional"), "{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":1,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else {
			// no elevation information
			result = result.replaceAll(textcatPlaceholder.get("oberhalb_von_Höhe_optional"), "{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":0}");
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
		if (dangerSourceVariant.getWeakLayerPosition() != null) {
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
		} else {
			result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"), "{\"curlyName\":\"Schwachschichten\",\"line\":0}");
		}

		// RECOGNIZABILITY
		// phrase: Gefahrenstellen02§und_erkennbar.
		if (dangerSourceVariant.getDangerSpotRecognizability() != null) {
			switch (dangerSourceVariant.getDangerSpotRecognizability()) {
				case very_easy:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":1,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				case easy:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":2,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				case hard:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":3,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				case very_hard:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":6,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":0,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
			}

			if (dangerSourceVariant.getEawsMatrixInformation() != null && dangerSourceVariant.getEawsMatrixInformation().getFrequency() != null) {
				// replace aber_und
				if (
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.few && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.very_easy) ||
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.few && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.easy) ||
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.many && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.hard) ||
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.many && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.very_hard)
				) {
					// und
					result = result.replaceAll(textcatPlaceholder.get("aber_und"), "{\"curlyName\":\"aber_und\",\"line\":2}");
				} else if (
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.few && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.hard) ||
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.few && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.very_hard) ||
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.many && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.easy) ||
					(dangerSourceVariant.getEawsMatrixInformation().getFrequency() == Frequency.many && dangerSourceVariant.getDangerSpotRecognizability() == Recognizability.very_easy)
				) {
					// aber
					result = result.replaceAll(textcatPlaceholder.get("aber_und"), "{\"curlyName\":\"aber_und\",\"line\":1}");
				}
					
				switch (dangerSourceVariant.getEawsMatrixInformation().getFrequency()) {
					case few:
						result = result.replaceAll(textcatPlaceholder.get("aber_und"), "{\"curlyName\":\"aber_und\",\"line\":0}");
						break;
					case many:
						result = result.replaceAll(textcatPlaceholder.get("aber_und"), "{\"curlyName\":\"aber_und\",\"line\":2}");
						break;
					default:
						result = result.replaceAll(textcatPlaceholder.get("aber_und"), "{\"curlyName\":\"aber_und\",\"line\":0}");
						break;
				}
			}

		} else {
			if (dangerSourceVariant.getEawsMatrixInformation() == null || dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null) {
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "");
			}
		}

		// REMOTE TRIGGERING
		// phrase: Auslösung07§möglich.
		if (dangerSourceVariant.getRemoteTriggering() != null) {
			switch (dangerSourceVariant.getRemoteTriggering()) {
				case likely:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."), "{\"curlyName\":\"Auslösung07§möglich.\",\"line\":2}");
					break;
				case possible:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."), "{\"curlyName\":\"Auslösung07§möglich.\",\"line\":1}");
					break;
				case unlikely:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."), "{\"curlyName\":\"Auslösung07§möglich.\",\"line\":0}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."), "{\"curlyName\":\"Auslösung07§möglich.\",\"line\":0}");
					break;
			}
		}

		// TERRAIN TYPES
		// phrase: Hangart1
		Set<TerrainType> terrainTypes = dangerSourceVariant.getTerrainTypes();
		for (Map.Entry<Set<TerrainType>, String> entry : textcatSubstitutionsTerrainTypes.entrySet()) {
			if (entry.getKey().size() == terrainTypes.size() && entry.getKey().containsAll(terrainTypes)) {
				result = result.replaceAll(textcatPlaceholder.get("Geländeformen"), entry.getValue());
				break;
			}
		}

		// WETNESS
		// phrase: {trockene}
		if (dangerSourceVariant.getLooseSnowMoisture() != null) {
			switch (dangerSourceVariant.getLooseSnowMoisture()) {
				case dry:
					result = result.replaceAll(textcatPlaceholder.get("trockene"), "{\"curlyName\":\"Lawinenart5\",\"line\":12}");
					break;
				case moist:
					result = result.replaceAll(textcatPlaceholder.get("trockene"), "{\"curlyName\":\"Lawinenart5\",\"line\":14}");
					break;
				case wet:
					result = result.replaceAll(textcatPlaceholder.get("trockene"), "{\"curlyName\":\"Lawinenart5\",\"line\":15}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("trockene"), "{\"curlyName\":\"Lawinenart5\",\"line\":12}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("trockene"), "{\"curlyName\":\"Lawinenart5\",\"line\":12}");
		}

		// DANGER PEAK
		// phrase: Zeit
		if (dangerSourceVariant.getDangerPeak() != null) {
			switch (dangerSourceVariant.getDangerPeak()) {
				case evening:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":16}");
					break;
				case morning:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":8}");
					break;
				case forenoon:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":10");
					break;
				case afternoon:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":13}");
					break;
				case first_night_half:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":3}");
					break;
				case second_night_half:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":4}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Zeit"), "{\"curlyName\":\"Zeit\",\"line\":0}");
					break;
			}
		}

		// phrase: Zeit_optional1
		if (dangerSourceVariant.getDangerPeak() != null) {
			switch (dangerSourceVariant.getDangerPeak()) {
				case evening:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":18}");
					break;
				case morning:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":10}");
					break;
				case forenoon:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":12}");
					break;
				case afternoon:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":17}");
					break;
				case first_night_half:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":5}");
					break;
				case second_night_half:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":6}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":0}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"), "{\"curlyName\":\"Zeit_optional1\",\"line\":0}");
		}

		// STEEPNESS
		// phrase: an_steilen
		if (dangerSourceVariant.getSlopeGradient() != null) {
			switch (dangerSourceVariant.getSlopeGradient()) {
				case moderately_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"an_steilen\",\"line\":0}");
					break;
				case steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"an_steilen\",\"line\":1}");
					break;
				case very_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"an_steilen\",\"line\":2}");
					break;
				case extremely_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"an_steilen\",\"line\":3}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"an_steilen\",\"line\":0}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"an_steilen\",\"line\":0}");
		}

		// phrase: steilen
		if (dangerSourceVariant.getSlopeGradient() != null) {
			switch (dangerSourceVariant.getSlopeGradient()) {
				case moderately_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"steilen\",\"line\":0}");
					break;
				case steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"steilen\",\"line\":1}");
					break;
				case very_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"steilen\",\"line\":2}");
					break;
				case extremely_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"steilen\",\"line\":3}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"steilen\",\"line\":0}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("an_steilen"), "{\"curlyName\":\"steilen\",\"line\":0}");
		}

		// NATURAL AVALANCHES
		// phrase: möglich
		if (dangerSourceVariant.getNaturalRelease() != null) {
			switch (dangerSourceVariant.getNaturalRelease()) {
				case likely:
					result = result.replaceAll(textcatPlaceholder.get("möglich"), "{\"curlyName\":\"wieviele\",\"line\":0}, {\"curlyName\":\"Auslösung01§möglich\",\"line\":1}");
					break;
				case possible:
					result = result.replaceAll(textcatPlaceholder.get("möglich."), "{\"curlyName\":\"wieviele\",\"line\":0}, {\"curlyName\":\"Auslösung01§möglich\",\"line\":0}");
					break;
				case unlikely:
					result = result.replaceAll(textcatPlaceholder.get("möglich."), "{\"curlyName\":\"wieviele\",\"line\":5}, {\"curlyName\":\"Auslösung01§möglich\",\"line\":0}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("möglich."), "{\"curlyName\":\"wieviele\",\"line\":0}, {\"curlyName\":\"Auslösung01§möglich\",\"line\":0}");
					break;
			}
		}

		// DANGER SIGNS
		// phrase: Alarmzeichen
		Set<DangerSign> dangerSigns = dangerSourceVariant.getDangerSigns();
		for (Map.Entry<Set<DangerSign>, String> entry : textcatSubstitutionsDangerSigns.entrySet()) {
			if (entry.getKey().size() == dangerSigns.size() && entry.getKey().containsAll(dangerSigns)) {
				result = result.replaceAll(textcatPlaceholder.get("Alarmzeichen"), entry.getValue());
				break;
			}
		}
		
		return result;
	}
}

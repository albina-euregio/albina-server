// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantText;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.enumerations.AddOns;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.model.enumerations.DangerSign;
import eu.albina.model.enumerations.Daytime;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.Probability;
import eu.albina.model.enumerations.Recognizability;
import eu.albina.model.enumerations.SlopeGradient;
import eu.albina.model.enumerations.TerrainType;
import eu.albina.model.enumerations.Wetness;

/**
 * Controller for danger sources variant texts.
 *
 * @author Norbert Lanzanasto
 *
 */
@Singleton
public class DangerSourceVariantTextController {

	private static final Logger logger = LoggerFactory.getLogger(DangerSourceVariantTextController.class);

	@Inject
	DangerSourceVariantTextRepository dangerSourceVariantTextRepository;

	// phrase "Gefahrenstellen05§an_Expositionen"
	// placeholder:
	// {"curlyName":"Gefahrenstellen05§an_Expositionen","line":3,"args":{"vor_allem_an":{"curlyName":"vor_allem_an","line":0},"steilen":{"curlyName":"steilen","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects1 = Map.of(
			Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW),
			"{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":1}",
			Set.of(Aspect.NW, Aspect.N, Aspect.NE),
			"{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":5,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":0}}}",
			Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W),
			"{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":4,\"args\":{\"vor_allem_an\":{\"curlyName\":\"vor_allem_an\",\"line\":0},\"steilen\":{\"curlyName\":\"steilen\",\"line\":0}}}");

	// phrase "es_warum_Ort_wann"
	// placeholder:
	// {"curlyName":"es_warum_Ort_wann","line":27,"args":{"an_steilen":{"curlyName":"an_steilen","line":0},"vor_allem":{"curlyName":"vor_allem","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects2 = Map.of(
			Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW),
			"{\"es_warum_Ort_wann\":{\"curlyName\":\"es_warum_Ort_wann\",\"line\":32}",
			Set.of(Aspect.NW, Aspect.N, Aspect.NE),
			"{\"curlyName\":\"es_warum_Ort_wann\",\"line\":28,\"args\":{\"vor_allem\":{\"curlyName\":\"vor_allem\",\"line\":0},\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
			Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W),
			"{\"curlyName\":\"es_warum_Ort_wann\",\"line\":29,\"args\":{\"vor_allem\":{\"curlyName\":\"vor_allem\",\"line\":0},\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}");

	// phrase "Hangart1"
	// placeholder:
	// {"curlyName":"Hangart1","line":2,"args":{"an_steilen":{"curlyName":"an_steilen","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects3 = Map.of(
			Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW),
			"{\"curlyName\":\"Hangart1\",\"line\":7}",
			Set.of(Aspect.NW, Aspect.N, Aspect.NE),
			"{\"curlyName\":\"Hangart1\",\"line\":3,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
			Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W),
			"{\"curlyName\":\"Hangart1\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}");

	// phrase "Hangart_Höhe2"
	// placeholder:
	// {"curlyName":"Hangart_Höhe2","line":2,"args":{"an_steilen":{"curlyName":"an_steilen","line":0},"Expo":{"curlyName":"Expo","line":2},"Komma_Expo":{"curlyName":"Komma_Expo","line":0},"und_Expo":{"curlyName":"und_Expo","line":0}}}
	private static final Map<Set<Aspect>, String> textcatSubstitutionsAspects4 = Map.of(
			Set.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW),
			"{\"curlyName\":\"Hangart_Höhe2\",\"line\":7,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
			Set.of(Aspect.NW, Aspect.N, Aspect.NE),
			"{\"curlyName\":\"Hangart_Höhe2\",\"line\":3,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}",
			Set.of(Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W),
			"{\"curlyName\":\"Hangart_Höhe2\",\"line\":4,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}");

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
			Aspect.NW, "{\"curlyName\":\"Expo\",\"line\":7}");

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
			Aspect.NW, "{\"curlyName\":\"Komma_Expo\",\"line\":8}");

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
			Aspect.NW, "{\"curlyName\":\"und_Expo\",\"line\":8}");

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
			Map.entry(4500, "{\"curlyName\":\"Höhe_m\",\"line\":41}"));

	// phrase "Alarmzeichen"
	// placeholder: {"curlyName":"Alarmzeichen","line":0},
	// {"curlyName":"Altschnee03§sowie_Alarmzeichen","line":0,"args":{"Alarmzeichen2":{"curlyName":"Alarmzeichen2","line":0}}}
	private static final Map<Set<DangerSign>, String> textcatSubstitutionsDangerSigns = Map.of(
			Set.of(DangerSign.whumpfing, DangerSign.shooting_cracks, DangerSign.fresh_avalanches),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":3},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":1,\"args\":{\"Alarmzeichen2\":{\"curlyName\":\"Alarmzeichen2\",\"line\":7}}}",
			Set.of(DangerSign.whumpfing, DangerSign.fresh_avalanches),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":0},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":1,\"args\":{\"Alarmzeichen2\":{\"curlyName\":\"Alarmzeichen2\",\"line\":7}}}",
			Set.of(DangerSign.whumpfing, DangerSign.shooting_cracks),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":3},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0,\"args\":{\"Alarmzeichen2\":{\"curlyName\":\"Alarmzeichen2\",\"line\":0}}}",
			Set.of(DangerSign.shooting_cracks, DangerSign.fresh_avalanches),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":4},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":1,\"args\":{\"Alarmzeichen2\":{\"curlyName\":\"Alarmzeichen2\",\"line\":7}}}",
			Set.of(DangerSign.whumpfing),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":0},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0}",
			Set.of(DangerSign.shooting_cracks),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":4},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0}",
			Set.of(DangerSign.fresh_avalanches),
			"{\"curlyName\":\"Alarmzeichen\",\"line\":7},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0}");

	// phrase "Hangart1"
	// placeholder: {"curlyName":"Hangart1","line":9},
	// {"curlyName":"Dies02§sowie","line":0}, {"curlyName":"Hangart1°1","line":0}
	private static final Map<Set<TerrainType>, String> textcatSubstitutionsTerrainTypes = Map.ofEntries(
			Map.entry(
					Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack, TerrainType.gullies_and_bowls,
							TerrainType.little_used_backcountry_terrain),
					"{\"curlyName\":\"Hangart1\",\"line\":23},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":1},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}}"),
			Map.entry(Set.of(TerrainType.adjacent_to_ridgelines, TerrainType.gullies_and_bowls),
					"{\"curlyName\":\"Hangart1\",\"line\":9},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":1},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":16}"),
			Map.entry(Set.of(TerrainType.adjacent_to_ridgelines, TerrainType.pass_areas),
					"{\"curlyName\":\"Hangart1\",\"line\":14},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.gullies_and_bowls, TerrainType.behind_abrupt_changes_in_the_terrain),
					"{\"curlyName\":\"Hangart1\",\"line\":19},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.gullies_and_bowls, TerrainType.transitions_from_a_shallow_to_a_deep_snowpack),
					"{\"curlyName\":\"Hangart1\",\"line\":23},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(
					Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack,
							TerrainType.little_used_backcountry_terrain),
					"{\"curlyName\":\"Hangart1\",\"line\":22},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":1},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}}"),
			Map.entry(Set.of(TerrainType.base_of_rock_walls, TerrainType.behind_abrupt_changes_in_the_terrain),
					"{\"curlyName\":\"Hangart1\",\"line\":18},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.cut_slopes, TerrainType.grassy_slopes),
					"{\"curlyName\":\"Hangart1\",\"line\":6,\"args\":{\"an_Böschungen\":{\"curlyName\":\"an_Böschungen\",\"line\":3}}},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(
					Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack,
							TerrainType.areas_where_the_snow_cover_is_rather_shallow),
					"{\"curlyName\":\"Hangart1\",\"line\":22},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":1},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":21}"),
			Map.entry(
					Set.of(TerrainType.little_used_backcountry_terrain,
							TerrainType.areas_where_the_snow_cover_is_rather_shallow),
					"{\"curlyName\":\"Hangart1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":1},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":21}"),
			Map.entry(Set.of(TerrainType.adjacent_to_ridgelines),
					"{\"curlyName\":\"Hangart1\",\"line\":9},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.distant_from_ridgelines),
					"{\"curlyName\":\"Hangart1\",\"line\":12},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.pass_areas),
					"{\"curlyName\":\"Hangart1\",\"line\":13},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.gullies_and_bowls),
					"{\"curlyName\":\"Hangart1\",\"line\":16},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.base_of_rock_walls),
					"{\"curlyName\":\"Hangart1\",\"line\":17},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.little_used_backcountry_terrain),
					"{\"curlyName\":\"Hangart1\",\"line\":27,\"args\":{\"Tourengelände\":{\"curlyName\":\"Tourengelände\",\"line\":0}}},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.transitions_from_a_shallow_to_a_deep_snowpack),
					"{\"curlyName\":\"Hangart1\",\"line\":22},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.in_the_vicinity_of_peaks),
					"{\"curlyName\":\"Hangart1\",\"line\":11},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.cut_slopes),
					"{\"curlyName\":\"Hangart1\",\"line\":6,\"args\":{\"an_Böschungen\":{\"curlyName\":\"an_Böschungen\",\"line\":0}}},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.wind_loaded_slopes),
					"{\"curlyName\":\"Hangart1\",\"line\":8},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.areas_where_the_snow_cover_is_rather_shallow),
					"{\"curlyName\":\"Hangart1\",\"line\":21},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.highly_frequented_off_piste_terrain),
					"{\"curlyName\":\"Hangart1\",\"line\":26},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.places_that_are_protected_from_the_wind),
					"{\"curlyName\":\"Hangart1\",\"line\":28},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.regions_with_a_lot_of_snow),
					"{\"curlyName\":\"Hangart1\",\"line\":33},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.regions_exposed_to_precipitation),
					"{\"curlyName\":\"Hangart1\",\"line\":34},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.regions_exposed_to_heavier_precipitation),
					"{\"curlyName\":\"Hangart1\",\"line\":35},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"),
			Map.entry(Set.of(TerrainType.grassy_slopes),
					"{\"curlyName\":\"Hangart1\",\"line\":5,\"args\":{}},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0}"));

	// Add on sentences
	private static final Map<AddOns, String> addOnSentences = Map.of(
			AddOns.remote_triggering,
			"{\"curlyName\":\"Auslösung07\",\"line\":0,\"args\":{\"Auslösung07§Fernauslösungen_sind\":{\"curlyName\":\"Auslösung07§Fernauslösungen_sind\",\"line\":0},\"Auslösung07§möglich.\":{\"curlyName\":\"Auslösung07§möglich.\",\"line\":1}}}",
			AddOns.terrain_features,
			"{\"curlyName\":\"Dies02\",\"line\":0,\"args\":{\"Dies2\":{\"curlyName\":\"Dies2\",\"line\":6},\"Hangart1\":{\"curlyName\":\"Hangart1\",\"line\":9},\"Höhenlage3\":{\"curlyName\":\"Höhenlage3\",\"line\":0},\"Dies02§sowie\":{\"curlyName\":\"Dies02§sowie\",\"line\":0},\"Hangart1°1\":{\"curlyName\":\"Hangart1°1\",\"line\":0},\"Höhenlage3°1\":{\"curlyName\":\"Höhenlage3°1\",\"line\":0}}}",
			AddOns.danger_increase_with_elevation,
			"{\"curlyName\":\"Änderung02\",\"line\":0,\"args\":{\"Änderung02§Die_Gefahrenstellen\":{\"curlyName\":\"Änderung02§Die_Gefahrenstellen\",\"line\":4},\"wo_wann\":{\"curlyName\":\"wo_wann\",\"line\":20}}}",
			AddOns.highest_danger_aspect,
			"{\"curlyName\":\"Änderung08\",\"line\":0,\"args\":{\"Hangart_Höhe_Gebiet1\":{\"curlyName\":\"Hangart_Höhe_Gebiet1\",\"line\":1,\"args\":{\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0},\"Expo\":{\"curlyName\":\"Expo\",\"line\":2},\"Komma_Expo\":{\"curlyName\":\"Komma_Expo\",\"line\":0},\"und_Expo\":{\"curlyName\":\"und_Expo\",\"line\":0}}},\"und_Hangart_Höhe_Gebiet\":{\"curlyName\":\"und_Hangart_Höhe_Gebiet\",\"line\":0},\"Änderung08§Gefahr_höher\":{\"curlyName\":\"Änderung08§Gefahr_höher\",\"line\":2,\"args\":{\"Lawinengefahr2\":{\"curlyName\":\"Lawinengefahr2\",\"line\":0},\"etwas\":{\"curlyName\":\"etwas\",\"line\":0},\"höher\":{\"curlyName\":\"höher\",\"line\":0}}}}}",
			AddOns.danger_peak,
			"{\"curlyName\":\"Änderung03\",\"line\":0,\"args\":{\"wo_Gefahrenspitze2\":{\"curlyName\":\"wo_Gefahrenspitze2\",\"line\":0},\"Änderung03§erreicht\":{\"curlyName\":\"Änderung03§erreicht\",\"line\":0},\"Gefahrenspitze\":{\"curlyName\":\"Gefahrenspitze\",\"line\":1},\"wo_Gefahrenspitze1\":{\"curlyName\":\"wo_Gefahrenspitze1\",\"line\":1},\"Zeit\":{\"curlyName\":\"Zeit\",\"line\":0}}}",
			AddOns.runout_into_green,
			"{\"curlyName\":\"Grösse05\",\"line\":0,\"args\":{\"Die_Lawinen\":{\"curlyName\":\"Die_Lawinen\",\"line\":1},\"Gebiet_auch\":{\"curlyName\":\"Gebiet_auch\",\"line\":0},\"Einzugsgebiet\":{\"curlyName\":\"Einzugsgebiet\",\"line\":1},\"oft5\":{\"curlyName\":\"oft5\",\"line\":0},\"vorstossen\":{\"curlyName\":\"vorstossen\",\"line\":3,\"args\":{\"bis_in_Täler\":{\"curlyName\":\"bis_in_Täler\",\"line\":13}}}}}",
			AddOns.natural_release,
			"{\"curlyName\":\"Auslösung01\",\"line\":0,\"args\":{\"Zeit_optional1\":{\"curlyName\":\"Zeit_optional1\",\"line\":0},\"wieviele\":{\"curlyName\":\"wieviele\",\"line\":0},\"Lawinenart5\":{\"curlyName\":\"Lawinenart5\",\"line\":19},\"Auslösung01§möglich\":{\"curlyName\":\"Auslösung01§möglich\",\"line\":0},\"Auslösung01§auch_grosse\":{\"curlyName\":\"Auslösung01§auch_grosse\",\"line\":0}}}",
			AddOns.danger_signs,
			"{\"curlyName\":\"Altschnee03\",\"line\":0,\"args\":{\"Alarmzeichen\":{\"curlyName\":\"Alarmzeichen\",\"line\":0},\"Altschnee03§sowie_Alarmzeichen\":{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0},\"Altschnee03§weisen_auf_Gefahr_hin\":{\"curlyName\":\"Altschnee03§weisen_auf_Gefahr_hin\",\"line\":5}}}",
			AddOns.penetrate_deep_layers,
			"{\"curlyName\":\"Altschnee04\",\"line\":0,\"args\":{\"Altschnee04§Lawinen_können\":{\"curlyName\":\"Altschnee04§Lawinen_können\",\"line\":1,\"args\":{\"trockene\":{\"curlyName\":\"trockene\",\"line\":0},\"auch1\":{\"curlyName\":\"auch1\",\"line\":1},\"zudem_können\":{\"curlyName\":\"zudem_können\",\"line\":4}}},\"Altschnee04§Altschnee_Durchreissen\":{\"curlyName\":\"Altschnee04§Altschnee_Durchreissen\",\"line\":3},\"Altschnee04§und\":{\"curlyName\":\"Altschnee04§und\",\"line\":0},\"wo_gross_werden\":{\"curlyName\":\"wo_gross_werden\",\"line\":0}}}",
			AddOns.outflow_areas_to_consider,
			"{\"curlyName\":\"Empfehlung11\",\"line\":0,\"args\":{\"Empfehlung11§Absturzgefahr\":{\"curlyName\":\"Empfehlung11§Absturzgefahr\",\"line\":7,\"args\":{\"auch\":{\"curlyName\":\"auch\",\"line\":0},\"von_grossen_Lawinen\":{\"curlyName\":\"von_grossen_Lawinen\",\"line\":2}}}}}");

	public DangerSourceVariantText getDangerSourceVariantText(DangerSourceVariant dangerSourceVariant) {
		if (dangerSourceVariant == null || dangerSourceVariant.getAvalancheType() == null) {
			return null;
		}
		AvalancheType avalancheType = dangerSourceVariant.getAvalancheType();
		List<DangerSourceVariantText> result = dangerSourceVariantTextRepository.findByAvalancheType(avalancheType);
		return switch (dangerSourceVariant.getAvalancheType()) {
			case glide -> result.stream()
				.filter(text -> dangerSourceVariant.getGlidingSnowActivity() == null
					|| text.getGlidingSnowActivity() == dangerSourceVariant.getGlidingSnowActivity())
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null
					|| text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation()
					.getAvalancheSize())
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null
					|| text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
				.findFirst().orElse(null);
			case loose -> result.stream()
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null
					|| text.getAvalancheSize() == dangerSourceVariant.getEawsMatrixInformation()
					.getAvalancheSize())
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability() == null
					|| text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation()
					.getSnowpackStability())
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null
					|| text.getFrequency() == dangerSourceVariant.getEawsMatrixInformation().getFrequency())
				.findFirst().orElse(null);
			case slab -> result.stream()
				.filter(text -> text.getAvalancheProblem() != null
					&& text.getAvalancheProblem() == dangerSourceVariant.deriveAvalancheProblem())
				.filter(text ->
					// dsv true text true or null
					(Boolean.TRUE.equals(dangerSourceVariant.getHasDaytimeDependency())
						&& (text.getHasDaytimeDependency() == null
						|| Boolean.TRUE.equals(text.getHasDaytimeDependency())))
						||
						// dsv false text false or null
						(Boolean.FALSE.equals(dangerSourceVariant.getHasDaytimeDependency())
							&& (text.getHasDaytimeDependency() == null
							|| Boolean.FALSE.equals(text.getHasDaytimeDependency())))
						||
						// dsv null text false or null
						(dangerSourceVariant.getHasDaytimeDependency() == null
							&& (text.getHasDaytimeDependency() == null || !text.getHasDaytimeDependency())))
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == null
					|| (text.getAvalancheSize() != null && text.getAvalancheSize() == dangerSourceVariant
					.getEawsMatrixInformation().getAvalancheSize()))
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getSnowpackStability() == null
					|| (text.getSnowpackStability() != null
					&& text.getSnowpackStability() == dangerSourceVariant.getEawsMatrixInformation()
					.getSnowpackStability()))
				.filter(text -> dangerSourceVariant.getEawsMatrixInformation() == null
					|| dangerSourceVariant.getEawsMatrixInformation().getFrequency() == null
					|| (text.getFrequency() != null && text.getFrequency() == dangerSourceVariant
					.getEawsMatrixInformation().getFrequency()))
				.findFirst().orElse(null);
		};
	}

	// map of placeholder texts
	// key: phrase name
	// value: placeholder regex
	protected static final Map<String, String> textcatPlaceholder = Map.ofEntries(
			Map.entry("Gefahrenstellen05§an_Expositionen",
					"\\{\"curlyName\":\"Gefahrenstellen05§an_Expositionen\",\"line\":3,\"args\":\\{\"vor_allem_an\":\\{\"curlyName\":\"vor_allem_an\",\"line\":0\\},\"steilen\":\\{\"curlyName\":\"steilen\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
			Map.entry("es_warum_Ort_wann",
					"\\{\"curlyName\":\"es_warum_Ort_wann\",\"line\":27,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":0\\},\"vor_allem\":\\{\"curlyName\":\"vor_allem\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
			Map.entry("Hangart1",
					"\\{\"curlyName\":\"Hangart1\",\"line\":2,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
			Map.entry("Hangart_Höhe2",
					"\\{\"curlyName\":\"Hangart_Höhe2\",\"line\":2,\"args\":\\{\"an_steilen\":\\{\"curlyName\":\"an_steilen\",\"line\":0\\},\"Expo\":\\{\"curlyName\":\"Expo\",\"line\":2\\},\"Komma_Expo\":\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\},\"und_Expo\":\\{\"curlyName\":\"und_Expo\",\"line\":0\\}\\}\\}"),
			Map.entry("Expo", "\\{\"curlyName\":\"Expo\",\"line\":2\\}"),
			Map.entry("Komma_Expo", "\\{\"curlyName\":\"Komma_Expo\",\"line\":0\\}"),
			Map.entry("und_Expo", "\\{\"curlyName\":\"und_Expo\",\"line\":0\\}"),
			Map.entry("Höhe_Höhenlage", "\\{\"curlyName\":\"Höhe_Höhenlage\",\"line\":0\\}"),
			Map.entry("Höhenlage3", "\\{\"curlyName\":\"Höhenlage3\",\"line\":0\\}"),
			Map.entry("oberhalb_von_Höhe_optional", "\\{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":0\\}"),
			Map.entry("Höhe_m", "\\{\"curlyName\":\"Höhe_m\",\"line\":0\\}"),
			Map.entry("Schwachschichten", "\\{\"curlyName\":\"Schwachschichten\",\"line\":0\\}"),
			Map.entry("Gefahrenstellen02§und_erkennbar.",
					"\\{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":0,\"args\":\\{\"aber_und\":\\{\"curlyName\":\"aber_und\",\"line\":0\\}\\}\\}"),
			Map.entry("aber_und", "\\{\"curlyName\":\"aber_und\",\"line\":0\\}"),
			Map.entry("Auslösung07§möglich.", "\\{\"curlyName\":\"Auslösung07§möglich.\",\"line\":1\\}"),
			Map.entry("Geländeformen",
					"\\{\"curlyName\":\"Hangart1\",\"line\":9\\},\"Höhenlage3\":\\{\"curlyName\":\"Höhenlage3\",\"line\":0\\},\"Dies02§sowie\":\\{\"curlyName\":\"Dies02§sowie\",\"line\":0\\},\"Hangart1°1\":\\{\"curlyName\":\"Hangart1°1\",\"line\":0\\}"),
			Map.entry("trockene", "\\{\"curlyName\":\"Lawinenart5\",\"line\":12\\}"),
			Map.entry("Zeit", "\\{\"curlyName\":\"Zeit\",\"line\":0\\}"),
			Map.entry("Zeit_optional1", "\\{\"curlyName\":\"Zeit_optional1\",\"line\":0\\}"),
			Map.entry("an_steilen", "\\{\"curlyName\":\"an_steilen\",\"line\":0\\}"),
			Map.entry("steilen", "\\{\"curlyName\":\"steilen\",\"line\":0\\}"),
			Map.entry("möglich.",
					"\\{\"curlyName\":\"Auslösung01§möglich\",\"line\":0\\}"),
			Map.entry("Alarmzeichen",
					"\\{\"curlyName\":\"Alarmzeichen\",\"line\":0\\},\"Altschnee03§sowie_Alarmzeichen\":\\{\"curlyName\":\"Altschnee03§sowie_Alarmzeichen\",\"line\":0\\}"));

	public String getTextForDangerSourceVariant(DangerSourceVariant dangerSourceVariant) {
		return getTextForDangerSourceVariant(dangerSourceVariant, getDangerSourceVariantText(dangerSourceVariant));
	}

	public String getTextForDangerSourceVariant(DangerSourceVariant dangerSourceVariant,
			DangerSourceVariantText dangerSourceVariantText) {
		if (dangerSourceVariantText == null) {
			logger.warn("No danger source variant text found!");
			return "";
		}

		String textcatResult = dangerSourceVariantText.getTextcat();
		textcatResult = doReplacements(dangerSourceVariant, textcatResult);

		String addOnsResult = addAddOns(dangerSourceVariant);

		String result = concatTexts(textcatResult, addOnsResult);

		return result;
	}

	private String addAddOns(DangerSourceVariant dangerSourceVariant) {
		String result = "";
		if (dangerSourceVariant.getRemoteTriggering() != null) {
			String remoteTriggeringText = replaceRemoteTriggering(dangerSourceVariant.getRemoteTriggering(),
					addOnSentences.get(AddOns.remote_triggering));
			result = concatTexts(result, remoteTriggeringText);
		}
		if (dangerSourceVariant.getTerrainTypes() != null && !dangerSourceVariant.getTerrainTypes().isEmpty()) {
			String terrainTypesText = replaceTerrainTypes(dangerSourceVariant.getTerrainTypes(),
					addOnSentences.get(AddOns.terrain_features));
			result = concatTexts(result, terrainTypesText);
		}
		if (dangerSourceVariant.getDangerIncreaseWithElevation() != null
				&& dangerSourceVariant.getDangerIncreaseWithElevation()) {
			String dangerIncreaseWithElevationText = addOnSentences.get(AddOns.danger_increase_with_elevation);
			result = concatTexts(result, dangerIncreaseWithElevationText);
		}
		if (dangerSourceVariant.getHighestDangerAspect() != null) {
			String aspectsText = replaceAspects(
					dangerSourceVariant,
					java.util.Collections.singleton(dangerSourceVariant.getHighestDangerAspect()),
					addOnSentences.get(AddOns.highest_danger_aspect));
			result = concatTexts(result, aspectsText);
		}
		if (dangerSourceVariant.getDangerPeak() != null) {
			String dangerPeakText = replaceDangerPeak(dangerSourceVariant.getDangerPeak(),
					addOnSentences.get(AddOns.danger_peak));
			result = concatTexts(result, dangerPeakText);
		}
		if (dangerSourceVariant.getRunoutIntoGreen() != null && dangerSourceVariant.getRunoutIntoGreen()) {
			String runoutIntoGreenText = addOnSentences.get(AddOns.runout_into_green);
			result = concatTexts(result, runoutIntoGreenText);
		}
		if (dangerSourceVariant.getNaturalRelease() != null) {
			String naturalAvalanchesText = replaceNaturalAvalanches(dangerSourceVariant.getNaturalRelease(),
					addOnSentences.get(AddOns.natural_release));
			result = concatTexts(result, naturalAvalanchesText);
		}
		if (dangerSourceVariant.getDangerSigns() != null && !dangerSourceVariant.getDangerSigns().isEmpty()) {
			String dangerSignsText = replaceDangerSigns(dangerSourceVariant.getDangerSigns(),
					addOnSentences.get(AddOns.danger_signs));
			result = concatTexts(result, dangerSignsText);
		}
		if (dangerSourceVariant.getPenetrateDeepLayers() != null && dangerSourceVariant.getPenetrateDeepLayers()) {
			String penetrateDeepLayersText = addOnSentences.get(AddOns.penetrate_deep_layers);
			result = concatTexts(result, penetrateDeepLayersText);
		}
		if (dangerSourceVariant.getNaturalRelease() != null && dangerSourceVariant.getEawsMatrixInformation() != null
				&& (dangerSourceVariant.getNaturalRelease() == Probability.possible
						|| dangerSourceVariant.getNaturalRelease() == Probability.likely)
				&&
				((dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == AvalancheSize.large) ||
						(dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == AvalancheSize.very_large)
						||
						(dangerSourceVariant.getEawsMatrixInformation().getAvalancheSize() == AvalancheSize.extreme))) {
			String outflowAreasToConsiderText = addOnSentences.get(AddOns.outflow_areas_to_consider);
			result = concatTexts(result, outflowAreasToConsiderText);
		}

		return result;
	}

	private String concatTexts(String first, String second) {
		if (second != null && !second.isEmpty()) {
			if (first != null && !first.isEmpty()) {
				first = first + "," + second;
			} else {
				first = second;
			}
		}
		return first;
	}

	private String doReplacements(DangerSourceVariant dangerSourceVariant, String result) {
		// ASPECTS
		result = replaceAspects(dangerSourceVariant, dangerSourceVariant.getAspects(), result);

		// ELEVATION
		result = replaceElevations(dangerSourceVariant.getTreelineHigh(), dangerSourceVariant.getTreelineLow(),
				dangerSourceVariant.getElevationHigh(), dangerSourceVariant.getElevationLow(), result);

		// POSITION OF THE WEAK LAYER
		result = replacePositionOfWeakLayer(dangerSourceVariant, result);

		// RECOGNIZABILITY
		result = replaceRecognizability(dangerSourceVariant.getDangerSpotRecognizability(),
				dangerSourceVariant.getEawsMatrixInformation(), result);

		// REMOTE TRIGGERING
		result = replaceRemoteTriggering(dangerSourceVariant.getRemoteTriggering(), result);

		// TERRAIN TYPES
		result = replaceTerrainTypes(dangerSourceVariant.getTerrainTypes(), result);

		// WETNESS
		result = replaceWetness(result, dangerSourceVariant.getLooseSnowMoisture());

		// DANGER PEAK
		result = replaceDangerPeak(dangerSourceVariant.getDangerPeak(), result);

		// STEEPNESS
		result = replaceSteepness(dangerSourceVariant.getSlopeGradient(), result);

		// NATURAL AVALANCHES
		result = replaceNaturalAvalanches(dangerSourceVariant.getNaturalRelease(), result);

		// DANGER SIGNS
		result = replaceDangerSigns(dangerSourceVariant.getDangerSigns(), result);
		return result;
	}

	private String replaceDangerSigns(Set<DangerSign> dangerSigns, String result) {
		// phrase: Alarmzeichen
		for (Map.Entry<Set<DangerSign>, String> entry : textcatSubstitutionsDangerSigns.entrySet()) {
			if (entry.getKey().size() == dangerSigns.size() && entry.getKey().containsAll(dangerSigns)) {
				result = result.replaceAll(textcatPlaceholder.get("Alarmzeichen"), entry.getValue());
				break;
			}
		}
		return result;
	}

	private String replaceNaturalAvalanches(Probability naturalRelease, String result) {
		// phrase: möglich
		if (naturalRelease != null) {
			switch (naturalRelease) {
				case likely:
					result = result.replaceAll(textcatPlaceholder.get("möglich."),
							"{\"curlyName\":\"Auslösung01§möglich\",\"line\":1}");
					break;
				case possible:
					result = result.replaceAll(textcatPlaceholder.get("möglich."),
							"{\"curlyName\":\"Auslösung01§möglich\",\"line\":0}");
					break;
				case unlikely:
					result = result.replaceAll(textcatPlaceholder.get("möglich."),
							"{\"curlyName\":\"Auslösung01§möglich\",\"line\":0}");
					break;
				default:
					break;
			}
		}
		return result;
	}

	private String replaceSteepness(SlopeGradient slopeGradient, String result) {
		// phrase: an_steilen
		if (slopeGradient != null) {
			switch (slopeGradient) {
				case moderately_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"),
							"{\"curlyName\":\"an_steilen\",\"line\":0}");
					break;
				case steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"),
							"{\"curlyName\":\"an_steilen\",\"line\":1}");
					break;
				case very_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"),
							"{\"curlyName\":\"an_steilen\",\"line\":2}");
					break;
				case extremely_steep:
					result = result.replaceAll(textcatPlaceholder.get("an_steilen"),
							"{\"curlyName\":\"an_steilen\",\"line\":3}");
					break;
				default:
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("an_steilen"),
					"{\"curlyName\":\"an_steilen\",\"line\":0}");
		}
		// phrase: steilen
		if (slopeGradient != null) {
			switch (slopeGradient) {
				case moderately_steep:
					result = result.replaceAll(textcatPlaceholder.get("steilen"),
							"{\"curlyName\":\"steilen\",\"line\":0}");
					break;
				case steep:
					result = result.replaceAll(textcatPlaceholder.get("steilen"),
							"{\"curlyName\":\"steilen\",\"line\":1}");
					break;
				case very_steep:
					result = result.replaceAll(textcatPlaceholder.get("steilen"),
							"{\"curlyName\":\"steilen\",\"line\":2}");
					break;
				case extremely_steep:
					result = result.replaceAll(textcatPlaceholder.get("steilen"),
							"{\"curlyName\":\"steilen\",\"line\":3}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("steilen"),
							"{\"curlyName\":\"steilen\",\"line\":0}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("steilen"), "{\"curlyName\":\"steilen\",\"line\":0}");
		}
		return result;
	}

	private String replaceDangerPeak(Daytime dangerPeak, String result) {
		// phrase: Zeit
		if (dangerPeak != null) {
			switch (dangerPeak) {
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
		if (dangerPeak != null) {
			switch (dangerPeak) {
				case evening:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":18}");
					break;
				case morning:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":10}");
					break;
				case forenoon:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":12}");
					break;
				case afternoon:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":17}");
					break;
				case first_night_half:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":5}");
					break;
				case second_night_half:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":6}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Zeit_optional1"),
							"{\"curlyName\":\"Zeit_optional1\",\"line\":0}");
					break;
			}
		}

		return result;
	}

	private String replaceWetness(String result, Wetness looseSnowMoisture) {
		// phrase: {trockene}
		if (looseSnowMoisture != null) {
			switch (looseSnowMoisture) {
				case dry:
					result = result.replaceAll(textcatPlaceholder.get("trockene"),
							"{\"curlyName\":\"Lawinenart5\",\"line\":12}");
					break;
				case moist:
					result = result.replaceAll(textcatPlaceholder.get("trockene"),
							"{\"curlyName\":\"Lawinenart5\",\"line\":14}");
					break;
				case wet:
					result = result.replaceAll(textcatPlaceholder.get("trockene"),
							"{\"curlyName\":\"Lawinenart5\",\"line\":15}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("trockene"),
							"{\"curlyName\":\"Lawinenart5\",\"line\":12}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("trockene"),
					"{\"curlyName\":\"Lawinenart5\",\"line\":12}");
		}
		return result;
	}

	private String replaceTerrainTypes(Set<TerrainType> terrainTypes, String result) {
		// phrase: Hangart1
		if (terrainTypes == null || terrainTypes.isEmpty()) {
			Set<TerrainType> bestMatchKey = null;
			int maxCommon = 0;
			for (Set<TerrainType> key : textcatSubstitutionsTerrainTypes.keySet()) {
				int common = 0;
				for (TerrainType t : terrainTypes) {
					if (key.contains(t)) {
						common++;
					}
				}
				if (common > maxCommon && common == key.size()) {
					maxCommon = common;
					bestMatchKey = key;
				}
			}

			if (bestMatchKey != null) {
				result = result.replaceAll(textcatPlaceholder.get("Geländeformen"),
						textcatSubstitutionsTerrainTypes.get(bestMatchKey));
			}
		}

		return result;
	}

	private String replaceRemoteTriggering(Probability remoteTriggering, String result) {
		// phrase: Auslösung07§möglich.
		if (remoteTriggering != null) {
			switch (remoteTriggering) {
				case likely:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."),
							"{\"curlyName\":\"Auslösung07§möglich.\",\"line\":2}");
					break;
				case possible:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."),
							"{\"curlyName\":\"Auslösung07§möglich.\",\"line\":1}");
					break;
				case unlikely:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."),
							"{\"curlyName\":\"Auslösung07§möglich.\",\"line\":0}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Auslösung07§möglich."),
							"{\"curlyName\":\"Auslösung07§möglich.\",\"line\":0}");
					break;
			}
		}
		return result;
	}

	private String replaceRecognizability(Recognizability dangerSpotRecognizability,
			EawsMatrixInformation eawsMatrixInformation, String result) {
		// phrase: Gefahrenstellen02§und_erkennbar.
		if (dangerSpotRecognizability != null) {
			switch (dangerSpotRecognizability) {
				case very_easy:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."),
							"{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":1,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				case easy:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."),
							"{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":2,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				case hard:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."),
							"{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":3,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				case very_hard:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."),
							"{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":6,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."),
							"{\"curlyName\":\"Gefahrenstellen02§und_erkennbar.\",\"line\":0,\"args\":{\"aber_und\":{\"curlyName\":\"aber_und\",\"line\":0}}}");
					break;
			}

			if (eawsMatrixInformation != null
					&& eawsMatrixInformation.getFrequency() != null) {
				// replace aber_und
				if ((eawsMatrixInformation.getFrequency() == Frequency.few
						&& dangerSpotRecognizability == Recognizability.very_easy) ||
						(eawsMatrixInformation.getFrequency() == Frequency.few
								&& dangerSpotRecognizability == Recognizability.easy)
						||
						(eawsMatrixInformation.getFrequency() == Frequency.many
								&& dangerSpotRecognizability == Recognizability.hard)
						||
						(eawsMatrixInformation.getFrequency() == Frequency.many
								&& dangerSpotRecognizability == Recognizability.very_hard)) {
					// und
					result = result.replaceAll(textcatPlaceholder.get("aber_und"),
							"{\"curlyName\":\"aber_und\",\"line\":2}");
				} else if ((eawsMatrixInformation.getFrequency() == Frequency.few
						&& dangerSpotRecognizability == Recognizability.hard) ||
						(eawsMatrixInformation.getFrequency() == Frequency.few
								&& dangerSpotRecognizability == Recognizability.very_hard)
						||
						(eawsMatrixInformation.getFrequency() == Frequency.many
								&& dangerSpotRecognizability == Recognizability.easy)
						||
						(eawsMatrixInformation.getFrequency() == Frequency.many
								&& dangerSpotRecognizability == Recognizability.very_easy)) {
					// aber
					result = result.replaceAll(textcatPlaceholder.get("aber_und"),
							"{\"curlyName\":\"aber_und\",\"line\":1}");
				}

				switch (eawsMatrixInformation.getFrequency()) {
					case few:
						result = result.replaceAll(textcatPlaceholder.get("aber_und"),
								"{\"curlyName\":\"aber_und\",\"line\":0}");
						break;
					case many:
						result = result.replaceAll(textcatPlaceholder.get("aber_und"),
								"{\"curlyName\":\"aber_und\",\"line\":2}");
						break;
					default:
						result = result.replaceAll(textcatPlaceholder.get("aber_und"),
								"{\"curlyName\":\"aber_und\",\"line\":0}");
						break;
				}
			}

		} else {
			if (eawsMatrixInformation == null
					|| eawsMatrixInformation.getFrequency() == null) {
				result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen02§und_erkennbar."), "");
			}
		}
		return result;
	}

	private String replacePositionOfWeakLayer(DangerSourceVariant dangerSourceVariant, String result) {
		// phrase: Schwachschichten
		if (dangerSourceVariant.getWeakLayerPosition() != null) {
			switch (dangerSourceVariant.getWeakLayerPosition()) {
				case upper:
					result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"),
							"{\"curlyName\":\"Schwachschichten\",\"line\":8}");
					break;
				case lower:
					result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"),
							"{\"curlyName\":\"Schwachschichten\",\"line\":6}");
					break;
				case middle:
					result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"),
							"{\"curlyName\":\"Schwachschichten\",\"line\":0}");
					break;
				case ground:
					result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"),
							"{\"curlyName\":\"Schwachschichten\",\"line\":5}");
					break;
				default:
					result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"),
							"{\"curlyName\":\"Schwachschichten\",\"line\":0}");
					break;
			}
		} else {
			result = result.replaceAll(textcatPlaceholder.get("Schwachschichten"),
					"{\"curlyName\":\"Schwachschichten\",\"line\":0}");
		}
		return result;
	}

	private String replaceElevations(Boolean treelineHigh, Boolean treelineLow, Integer elevationHigh,
			Integer elevationLow, String result) {
		// phrase: Höhe_Höhenlage
		if (elevationHigh != null || (treelineHigh != null && treelineHigh)) {
			if (elevationHigh != null) {
				// below [m]
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"),
						"{\"curlyName\":\"Höhe_Höhenlage\",\"line\":2,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
			} else {
				// below treeline
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"),
						"{\"curlyName\":\"Höhe_Höhenlage\",\"line\":12}");
			}
		} else if (elevationLow != null || (treelineLow != null && treelineLow)) {
			if (elevationLow != null) {
				// above [m]
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"),
						"{\"curlyName\":\"Höhe_Höhenlage\",\"line\":1,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
			} else {
				// above treeline
				result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"),
						"{\"curlyName\":\"Höhe_Höhenlage\",\"line\":13}");
			}
		} else {
			// no elevation information
			result = result.replaceAll(textcatPlaceholder.get("Höhe_Höhenlage"),
					"{\"curlyName\":\"Höhe_Höhenlage\",\"line\":0}");
		}

		// phrase: Höhenlage3
		if (elevationHigh != null) {
			// below [m]
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"),
					"{\"curlyName\":\"Höhenlage3\",\"line\":2,\"args\":{\"und_vor_allem\":{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else if (elevationLow != null) {
			// above [m]
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"),
					"{\"curlyName\":\"Höhenlage3\",\"line\":1,\"args\":{\"und_vor_allem\":{\"curlyName\":\"und_vor_allem\",\"line\":0},\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else {
			// no elevation information
			result = result.replaceAll(textcatPlaceholder.get("Höhenlage3"),
					"{\"curlyName\":\"Höhenlage3\",\"line\":0}");
		}

		// phrase: oberhalb_von_Höhe_optional
		if (elevationHigh != null) {
			// below [m]
			result = result.replaceAll(textcatPlaceholder.get("oberhalb_von_Höhe_optional"),
					"{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":2,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else if (elevationLow != null) {
			// above [m]
			result = result.replaceAll(textcatPlaceholder.get("oberhalb_von_Höhe_optional"),
					"{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":1,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}");
		} else {
			// no elevation information
			result = result.replaceAll(textcatPlaceholder.get("oberhalb_von_Höhe_optional"),
					"{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":0}");
		}

		// phrase: Höhe_m
		if (elevationHigh != null) {
			for (Map.Entry<Integer, String> entry : textcatSubstitutionsElevation1.entrySet()) {
				if (elevationHigh == entry.getKey().intValue()) {
					result = result.replaceAll(textcatPlaceholder.get("Höhe_m"), entry.getValue());
					break;
				}
			}
		}
		if (elevationLow != null) {
			for (Map.Entry<Integer, String> entry : textcatSubstitutionsElevation1.entrySet()) {
				if (elevationLow == entry.getKey().intValue()) {
					result = result.replaceAll(textcatPlaceholder.get("Höhe_m"), entry.getValue());
					break;
				}
			}
		}
		return result;
	}

	private String replaceAspects(DangerSourceVariant dangerSourceVariant, Set<Aspect> aspects, String result) {
		if (aspects != null && !aspects.isEmpty()) {
			// phrase: Gefahrenstellen05§an_Expositionen
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects1.entrySet()) {
				if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("Gefahrenstellen05§an_Expositionen"),
							entry.getValue());
					break;
				}
			}
			// phrase: es_warum_Ort_wann
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects2.entrySet()) {
				if (entry.getKey().size() == 8 && entry.getKey().containsAll(aspects)
						&& dangerSourceVariant.getAvalancheType() == AvalancheType.glide) {
					result = result.replaceAll(textcatPlaceholder.get("es_warum_Ort_wann"),
							"{\"curlyName\":\"es_warum_Ort_wann\",\"line\":30,\"args\":{\"vor_allem\":{\"curlyName\":\"vor_allem\",\"line\":0},\"an_steilen\":{\"curlyName\":\"an_steilen\",\"line\":0}}}");
					break;
				} else if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
					result = result.replaceAll(textcatPlaceholder.get("es_warum_Ort_wann"), entry.getValue());
					break;
				}
			}
			// phrase: Hangart1
			for (Map.Entry<Set<Aspect>, String> entry : textcatSubstitutionsAspects3.entrySet()) {
				if (entry.getKey().size() == 8 && entry.getKey().containsAll(aspects)
						&& dangerSourceVariant.getAvalancheType() == AvalancheType.glide) {
					result = result.replaceAll(textcatPlaceholder.get("Hangart1"),
							"{\"curlyName\":\"Hangart1\",\"line\":5}");
					break;
				} else if (entry.getKey().size() == aspects.size() && entry.getKey().containsAll(aspects)) {
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
				if (!sortedAspects.isEmpty()) {
					if (entry.getKey() == sortedAspects.getFirst()) {
						result = result.replaceAll(textcatPlaceholder.get("Expo"), entry.getValue());
						break;
					}
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
					result = result.replaceAll(textcatPlaceholder.get("Komma_Expo"),
							"{\"curlyName\":\"Komma_Expo\",\"line\":0}");
					break;
				}
			}
			// phrase: und_Expo
			for (Map.Entry<Aspect, String> entry : textcatSubstitutionsAspect3.entrySet()) {
				if (sortedAspects.size() > 2) {
					if (entry.getKey() == sortedAspects.get(2)) {
						result = result.replaceAll(textcatPlaceholder.get("und_Expo"), entry.getValue());
						break;
					}
				} else if (sortedAspects.size() > 1) {
					if (entry.getKey() == sortedAspects.get(1)) {
						result = result.replaceAll(textcatPlaceholder.get("und_Expo"), entry.getValue());
						break;
					}
				} else {
					result = result.replaceAll(textcatPlaceholder.get("und_Expo"),
							"{\"curlyName\":\"und_Expo\",\"line\":0}");
					break;
				}
			}
		}
		return result;
	}
}

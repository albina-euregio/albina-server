// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.albina.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.DangerSourceVariant;
import eu.albina.model.enumerations.LanguageCode;

public class StatisticsControllerTest {

	private List<AvalancheBulletin> bulletinsAmPm;
	private List<DangerSourceVariant> dangerSourceVariants;

	public static DangerSourceVariant readDangerSourceVariant(final URL resource) throws IOException {
		final String json = Resources.toString(resource, StandardCharsets.UTF_8);
		return JsonUtil.parseUsingJackson(json, DangerSourceVariant.class);
	}

	@BeforeEach
	public void setUp() throws Exception {
		bulletinsAmPm = new ArrayList<>();
		bulletinsAmPm.addAll(AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2030-02-16_1.json")));
		bulletinsAmPm.addAll(AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2030-02-16_6.json")));
		dangerSourceVariants = Arrays.asList(readDangerSourceVariant(Resources.getResource("danger_source_variants.json")));
	}

	@Test
	public void getAvalancheBulletinCsv() throws IOException {
		final String expected = Resources.toString(Resources.getResource("2030-02-16.statistics.csv"),
				StandardCharsets.UTF_8).replaceAll("\r?\n", StatisticsController.csvLineBreak);
		String csvString = StatisticsController.getAvalancheBulletinCsvString(LanguageCode.de, bulletinsAmPm, false, false, true);
		Assertions.assertEquals(expected, csvString);
	}

	@Test
	public void getExtendedAvalancheBulletinCsv() throws IOException {
		final String expected = Resources.toString(Resources.getResource("2030-02-16.statistics.extended.csv"),
				StandardCharsets.UTF_8).replaceAll("\r?\n", StatisticsController.csvLineBreak);
		String csvString = StatisticsController.getAvalancheBulletinCsvString(LanguageCode.de, bulletinsAmPm, true, false, true);
		Assertions.assertEquals(expected, csvString);
	}

	@Test
	public void getDangerSourceVariantCsv() throws IOException {
		final String expected = Resources.toString(Resources.getResource("danger_source_variants.statistics.csv"),
				StandardCharsets.UTF_8).replaceAll("\r?\n", StatisticsController.csvLineBreak);
		String csvString = StatisticsController.getDangerSourceVariantsCsvString(dangerSourceVariants);
		Assertions.assertEquals(expected, csvString);
	}
}

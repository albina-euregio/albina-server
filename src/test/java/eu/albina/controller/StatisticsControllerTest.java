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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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

	@BeforeEach
	public void setUp() throws Exception {
		bulletinsAmPm = Arrays.asList(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_1.json")),
				AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_2.json")),
				AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_3.json")),
				AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_4.json")),
				AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_5.json")),
				AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_6.json")),
				AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_7.json")));

		dangerSourceVariants = Arrays.asList(DangerSourceVariant.readDangerSourceVariant(Resources.getResource("danger_source_variants.json")));
	}

	@Test
	public void getAvalancheBulletinCsv() throws IOException {
		final String expected = Resources.toString(Resources.getResource("2030-02-16.statistics.csv"),
				StandardCharsets.UTF_8).replaceAll("\r?\n", StatisticsController.csvLineBreak);
		String csvString = StatisticsController.getInstance().getAvalancheBulletinCsvString(LanguageCode.de, bulletinsAmPm, false, false, true);
		Assertions.assertEquals(expected, csvString);
	}

	@Test
	public void getExtendedAvalancheBulletinCsv() throws IOException {
		final String expected = Resources.toString(Resources.getResource("2030-02-16.statistics.extended.csv"),
				StandardCharsets.UTF_8).replaceAll("\r?\n", StatisticsController.csvLineBreak);
		String csvString = StatisticsController.getInstance().getAvalancheBulletinCsvString(LanguageCode.de, bulletinsAmPm, true, false, true);
		Assertions.assertEquals(expected, csvString);
	}

	@Test
	public void getDangerSourceVariantCsv() throws IOException {
		final String expected = Resources.toString(Resources.getResource("danger_source_empty.statistics.csv"),
				StandardCharsets.UTF_8).replaceAll("\r?\n", StatisticsController.csvLineBreak);
		String csvString = StatisticsController.getInstance().getDangerSourceVariantsCsvString(dangerSourceVariants);
		Assertions.assertEquals(expected, csvString);
	}
}

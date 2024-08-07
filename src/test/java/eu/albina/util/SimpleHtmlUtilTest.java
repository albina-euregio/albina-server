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
package eu.albina.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import eu.albina.model.AvalancheReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import static eu.albina.RegionTestUtils.regionAran;
import static eu.albina.RegionTestUtils.regionEuregio;
import static eu.albina.RegionTestUtils.regionTyrol;

public class SimpleHtmlUtilTest {

	private ServerInstance serverInstanceEuregio;
	private ServerInstance serverInstanceAran;

	@BeforeEach
	public void setUp() throws Exception {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setName("ALBINA-TEST");
		serverInstanceEuregio.setHtmlDirectory("/mnt/simple_local/");
		serverInstanceEuregio.setMapsPath("/mnt/bulletins/");
		serverInstanceEuregio.setServerImagesUrl("https://static.avalanche.report/images/");
		serverInstanceEuregio.setHtmlDirectory("/mnt/simple_local");
		serverInstanceAran = new ServerInstance();
		serverInstanceAran.setName("ALBINA-TEST");
		serverInstanceAran.setHtmlDirectory("/mnt/simple_local/");
		serverInstanceAran.setMapsPath("/mnt/albina_files_local/");
		serverInstanceAran.setServerImagesUrl("https://static.lauegi.report/images/");
		serverInstanceAran.setHtmlDirectory("/mnt/simple_local");
	}

	@Test
	public void createSimpleHtmlString() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.de).replaceAll("\\s*<", "\n<");
		String expected = Resources.toString(Resources.getResource("2019-01-17.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringTyrol() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final List<AvalancheBulletin> bulletinsTyrol = bulletins.stream()
			.filter(avalancheBulletin -> avalancheBulletin.affectsRegionOnlyPublished(regionTyrol))
			.collect(Collectors.toList());
		AvalancheReport avalancheReport = AvalancheReport.of(bulletinsTyrol, regionTyrol, serverInstanceEuregio);
		avalancheReport.setBulletins(bulletinsTyrol, bulletins);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.de).replaceAll("\\s*<", "\n<");
		Assertions.assertFalse(htmlString.contains("853733e5-cb48-4cf1-91a2-ebde59dda31f")); // IT-32-TN
	}

	@Test
	public void createSimpleHtmlStringAran() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("lauegi.report-2021-01-24/2021-01-24.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.ca);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-01-24/2021-01-24.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringAranDaytimeDependency() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String html = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.en);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-12-10/2021-12-10.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), html.trim());
	}
}


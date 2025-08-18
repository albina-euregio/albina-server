// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import static eu.albina.RegionTestUtils.regionAran;
import static eu.albina.RegionTestUtils.regionEuregio;
import static eu.albina.RegionTestUtils.regionTyrol;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.TemplateException;

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
		regionEuregio.setServerInstance(serverInstanceEuregio);
		regionTyrol.setServerInstance(serverInstanceEuregio);
		regionAran.setServerInstance(serverInstanceAran);
	}

	@Test
	public void createSimpleHtmlString() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.de).replaceAll("\\s*<", "\n<");
		String expected = Resources.toString(Resources.getResource("2019-01-17.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringTyrol() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
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
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.ca);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-01-24/2021-01-24.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringAranDaytimeDependency() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String html = SimpleHtmlUtil.getInstance().createSimpleHtmlString(avalancheReport, LanguageCode.en);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-12-10/2021-12-10.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), html.trim());
	}
}


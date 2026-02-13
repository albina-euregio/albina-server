// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.Region;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.TemplateException;

@MicronautTest
public class SimpleHtmlUtilTest {

	@Inject
	AvalancheBulletinTestUtils avalancheBulletinTestUtils;

	@Inject
	RegionTestUtils regionTestUtils;

	private LocalServerInstance serverInstanceEuregio;
	private LocalServerInstance serverInstanceAran;
	private Region regionEuregio;
	private Region regionTyrol;
	private Region regionAran;

	@BeforeEach
	public void setUp() throws Exception {
		serverInstanceEuregio = new LocalServerInstance(false, false, "/mnt/bulletins/", null, null, "/mnt/simple_local/", null);
		serverInstanceAran = new LocalServerInstance(false, false, "/mnt/albina_files_local/", null, null, "/mnt/simple_local/", null);
		regionEuregio = regionTestUtils.regionEuregio();
		regionTyrol = regionTestUtils.regionTyrol();
		regionAran = regionTestUtils.regionAran();
	}

	@Test
	public void createSimpleHtmlString() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		String htmlString = SimpleHtmlUtil.createSimpleHtmlString(avalancheReport, LanguageCode.de).replaceAll("\\s*<", "\n<");
		String expected = Resources.toString(Resources.getResource("2019-01-17.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringTyrol() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final List<AvalancheBulletin> bulletinsTyrol = bulletins.stream()
			.filter(avalancheBulletin -> avalancheBulletin.affectsRegionOnlyPublished(regionTyrol))
			.collect(Collectors.toList());
		AvalancheReport avalancheReport = AvalancheReport.of(bulletinsTyrol, regionTyrol, serverInstanceEuregio);
		avalancheReport.setBulletins(bulletinsTyrol, bulletins);
		String htmlString = SimpleHtmlUtil.createSimpleHtmlString(avalancheReport, LanguageCode.de).replaceAll("\\s*<", "\n<");
		Assertions.assertFalse(htmlString.contains("853733e5-cb48-4cf1-91a2-ebde59dda31f")); // IT-32-TN
	}

	@Test
	public void createSimpleHtmlStringAran() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("lauegi.report-2021-01-24/2021-01-24.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String htmlString = SimpleHtmlUtil.createSimpleHtmlString(avalancheReport, LanguageCode.ca);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-01-24/2021-01-24.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringAranDaytimeDependency() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String html = SimpleHtmlUtil.createSimpleHtmlString(avalancheReport, LanguageCode.en);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-12-10/2021-12-10.simple.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), html.trim());
	}
}


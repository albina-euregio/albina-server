// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

@MicronautTest
public class EmailUtilTest {

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
	public void setUp() throws IOException {
		serverInstanceEuregio = new LocalServerInstance(false, false, "/mnt/bulletins/", null, "/mnt/simple_local/", null, null);
		serverInstanceAran = new LocalServerInstance(false, false, "/mnt/albina_files_local/", null, "/mnt/simple_local/", null, null);
		regionEuregio = regionTestUtils.regionEuregio();
		regionTyrol = regionTestUtils.regionTyrol();
		regionTyrol.setServerImagesUrl("/mnt/images/");
		regionAran = regionTestUtils.regionAran();
	}

	private static void assertResourceEquals(String resourceName, String html) throws IOException {
		String expected = Resources.toString(Resources.getResource(resourceName), StandardCharsets.UTF_8);
		// java.nio.file.Files.writeString(java.nio.file.Path.of("src/test/resources/" + resourceName), html);
		Assertions.assertEquals(expected.trim(), html.trim());
	}

	@Test
	public void createBulletinEmailHtml() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de).replace("\n", "");
		assertResourceEquals("2019-01-17.mail.html", html);
	}

	@Test
	public void createBulletinEmailHtmlAran() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.en).replace("\n", "");
		assertResourceEquals("lauegi.report-2021-12-10/2021-12-10.mail.html", html);
	}

	@Test
	public void createBulletinEmailHtml2026() throws Exception {
		final URL resource = Resources.getResource("2026-04-13.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de).replace("\n", "");
		assertResourceEquals("2026-04-13.mail.html", html);
	}

	@Test
	public void createBulletinEmailHtml2021() throws Exception {
		final URL resource = Resources.getResource("2021-12-01.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de).replace("\n", "");
		assertResourceEquals("2021-12-01.mail.html", html);
	}

	@Test
	public void langTest() {
		Assertions.assertEquals("Alle Höhenlagen", LanguageCode.de.getCaamlBundleString("elevation.all"));
		Assertions.assertEquals("Tutte le quote", LanguageCode.it.getCaamlBundleString("elevation.all"));
		Assertions.assertEquals("All elevations", LanguageCode.en.getCaamlBundleString("elevation.all"));
	}
}

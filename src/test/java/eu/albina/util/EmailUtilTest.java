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
import eu.albina.model.enumerations.DangerRating;
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
	private Region regionVorarlberg;
	private Region regionTyrol;
	private Region regionAran;

	@BeforeEach
	public void setUp() throws IOException {
		serverInstanceEuregio = new LocalServerInstance(false, false, "/mnt/bulletins/", null, "/mnt/simple_local/", null, null);
		serverInstanceAran = new LocalServerInstance(false, false, "/mnt/albina_files_local/", null, "/mnt/simple_local/", null, null);
		regionEuregio = regionTestUtils.regionEuregio();
		regionEuregio.setServerImagesUrl("https://admin.avalanche.report/images/");
		regionVorarlberg = regionTestUtils.regionVorarlberg();
		regionTyrol = regionTestUtils.regionTyrol();
		regionTyrol.setServerImagesUrl("https://admin.avalanche.report/images/");
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
	public void createBulletinEmailHtmlVorarlberg() throws Exception {
		final URL resource = Resources.getResource("2026-04-13.AT-08.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionVorarlberg, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de).replace("\n", "");
		assertResourceEquals("2026-04-13.AT-08.mail.html", html);
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
	public void createBulletinEmailHtmlWithoutDangerRating() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		bulletins.forEach(bulletin -> {
			bulletin.getForenoon().setHasElevationDependency(false);
			bulletin.getForenoon().setDangerRatingAbove(DangerRating.missing);
			bulletin.getForenoon().setDangerRatingBelow(null);
		});
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de);
		Assertions.assertTrue(html.contains("warning_pictos/color/level_0_0.png"), "level_0_0");
		Assertions.assertTrue(html.contains("Keine Beurteilung"), "Keine Beurteilung");
		Assertions.assertFalse(html.contains("bgcolor=\"#969696\""), "no colour bar");
	}

	/** Outlook renders neither of these, so the mail must not depend on them. */
	@Test
	public void outlookCompatibility() throws Exception {
		final URL resource = Resources.getResource("2026-04-13.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de);
		for (String unsupported : List.of("display: flex", "display: grid", "var(--", "data:image", ".webp")) {
			Assertions.assertFalse(html.contains(unsupported), unsupported);
		}
		Assertions.assertEquals(
			html.split("<table", -1).length,
			html.split("<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"", -1).length,
			"every layout table needs the presentational attributes");
		Assertions.assertEquals(
			html.split("<img", -1).length,
			html.split(" alt=\"", -1).length,
			"every image needs an alt attribute");
	}

	@Test
	public void langTest() {
		Assertions.assertEquals("Alle Höhenlagen", LanguageCode.de.getCaamlBundleString("elevation.all"));
		Assertions.assertEquals("Tutte le quote", LanguageCode.it.getCaamlBundleString("elevation.all"));
		Assertions.assertEquals("All elevations", LanguageCode.en.getCaamlBundleString("elevation.all"));
	}
}

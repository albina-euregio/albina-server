// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import static eu.albina.RegionTestUtils.regionAran;
import static eu.albina.RegionTestUtils.regionTyrol;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

public class EmailUtilTest {

	private ServerInstance serverInstanceEuregio;
	private ServerInstance serverInstanceAran;

	@BeforeEach
	public void setUp() throws IOException {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setServerImagesUrl("/mnt/images/");
		serverInstanceEuregio.setMapsPath("/mnt/bulletins/");
		serverInstanceEuregio.setPdfDirectory("/mnt/bulletins/");
		serverInstanceEuregio.setMediaPath("/mnt/media/");
		serverInstanceAran = new ServerInstance();
		serverInstanceAran.setServerImagesUrl("https://static.lauegi.report/images/");
		serverInstanceAran.setMapsPath("/mnt/albina_files_local/");
		serverInstanceAran.setPdfDirectory("/mnt/albina_files_local/");
		regionTyrol.setServerInstance(serverInstanceEuregio);
		regionAran.setServerInstance(serverInstanceAran);
	}

	@Test
	public void createBulletinEmailHtml() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de);
		Assertions.assertEquals(154.7177734375, html.getBytes(StandardCharsets.UTF_8).length / 1024., 1., "155 kB");
		Assertions.assertTrue(html.contains("<h2 style=\"margin-bottom: 5px\">Donnerstag, 17. Jänner 2019</h2>"));
		Assertions.assertTrue(html.contains("Veröffentlicht am <b>16.01.2019, 17:00:00</b>"));
		Assertions.assertTrue(html.contains("href=\"https://lawinen.report/bulletin/2019-01-17\""));
		Assertions.assertTrue(html.contains("Tendenz: Lawinengefahr nimmt ab</p><p style=\"text-align: left; margin-bottom: 0;\">am Freitag, 18. Jänner 2019"));
		Assertions.assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/fd_AT-07_map.jpg"));
		Assertions.assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/AT-07_6385c958-018d-4c89-aa67-5eddc31ada5a.jpg"));
	}

	@Test
	public void createBulletinEmailHtmlAran() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.en);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-12-10/2021-12-10.mail.html"), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected.trim(), html.trim());
	}

	@Test
	public void createBulletinEmailHtml2021() throws Exception {
		final URL resource = Resources.getResource("2021-12-01.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.createBulletinEmailHtml(avalancheReport, LanguageCode.de);
		Assertions.assertEquals(58, html.getBytes(StandardCharsets.UTF_8).length / 1024, "59 kB");
	}

	@Test
	public void langTest() {
		Assertions.assertEquals("Alle Höhenlagen", LanguageCode.de.getBundleString("elevation.all"));
		Assertions.assertEquals("Tutte le quote", LanguageCode.it.getBundleString("elevation.all"));
		Assertions.assertEquals("All elevations", LanguageCode.en.getBundleString("elevation.all"));
	}
}

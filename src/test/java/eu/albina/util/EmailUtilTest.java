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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import eu.albina.model.AvalancheReport;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

import static eu.albina.RegionTestUtils.regionAran;
import static eu.albina.RegionTestUtils.regionTyrol;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailUtilTest {

	private ServerInstance serverInstanceEuregio;
	private ServerInstance serverInstanceAran;

	@Before
	public void setUp() throws IOException {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setServerImagesUrl("/mnt/images/");
		serverInstanceEuregio.setMapsPath("/mnt/bulletins/");
		serverInstanceEuregio.setPdfDirectory("/mnt/bulletins/");
		serverInstanceAran = new ServerInstance();
		serverInstanceAran.setServerImagesUrl("https://static.lauegi.report/images/");
		serverInstanceAran.setMapsPath("/mnt/albina_files_local/");
		serverInstanceAran.setPdfDirectory("/mnt/albina_files_local/");
	}

	@Test
	public void createBulletinEmailHtml() throws IOException, URISyntaxException {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(avalancheReport, LanguageCode.de);
		assertEquals("155 kB", 154.7177734375, html.getBytes(StandardCharsets.UTF_8).length / 1024., 1.);
		assertTrue(html.contains("<h2 style=\"margin-bottom: 5px\">Donnerstag 17.01.2019</h2>"));
		assertTrue(html.contains("Veröffentlicht am <b>16.01.2019 um 17:00</b>"));
		assertTrue(html.contains("href=\"https://lawinen.report/bulletin/2019-01-17\""));
		assertTrue(html.contains("Tendenz: Lawinengefahr nimmt ab</p><p style=\"text-align: left; margin-bottom: 0;\">am Freitag, den 18.01.2019"));
		assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/fd_AT-07_map.jpg"));
		assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/AT-07_6385c958-018d-4c89-aa67-5eddc31ada5a.jpg"));
	}

	@Test
	public void createBulletinEmailHtmlAran() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstanceAran);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(avalancheReport, LanguageCode.en);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-12-10/2021-12-10.mail.html"), StandardCharsets.UTF_8);
		Assert.assertEquals(expected.trim(), html.trim());
		}

	@Test
	public void createBulletinEmailHtml2021() throws IOException, URISyntaxException {
		final URL resource = Resources.getResource("2021-12-01.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, serverInstanceEuregio);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(avalancheReport, LanguageCode.de);
		assertEquals("59 kB", 59, html.getBytes(StandardCharsets.UTF_8).length / 1024);
	}

	@Test
	public void langTest() {
		assertEquals("Alle Höhenlagen", LanguageCode.de.getBundleString("elevation.all"));
		assertEquals("Tutte le elevazioni", LanguageCode.it.getBundleString("elevation.all"));
		assertEquals("All elevations", LanguageCode.en.getBundleString("elevation.all"));
	}

	@Ignore
	@Test
	public void sendMediaEmails() throws IOException, URISyntaxException {
		EmailUtil.getInstance().sendMediaEmails("Test", "test.mp3", "test.txt", LocalDate.now(ZoneId.of("Europe/Vienna")).atStartOfDay(ZoneId.of("Europe/Vienna")).toInstant(), regionTyrol, "Norbert Lanzanasto", true, LanguageCode.de, serverInstanceEuregio, false);
	}
}

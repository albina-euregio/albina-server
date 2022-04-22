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
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(EmailUtilTest.class);

	private ServerInstance serverInstanceEuregio;
	private ServerInstance serverInstanceAran;

	private Region regionTirol;
	private Region regionSouthTyrol;
	private Region regionTrentino;
	private Region regionAran;

	@Before
	public void setUp() throws IOException {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setServerImagesUrl("/mnt/images/");
		serverInstanceEuregio.setMapsPath("/mnt/albina_files_local/");
		serverInstanceEuregio.setPdfDirectory("/mnt/albina_files_local/");
		serverInstanceAran = new ServerInstance();
		serverInstanceAran.setServerImagesUrl("https://static.lauegi.report/images/");
		serverInstanceAran.setMapsPath("/mnt/albina_files_local/");
		serverInstanceAran.setPdfDirectory("/mnt/albina_files_local/");

		regionTirol = new Region();
		regionTirol.setId("AT-07");
		regionTirol.setEmailColor("1AABFF");
		regionSouthTyrol = new Region();
		regionSouthTyrol.setId("IT-32-BZ");
		regionSouthTyrol.setEmailColor("1AABFF");
		regionTrentino = new Region();
		regionTrentino.setId("IT-32-TN");
		regionTrentino.setEmailColor("1AABFF");
		regionAran = new Region();
		regionAran.setId("ES-CT-L");
		regionAran.setEmailColor("A32136");
		regionAran.setImageColorbarColorPath("logo/color/colorbar.Aran.gif");
	}

	@Test
	public void createBulletinEmailHtml() throws IOException, URISyntaxException {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.de,
				regionTirol, false, false, serverInstanceEuregio);
		assertEquals("156 kB", 155.8759765625, html.getBytes(StandardCharsets.UTF_8).length / 1024., 1.);
		assertTrue(html.contains("<h2 style=\"margin-bottom: 5px\">Donnerstag 17.01.2019</h2>"));
		assertTrue(html.contains("Veröffentlicht am <b>16.01.2019 um 17:00</b>"));
		assertTrue(html.contains("href=\"https://lawinen.report/bulletin/2019-01-17\""));
		assertTrue(html.contains("Tendenz: Lawinengefahr nimmt ab</p><p style=\"text-align: left; margin-bottom: 0;\">am Freitag, den 18.01.2019"));
		assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/fd_AT-07_map.jpg"));
		assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/6385c958-018d-4c89-aa67-5eddc31ada5a.jpg"));
	}

	@Test
	public void createBulletinEmailHtmlAran() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.en,
				regionAran, false, false, serverInstanceAran);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-12-10/2021-12-10.mail.html"), StandardCharsets.UTF_8);
		Assert.assertEquals(expected.trim(), html.trim());
		}

	@Test
	public void createBulletinEmailHtml2021() throws IOException, URISyntaxException {
		final URL resource = Resources.getResource("2021-12-01.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.de,
			regionTirol, false, false, serverInstanceAran);
		assertEquals("61 kB", 61, html.getBytes(StandardCharsets.UTF_8).length / 1024);
	}

	@Test
	public void sendEmail() throws MessagingException, IOException, URISyntaxException {
		final URL resource = Resources.getResource("2021-12-02.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		logger.info("#bulletins: {}", bulletins.size());
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regionTirol, false, true, serverInstanceAran);
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regionSouthTyrol, false, true, serverInstanceAran);
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regionTrentino, false, true, serverInstanceAran);
	}

	@Ignore
	@Test
	public void sendEmailAran() throws Exception {
		final URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		logger.info("#bulletins: {}", bulletins.size());
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regionAran, false, false, serverInstanceAran);
	}

	@Test
	public void langTest() {
		assertEquals("Alle Höhenlagen", LanguageCode.de.getBundleString("elevation.all"));
		assertEquals("Tutte le elevazioni", LanguageCode.it.getBundleString("elevation.all"));
		assertEquals("All elevations", LanguageCode.en.getBundleString("elevation.all"));
	}

	@Test
	public void sendLangEmail() throws MessagingException, IOException, URISyntaxException {
		final URL resource = Resources.getResource("2021-12-02.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final boolean update = false;
		final LanguageCode lang = LanguageCode.en;
		ArrayList<Region> regions = new ArrayList<Region>();
		regions.add(regionTirol);
		regions.add(regionSouthTyrol);
		regions.add(regionTrentino);

		boolean daytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		for (Region region : regions) {
			String subject;
			if (update)
				subject = MessageFormat.format(lang.getBundleString("email.subject.update", region), lang.getBundleString("website.name", region)) + AlbinaUtil.getDate(bulletins, lang);
			else
				subject = MessageFormat.format(lang.getBundleString("email.subject", region), lang.getBundleString("website.name", region)) + AlbinaUtil.getDate(bulletins, lang);
			subject = System.getProperty("java.version") + " " + subject;
			ArrayList<AvalancheBulletin> regionBulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				if (avalancheBulletin.affectsRegionOnlyPublished(region))
					regionBulletins.add(avalancheBulletin);
			}
			String emailHtml = EmailUtil.getInstance().createBulletinEmailHtml(regionBulletins, lang, region, update, daytimeDependency, serverInstanceAran);
			EmailUtil.getInstance().sendBulletinEmailRapidmail(lang, region, emailHtml, subject, true);
		}
	}

	@Test
	public void sendMediaEmails() throws IOException, URISyntaxException {
		EmailUtil.getInstance().sendMediaEmails("", LocalDate.now(ZoneId.of("Europe/Vienna")).atStartOfDay(ZoneId.of("Europe/Vienna")).toInstant(), regionTirol, "Norbert Lanzanasto", true, LanguageCode.de);
	}
}

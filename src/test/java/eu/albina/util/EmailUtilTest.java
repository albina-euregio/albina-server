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
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(EmailUtilTest.class);

	@Test
	public void createBulletinEmailHtml() throws IOException, URISyntaxException {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.de,
				GlobalVariables.codeTyrol, false, false);
		assertEquals("162 kB", 162, html.getBytes(StandardCharsets.UTF_8).length / 1024);
		assertTrue(html.contains("<h2 style=\"margin-bottom: 5px\">Donnerstag  17.01.2019</h2>"));
		assertTrue(html.contains("Veröffentlicht am <b>16.01.2019 um 17:00</b>"));
		assertTrue(html.contains("href=\"https://lawinen.report/bulletin/2019-01-17\""));
		assertTrue(html.contains("Tendenz: Lawinengefahr nimmt ab</p><p style=\"text-align: left; margin-bottom: 0;\">am Freitag, den 18.01.2019"));
		assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/fd_tyrol_map.jpg"));
		assertTrue(html.contains("2019-01-17/2019-01-16_16-00-00/6385c958-018d-4c89-aa67-5eddc31ada5a.jpg"));
	}

	@Test
	public void createBulletinEmailHtml2021() throws IOException, URISyntaxException {
		final URL resource = Resources.getResource("2021-12-01.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.de,
			GlobalVariables.codeTyrol, false, false);
		assertEquals("61 kB", 61, html.getBytes(StandardCharsets.UTF_8).length / 1024);
	}

	@Ignore
	@Test
	public void sendEmail() throws MessagingException, IOException, URISyntaxException {
		final URL resource = Resources.getResource("2021-12-02.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		logger.info("#bulletins: {}", bulletins.size());
		ArrayList<String> regions = new ArrayList<String>();
		regions.add(GlobalVariables.codeTrentino);
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, false, true);
	}

	@Test
	public void langTest() {
		assertEquals("Alle Höhenlagen", LanguageCode.de.getBundleString("elevation.all"));
		assertEquals("Tutte le elevazioni", LanguageCode.it.getBundleString("elevation.all"));
		assertEquals("All elevations", LanguageCode.en.getBundleString("elevation.all"));
	}

	@Ignore
	@Test
	public void sendLangEmail() throws MessagingException, IOException, URISyntaxException {
		HibernateUtil.getInstance().setUp();
		final URL resource = Resources.getResource("2021-12-02.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final boolean update = false;
		final LanguageCode lang = LanguageCode.en;
		ArrayList<String> regions = new ArrayList<String>();
		regions.add(GlobalVariables.codeTyrol);
		regions.add(GlobalVariables.codeSouthTyrol);
		regions.add(GlobalVariables.codeTrentino);

		boolean daytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		String subject;
		if (update)
			subject = lang.getBundleString("email.subject.update") + AlbinaUtil.getDate(bulletins, lang);
		else
			subject = lang.getBundleString("email.subject") + AlbinaUtil.getDate(bulletins, lang);
		subject = System.getProperty("java.version") + " " + subject;
		for (String region : regions) {
			ArrayList<AvalancheBulletin> regionBulletins = new ArrayList<AvalancheBulletin>();
			for (AvalancheBulletin avalancheBulletin : bulletins) {
				if (avalancheBulletin.affectsRegionOnlyPublished(region))
					regionBulletins.add(avalancheBulletin);
			}
			String emailHtml = EmailUtil.getInstance().createBulletinEmailHtml(regionBulletins, lang, region, update, daytimeDependency);
			EmailUtil.getInstance().sendBulletinEmailRapidmail(lang, region, emailHtml, subject, true);
		}
		HibernateUtil.getInstance().shutDown();
	}
}

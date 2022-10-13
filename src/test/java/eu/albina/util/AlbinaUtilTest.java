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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerException;

import eu.albina.controller.AvalancheBulletinController;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.albina.controller.SubscriberController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.Subscriber;
import eu.albina.model.enumerations.LanguageCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlbinaUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(AlbinaUtilTest.class);

	private ServerInstance serverInstanceEuregio;

	private List<AvalancheBulletin> bulletins;
	private List<AvalancheBulletin> bulletinsAmPm;

	private Region regionTirol;
	private Region regionEuregio;

	private final String imgBaseUrl = "D:/norbert/vs_workspace/albina-server/src/test/resources/images/";
	@SuppressWarnings("unused")
	private final List<String> names = new ArrayList<String>();
	@SuppressWarnings("unused")
	private final List<String> passwords = new ArrayList<String>();
	private final List<String> recipients = new ArrayList<String>();

	@Before
	public void setUp() throws IOException {
		serverInstanceEuregio = new ServerInstance();

		regionTirol = new Region();
		regionTirol.setId("AT-07");
		regionEuregio = new Region();
		regionEuregio.setId("EUREGIO");

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletinsAmPm = new ArrayList<AvalancheBulletin>();
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_1.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_2.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_3.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_4.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_5.json")));
		bulletinsAmPm.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_6.json")));
		bulletinsAmPm.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_7.json")));

		recipients.add("n.lanzanasto@gmail.com");
		recipients.add("norbert.lanzanasto@tirol.gv.at");
		// recipients.add("mitterer.chris@gmail.com");
		// recipients.add("chris.mitterer@tirol.gv.at");
	}

	@Ignore
	@Test
	public void addSubscriber() throws KeyManagementException, CertificateException, NoSuchAlgorithmException,
			KeyStoreException, AlbinaException, IOException, Exception {
		Subscriber subscriber = new Subscriber();
		subscriber.setEmail("n.lanzanasto@gmail.com");
		subscriber.setLanguage(LanguageCode.it);
		subscriber.setRegions(Arrays.asList(regionTirol));

		SubscriberController.getInstance().createSubscriberRapidmail(subscriber);
	}

	@Test
	public void retrieveTranslationTest() throws UnsupportedEncodingException {
		String string = LanguageCode.ca.getBundleString("headline.tendency");
		Assert.assertEquals("Tendència", string);
	}

	@Test
	public void encodeImageAndPassword() {
		List<String> names = new ArrayList<String>();
		List<String> passwords = new ArrayList<String>();

		for (int i = 0; i < names.size(); i++) {
			File f = new File(imgBaseUrl + names.get(i) + ".jpg");
			String encodstring = AlbinaUtil.encodeFileToBase64Binary(f);
			String pwd = BCrypt.hashpw(passwords.get(i), BCrypt.gensalt());
			logger.warn(names.get(i));
			logger.warn("Image: " + encodstring);
			logger.warn("Password: " + pwd);
		}
	}

	@Test
	public void encodePassword() {
		String pwd = BCrypt.hashpw("Norbert", BCrypt.gensalt());
		logger.warn("Password: " + pwd);
	}

	@Ignore
	@Test
	public void testIsLatest() {
		ZonedDateTime dateTime = (ZonedDateTime.now()).minusDays(0);
		Assert.assertTrue(AlbinaUtil.isLatest(dateTime));
	}

	@Test
	public void testIsNotLatest() {
		ZonedDateTime dateTime = (ZonedDateTime.now()).minusDays(1);
		Assert.assertFalse(AlbinaUtil.isLatest(dateTime));
	}

	@Test
	public void testIsUpdate() {
		Assert.assertTrue(AlbinaUtil.isUpdate(bulletins));
	}

	@Ignore
	@Test
	public void sortBulletinsTest() {
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			System.out.println(avalancheBulletin.getHighestDangerRating());
		}
		System.out.println("Sorting ...");
		Collections.sort(bulletins);
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			System.out.println(avalancheBulletin.getHighestDangerRating());
		}
	}

	@Ignore
	@Test
	public void createJsonTest() throws TransformerException, IOException {
		JsonUtil.createJsonFile(bulletins, regionTirol, "2019-12-30", "2019-12-30_17-15-30", serverInstanceEuregio);
	}

	@Test
	public void testDates() throws Exception {
		serverInstanceEuregio.setPdfDirectory("/foo/bar/baz/bulletins");
		serverInstanceEuregio.setMapsPath("/foo/bar/baz/bulletins");
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		assertEquals("16.01.2019 um 17:00", AlbinaUtil.getPublicationDate(bulletins, LanguageCode.de));
		assertEquals("2019-01-16_16-00-00", AlbinaUtil.getPublicationTime(bulletins));
		assertEquals("2019-01-16T23:00Z", AlbinaUtil.getDate(bulletins).toString());
		assertEquals("Donnerstag 17.01.2019", AlbinaUtil.getDate(bulletins, LanguageCode.de));
		assertEquals("am Freitag, den 18.01.2019", AlbinaUtil.getTendencyDate(bulletins, LanguageCode.de));
		assertEquals("16.01.2019", AlbinaUtil.getPreviousValidityDateString(bulletins, LanguageCode.de));
		assertEquals("18.01.2019", AlbinaUtil.getNextValidityDateString(bulletins, LanguageCode.de));
		assertEquals("2019-01-17", bulletins.get(0).getValidityDateString());
		assertEquals("2019-01-17", AlbinaUtil.getValidityDateString(bulletins));
		assertEquals("2019-01-24", AlbinaUtil.getValidityDateString(bulletins, Period.ofDays(7)));
		assertEquals("Lawinen.report für Donnerstag 17.01.2019: https://lawinen.report/bulletin/2019-01-17",
			SocialMediaUtil.getSocialMediaText(bulletins, regionEuregio, false, LanguageCode.de));
		assertEquals("UPDATE zum Lawinen.report für Donnerstag 17.01.2019: https://lawinen.report/bulletin/2019-01-17",
			SocialMediaUtil.getSocialMediaText(bulletins, regionEuregio, true, LanguageCode.de));
		assertEquals("https://lawinen.report/bulletin/2019-01-17",
			LinkUtil.getBulletinUrl(bulletins, LanguageCode.de, regionEuregio));
		assertEquals("https://static.avalanche.report/bulletins/2019-01-17/2019-01-17_AT-07_de.pdf",
			LinkUtil.getPdfLink(bulletins, LanguageCode.de, regionTirol, serverInstanceEuregio));
		assertTrue(AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins),
			Clock.fixed(Instant.parse("2019-01-16T19:40:00Z"), AlbinaUtil.localZone())));
		assertTrue(AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins),
			Clock.fixed(Instant.parse("2019-01-17T10:40:00Z"), AlbinaUtil.localZone())));
		assertFalse(AlbinaUtil.isLatest(AlbinaUtil.getDate(bulletins),
			Clock.fixed(Instant.parse("2019-01-17T16:00:00Z"), AlbinaUtil.localZone())));

		// should yield strings in correct timezone, even if publication date is in a different timezone
		assertEquals("2019-01-16T16:00Z", bulletins.get(0).getPublicationDate().toString());
		bulletins.forEach(b -> b.setPublicationDate(b.getPublicationDate().withZoneSameInstant(ZoneId.of("Canada/Mountain"))));
		assertEquals("2019-01-16T09:00-07:00[Canada/Mountain]", bulletins.get(0).getPublicationDate().toString());
		assertEquals("16.01.2019 um 17:00", AlbinaUtil.getPublicationDate(bulletins, LanguageCode.de));
		assertEquals("2019-01-16_16-00-00", AlbinaUtil.getPublicationTime(bulletins));
	}

	@Test
	@Ignore
	public void testDatesHibernate() throws Exception {
		HibernateUtil.getInstance().setUp();
		final AvalancheBulletin bulletin = AvalancheBulletinController.getInstance().getBulletin("4e5bbd7c-7ccf-4a2a-8ac7-5a0bfc322a14");
		HibernateUtil.getInstance().shutDown();
		final List<AvalancheBulletin> bulletins = Collections.singletonList(bulletin);

		// Hibernate/MySQL returns timestamps in Europe/Vienna zone?!
		assertEquals("2021-12-05T17:00+01:00[Europe/Vienna]", bulletin.getPublicationDate().toString());
		assertEquals("05.12.2021 um 17:00", AlbinaUtil.getPublicationDate(bulletins, LanguageCode.de));
		assertEquals("2021-12-05_16-00-00", AlbinaUtil.getPublicationTime(bulletins));
	}

	@Test
	public void testInstantStartOfDay() {
		// code from AlbinaUtil.getInstantStartOfDay
		Instant startDate = LocalDate.parse("2022-03-27").atStartOfDay(ZoneId.of("Europe/Vienna")).toInstant();
		assertEquals(Instant.parse("2022-03-26T23:00:00Z"), startDate); // ok
		assertEquals(ZonedDateTime.parse("2022-03-27T00:00+01:00[Europe/Vienna]"), startDate.atZone(ZoneId.of("Europe/Vienna"))); // ok

		// old code from PublicationJob
		startDate = startDate.plus(1, ChronoUnit.DAYS);
		assertEquals(Instant.parse("2022-03-27T23:00:00Z"), startDate); // not good!
		assertEquals(ZonedDateTime.parse("2022-03-28T01:00+02:00[Europe/Vienna]"), startDate.atZone(ZoneId.of("Europe/Vienna"))); // not good!
		assertEquals(ZonedDateTime.parse("2022-03-27T23:00Z[UTC]"), startDate.atZone(ZoneId.of("UTC")));

		// code from PublicationJob
		ZonedDateTime today = LocalDate.parse("2022-03-27").atStartOfDay(AlbinaUtil.localZone());
		assertEquals(Instant.parse("2022-03-26T23:00:00Z"), today.toInstant()); // ok
		startDate = today.plusDays(1).toInstant();
		assertEquals(Instant.parse("2022-03-27T22:00:00Z"), startDate); // ok
		Instant endDate = today.plusDays(2).toInstant();
		assertEquals(Instant.parse("2022-03-28T22:00:00Z"), endDate); // ok
	}
}

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
package eu.albina.model;

import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.LinkUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;

public class AvalancheReportTest {

	private final ServerInstance serverInstanceEuregio = new ServerInstance();

	private List<AvalancheBulletin> bulletins;
	private List<AvalancheBulletin> bulletinsAmPm;

	private final Region regionEuregio = new Region("EUREGIO");

	@BeforeEach
	public void setUp() throws IOException {
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
	}

	@Test
	public void testIsUpdate() throws Exception {
		Assertions.assertTrue(AvalancheReport.of(bulletins, null, null).isUpdate());
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(Resources.getResource("2019-01-17.json"));
		Assertions.assertFalse(AvalancheReport.of(bulletins, null, null).isUpdate());
	}

	@Disabled
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

	@Test
	public void testDates() throws Exception {
		serverInstanceEuregio.setPdfDirectory("/foo/bar/baz/bulletins");
		serverInstanceEuregio.setMapsPath("/foo/bar/baz/bulletins");
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		Assertions.assertEquals("16.01.2019, 17:00:00", avalancheReport.getPublicationDate(LanguageCode.de));
		Assertions.assertEquals("2019-01-16_16-00-00", avalancheReport.getPublicationTimeString());
		Assertions.assertEquals("2019-01-17", avalancheReport.getValidityDate().toString());
		Assertions.assertEquals("Donnerstag, 17. Jänner 2019", avalancheReport.getDate(LanguageCode.de));
		Assertions.assertEquals("giovedì 17 gennaio 2019", avalancheReport.getDate(LanguageCode.it));
		Assertions.assertEquals("Thursday 17 January 2019", avalancheReport.getDate(LanguageCode.en));
		Assertions.assertEquals("jeudi 17 janvier 2019", avalancheReport.getDate(LanguageCode.fr));
		Assertions.assertEquals("jueves, 17 de enero de 2019", avalancheReport.getDate(LanguageCode.es));
		Assertions.assertEquals("dijous, 17 de gener de 2019", avalancheReport.getDate(LanguageCode.ca));
		Assertions.assertEquals("dijaus, 17 de Gèr de 2019", avalancheReport.getDate(LanguageCode.oc));
		Assertions.assertEquals("am Freitag, 18. Jänner 2019", avalancheReport.getTendencyDate(LanguageCode.de));
		Assertions.assertEquals("16.01.2019", avalancheReport.getPreviousValidityDateString(LanguageCode.de));
		Assertions.assertEquals("18.01.2019", avalancheReport.getNextValidityDateString(LanguageCode.de));
		Assertions.assertEquals("2019-01-17", bulletins.get(0).getValidityDateString());
		Assertions.assertEquals("2019-01-17", avalancheReport.getValidityDateString());
		Assertions.assertEquals("2019-01-24", avalancheReport.getValidityDateString(Period.ofDays(7)));
		Assertions.assertEquals("Lawinen.report für Donnerstag, 17. Jänner 2019: https://lawinen.report/bulletin/2019-01-17", MultichannelMessage.of(avalancheReport, LanguageCode.de).getSocialMediaText());
		avalancheReport.setStatus(BulletinStatus.republished);
		Assertions.assertEquals("UPDATE zum Lawinen.report für Donnerstag, 17. Jänner 2019: https://lawinen.report/bulletin/2019-01-17", MultichannelMessage.of(avalancheReport, LanguageCode.de).getSocialMediaText());
		Assertions.assertEquals("https://lawinen.report/bulletin/2019-01-17", LinkUtil.getBulletinUrl(avalancheReport, LanguageCode.de));
		Assertions.assertEquals("https://static.avalanche.report/bulletins/2019-01-17/2019-01-17_EUREGIO_de.pdf", LinkUtil.getPdfLink(avalancheReport, LanguageCode.de));
		Assertions.assertTrue(avalancheReport.isLatest(
			Clock.fixed(Instant.parse("2019-01-16T19:40:00Z"), AlbinaUtil.localZone())));
		Assertions.assertTrue(avalancheReport.isLatest(
			Clock.fixed(Instant.parse("2019-01-17T10:40:00Z"), AlbinaUtil.localZone())));
		Assertions.assertFalse(avalancheReport.isLatest(
			Clock.fixed(Instant.parse("2019-01-17T16:00:00Z"), AlbinaUtil.localZone())));

		// should yield strings in correct timezone, even if publication date is in a different timezone
		Assertions.assertEquals("2019-01-16T16:00Z", bulletins.get(0).getPublicationDate().toString());
		bulletins.forEach(b -> b.setPublicationDate(b.getPublicationDate().withZoneSameInstant(ZoneId.of("Canada/Mountain"))));
		Assertions.assertEquals("2019-01-16T09:00-07:00[Canada/Mountain]", bulletins.get(0).getPublicationDate().toString());
		Assertions.assertEquals("16.01.2019, 17:00:00", avalancheReport.getPublicationDate(LanguageCode.de));
		Assertions.assertEquals("2019-01-16_16-00-00", avalancheReport.getPublicationTimeString());
	}

	@Test
	@Disabled
	public void testDatesHibernate() throws Exception {
		HibernateUtil.getInstance().setUp();
		final AvalancheBulletin bulletin = AvalancheBulletinController.getInstance().getBulletin("4e5bbd7c-7ccf-4a2a-8ac7-5a0bfc322a14");
		HibernateUtil.getInstance().shutDown();
		final List<AvalancheBulletin> bulletins = List.of(bulletin);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);

		// Hibernate/MySQL returns timestamps in Europe/Vienna zone?!
		Assertions.assertEquals("2021-12-05T17:00+01:00[Europe/Vienna]", bulletin.getPublicationDate().toString());
		Assertions.assertEquals("05.12.2021 um 17:00", avalancheReport.getPublicationDate(LanguageCode.de));
		Assertions.assertEquals("2021-12-05_16-00-00", avalancheReport.getPublicationTimeString());
	}

	@Test
	public void getPublicationDate() {
		Assertions.assertNull(AvalancheReport.of(Collections.emptyList(), null, null).getPublicationDate());
		Assertions.assertNull(AvalancheReport.of(List.of(new AvalancheBulletin()), null, null).getPublicationDate());
		AvalancheBulletin bulletin = new AvalancheBulletin();
		bulletin.setPublicationDate(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
		Assertions.assertEquals(bulletin.getPublicationDate(), AvalancheReport.of(List.of(new AvalancheBulletin(), bulletin, new AvalancheBulletin()), null, null).getPublicationDate());
	}
}


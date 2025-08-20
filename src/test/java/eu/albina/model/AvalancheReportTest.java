// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import static eu.albina.RegionTestUtils.regionEuregio;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.controller.AvalancheBulletinController;
import eu.albina.controller.publication.MultichannelMessage;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;

public class AvalancheReportTest {

	private final ServerInstance serverInstanceEuregio = new ServerInstance();

	@Test
	public void testIsUpdate() throws Exception {
		List<AvalancheBulletin> bulletins0 = AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2030-02-16_1.json"));
		Assertions.assertTrue(AvalancheReport.of(bulletins0, null, null).isUpdate());
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2019-01-17.json"));
		Assertions.assertFalse(AvalancheReport.of(bulletins, null, null).isUpdate());
	}

	@Disabled
	@Test
	public void sortBulletinsTest() throws Exception {
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2030-02-16_1.json"));
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
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(resource);
		regionEuregio.setServerInstance(serverInstanceEuregio);
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
		Assertions.assertEquals("https://lawinen.report/bulletin/2019-01-17", avalancheReport.getRegion().getWebsiteUrlWithDate(LanguageCode.de, avalancheReport));
		Assertions.assertEquals("https://static.avalanche.report/bulletins/2019-01-17/2019-01-17_EUREGIO_de.pdf", avalancheReport.getRegion().getPdfUrl(LanguageCode.de, avalancheReport));
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


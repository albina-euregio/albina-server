package eu.albina.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.io.Resources;

import eu.albina.caaml.CaamlValidator;
import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtilTest {

	private Region regionEuregio;
	private ServerInstance serverInstanceEuregio;

	@Before
	public void setUp() throws Exception {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setHtmlDirectory("/foo/bar/baz/simple/");
		serverInstanceEuregio.setMapsPath("/foo/bar/baz/albina_files/");
		regionEuregio = new Region();
		regionEuregio.setId("EUREGIO");
	}

	private String createCaaml(CaamlVersion version) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc = XmlUtil.createCaaml(bulletins, regionEuregio, LanguageCode.en, version, serverInstanceEuregio);
		return XmlUtil.convertDocToString(doc);
	}

	@Ignore("<Operation> needs gml:id")
	@Test
	public void createValidCaamlv5() throws Exception {
		final String xml = createCaaml(CaamlVersion.V5);
		CaamlValidator.validateCaamlBulletin(xml, CaamlVersion.V5);
	}

	@Test
	public void createValidCaamlv6() throws Exception {
		final String xml = createCaaml(CaamlVersion.V6);
		CaamlValidator.validateCaamlBulletin(xml, CaamlVersion.V6);
	}

	@Test
	public void createExpectedCaamlv5() throws Exception {
		final String expected = Resources
				.toString(Resources.getResource("2019-01-16.caaml.v5.xml"), StandardCharsets.UTF_8).replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V5);
		Assert.assertEquals(expected, xml);
	}

	@Test
	public void createExpectedCaamlV6() throws Exception {
		final String expected = Resources
				.toString(Resources.getResource("2019-01-16.caaml.v6.xml"), StandardCharsets.UTF_8).replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V6);
		Assert.assertEquals(expected, xml);
	}

	@Ignore
	@Test
	public void createOldCaamlFiles() throws Exception {
		HibernateUtil.getInstance().setUp();
		for (LocalDate date = LocalDate.parse("2018-12-04"); date
				.isBefore(LocalDate.parse("2019-05-07")); date = date.plusDays(1)) {
			createOldCaamlFiles(date);
		}
		for (LocalDate date = LocalDate.parse("2019-11-16"); date
				.isBefore(LocalDate.parse("2020-05-04")); date = date.plusDays(1)) {
			createOldCaamlFiles(date);
		}
	}

	private void createOldCaamlFiles(LocalDate date) throws Exception {
		List<AvalancheBulletin> result = AvalancheReportController.getInstance().getPublishedBulletins(
				ZonedDateTime.of(date.atTime(0, 0, 0), ZoneId.of("UTC")).toInstant(), RegionController.getInstance().getPublishBulletinRegions());
		for (LanguageCode language : Arrays.asList(LanguageCode.de, LanguageCode.en, LanguageCode.it)) {
			Path path = Paths.get("/tmp/albina_files" + "/" + date + "/" + date + "_" + language + "_CAAMLv6.xml");
			Document caamlDoc = XmlUtil.createCaaml(result, regionEuregio, language, CaamlVersion.V6, serverInstanceEuregio);
			String caaml = XmlUtil.convertDocToString(caamlDoc);
			LoggerFactory.getLogger(getClass()).info("Writing {}", path);
			Files.write(path, caaml.getBytes(StandardCharsets.UTF_8));
		}
	}
}

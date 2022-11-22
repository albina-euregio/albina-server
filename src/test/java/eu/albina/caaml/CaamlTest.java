package eu.albina.caaml;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.albina.json.JsonValidator;
import eu.albina.model.AvalancheReport;
import eu.albina.util.HibernateUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

import static org.junit.Assert.assertEquals;

public class CaamlTest {

	private Region regionEuregio;
	private Region regionTyrol;
	private Region regionSouthTyrol;
	private Region regionTrentino;
	private ServerInstance serverInstanceEuregio;

	@Before
	public void setUp() throws Exception {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setHtmlDirectory("/foo/bar/baz/simple/");
		serverInstanceEuregio.setMapsPath("/foo/bar/baz/bulletins/");
		regionEuregio = new Region();
		regionEuregio.setId("EUREGIO");
		regionTyrol = new Region();
		regionTyrol.setId("AT-07");
		regionSouthTyrol = new Region();
		regionSouthTyrol.setId("IT-32-BZ");
		regionTrentino = new Region();
		regionTrentino.setId("IT-32-TN");
		regionEuregio.addSubRegion(regionTyrol);
		regionEuregio.addSubRegion(regionSouthTyrol);
		regionEuregio.addSubRegion(regionTrentino);
	}

	private String createCaaml(CaamlVersion version) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		return Caaml.createCaaml(avalancheReport, LanguageCode.en, version);
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
		try {
			CaamlValidator.validateCaamlBulletin(xml, CaamlVersion.V6);
		} catch (SAXParseException e) {
			// TODO CAAMLv6 schema file currently not in place
		}
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
			createOldCaamlFiles(date, CaamlVersion.V6);
		}
		for (LocalDate date = LocalDate.parse("2019-11-16"); date
				.isBefore(LocalDate.parse("2020-05-04")); date = date.plusDays(1)) {
			createOldCaamlFiles(date, CaamlVersion.V6);
		}
	}

	@Ignore
	@Test
	public void createOldCaamlFiles2022() throws Exception {
		for (LocalDate date = LocalDate.parse("2021-12-01");
			 date.isBefore(LocalDate.parse("2022-05-02"));
			 date = date.plusDays(1)) {
			createOldCaamlFiles(date, CaamlVersion.V6_2022);
		}
	}

	private void createOldCaamlFiles(LocalDate date, CaamlVersion version) throws Exception {
		URL url = new URL(String.format("https://static.avalanche.report/bulletins/%s/avalanche_report.json", date));
		LoggerFactory.getLogger(getClass()).info("Fetching bulletins from {}", url);
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(url);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		for (LanguageCode language : LanguageCode.ENABLED) {
			Path path = Paths.get(String.format("/tmp/bulletins/%s/%s_%s%s", date, date, language, version.filenameSuffix()));
			String caaml = Caaml.createCaaml(avalancheReport, language, version);
			LoggerFactory.getLogger(getClass()).info("Writing {}", path);
			Files.createDirectories(path.getParent());
			Files.write(path, caaml.getBytes(StandardCharsets.UTF_8));
		}
	}

	private static String buildCAAML(String resourceName) throws IOException {
		final URL resource = Resources.getResource(resourceName);
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);
		return Caaml6_2022.toCAAMLv6String_2022(avalancheReport, LanguageCode.en);
	}

	@Test
	public void toCAAMLv6_2022a() throws Exception {
		final String caaml = buildCAAML("2019-01-16.json");
		final String expected = Resources.toString(Resources.getResource("2019-01-16.caaml.v6.json"), StandardCharsets.UTF_8);
		assertEquals(expected.trim(), caaml.trim());
		assertEquals(Collections.emptySet(), JsonValidator.validateCAAMLv6(caaml));
	}

	@Test
	public void toCAAMLv6_2022b() throws Exception {
		final String caaml = buildCAAML("2019-01-17.json");
		final String expected = Resources.toString(Resources.getResource("2019-01-17.caaml.v6.json"), StandardCharsets.UTF_8);
		assertEquals(expected.trim(), caaml.trim());
		assertEquals(Collections.emptySet(), JsonValidator.validateCAAMLv6(caaml));
	}

	@Test
	public void toCAAMLv6_2022c() throws Exception {
		final String caaml = buildCAAML("2022-11-10.dev.json");
		final String expected = Resources.toString(Resources.getResource("2022-11-10.dev.caaml.v6.json"), StandardCharsets.UTF_8);
		assertEquals(expected.trim(), caaml.trim());
		assertEquals(Collections.emptySet(), JsonValidator.validateCAAMLv6(caaml));
	}
}

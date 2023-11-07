package eu.albina.caaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.json.JsonValidator;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;

import static eu.albina.RegionTestUtils.regionEuregio;

public class CaamlTest {

	private ServerInstance serverInstanceEuregio;

	@BeforeEach
	public void setUp() throws Exception {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setHtmlDirectory("/foo/bar/baz/simple/");
		serverInstanceEuregio.setMapsPath("/foo/bar/baz/bulletins/");
	}

	private String createCaaml(CaamlVersion version) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		AvalancheReport.of(bulletins, null, serverInstanceEuregio); // test without region for eu.albina.rest.AvalancheBulletinService.getJSONBulletins
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		return Caaml.createCaaml(avalancheReport, LanguageCode.en, version);
	}

	@Disabled("<Operation> needs gml:id")
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
		assertStringEquals(expected, xml);
	}

	@Test
	public void createExpectedCaamlV6() throws Exception {
		final String expected = Resources
				.toString(Resources.getResource("2019-01-16.caaml.v6.xml"), StandardCharsets.UTF_8).replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V6);
		assertStringEquals(expected, xml);
	}

	@Disabled
	@Test
	public void createOldCaamlFiles() throws Exception {
		HibernateUtil.getInstance().setUp();
		for (LocalDate date = LocalDate.parse("2018-12-04"); date
				.isBefore(LocalDate.parse("2019-05-07")); date = date.plusDays(1)) {
			createOldCaamlFiles(date, CaamlVersion.V6_JSON);
		}
		for (LocalDate date = LocalDate.parse("2019-11-16"); date
				.isBefore(LocalDate.parse("2020-05-04")); date = date.plusDays(1)) {
			createOldCaamlFiles(date, CaamlVersion.V6_JSON);
		}
	}

	@Disabled
	@Test
	public void createOldCaamlFiles2022() throws Exception {
		for (LocalDate date = LocalDate.parse("2020-11-01");
			 date.isBefore(LocalDate.parse("2022-05-02"));
			 date = date.plusDays(1)) {
			try {
				createOldCaamlFiles(date, CaamlVersion.V6_JSON);
			} catch (FileNotFoundException e) {
				LoggerFactory.getLogger(getClass()).warn("Not found {}", e.getMessage());
			}
		}
	}

	private void createOldCaamlFiles(LocalDate date, CaamlVersion version) throws Exception {
		LoggerFactory.getLogger(getClass()).info("Loading {}", date);
		AvalancheReport avalancheReport = date.isAfter(LocalDate.parse("2020-10-01")) ? loadFromURL(date): loadFromDatabase(date);
		for (LanguageCode language : LanguageCode.ENABLED) {
			Path path = Paths.get(String.format("/tmp/bulletins/%s/%s_%s%s", date, date, language, version.filenameSuffix()));
			String caaml = Caaml.createCaaml(avalancheReport, language, version);
			LoggerFactory.getLogger(getClass()).info("Writing {}", path);
			Files.createDirectories(path.getParent());
			Files.write(path, caaml.getBytes(StandardCharsets.UTF_8));
		}
	}

	private AvalancheReport loadFromURL(LocalDate date) throws Exception {
		URL url = new URL(String.format("https://static.avalanche.report/bulletins/%s/avalanche_report.json", date));
		LoggerFactory.getLogger(getClass()).info("Fetching bulletins from {}", url);
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(url);
		return AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
	}

	private AvalancheReport loadFromDatabase(LocalDate date) throws Exception {
		Instant instant = date.atStartOfDay(AlbinaUtil.localZone()).withZoneSameInstant(ZoneOffset.UTC).toInstant();
		List<AvalancheBulletin> bulletins = AvalancheReportController.getInstance().getPublishedBulletins(instant, RegionController.getInstance().getPublishBulletinRegions());
		AvalancheReport report = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		Path path = Paths.get(String.format("/tmp/bulletins/%s/avalanche_report.json", date));
		Files.createDirectories(path.getParent());
		Files.write(path, JsonUtil.createJSONString(bulletins, new Region("" /* empty to avoid filtering */), true).toString().getBytes(StandardCharsets.UTF_8));
		return report;
	}

	private static void toCAAMLv6_JSON(String bulletinResource, String expectedCaamlResource) throws IOException {
		final URL resource = Resources.getResource(bulletinResource);
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);
		final String caaml = Caaml6_JSON.createCaamlv6(avalancheReport, LanguageCode.en);
		// Files.write(Paths.get("src/test/resources/" + expectedCaamlResource), caaml.getBytes(StandardCharsets.UTF_8));
		final String expected = Resources.toString(Resources.getResource(expectedCaamlResource), StandardCharsets.UTF_8);
		assertStringEquals(expected, caaml);
		Assertions.assertEquals(Collections.emptySet(), JsonValidator.validateCAAMLv6(caaml));
	}

	private static void assertStringEquals(String expected, String actual) {
		Assertions.assertEquals(expected.trim().replace("\r\n", "\n"), actual.trim().replace("\r\n", "\n"));
	}

	@Test
	public void toCAAMLv6_JSON_a() throws Exception {
		toCAAMLv6_JSON("2019-01-16.json", "2019-01-16.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_JSON_b() throws Exception {
		toCAAMLv6_JSON("2019-01-17.json", "2019-01-17.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_JSON_c() throws Exception {
		toCAAMLv6_JSON("2022-11-10.dev.json", "2022-11-10.dev.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_JSON_d() throws Exception {
		toCAAMLv6_JSON("2018-12-27.json", "2018-12-27.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_JSON_e() throws Exception {
		toCAAMLv6_JSON("2022-12-20.json", "2022-12-20.caaml.v6.json");
	}
}

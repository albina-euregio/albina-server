// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.caaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.controller.RegionRepository;
import eu.albina.model.Region;
import eu.albina.util.JsonUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.io.Resources;

import eu.albina.controller.AvalancheReportController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.AlbinaUtil;

@MicronautTest
public class CaamlTest {

	private ServerInstance serverInstanceEuregio;
	private Region regionEuregio;

	@Inject
	AvalancheBulletinTestUtils avalancheBulletinTestUtils;

	@Inject
	RegionTestUtils regionTestUtils;

	@Inject
	Caaml caaml;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	private RegionRepository regionRepository;

	@BeforeEach
	public void setUp() throws Exception {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setHtmlDirectory("/foo/bar/baz/simple/");
		serverInstanceEuregio.setMapsPath("/foo/bar/baz/bulletins/");
		regionEuregio = regionTestUtils.regionEuregio();
	}

	private String createCaaml(CaamlVersion version) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport.of(bulletins, null, serverInstanceEuregio); // test without region for eu.albina.rest.AvalancheBulletinService.getJSONBulletins
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		return caaml.createCaaml(avalancheReport, LanguageCode.en, version);
	}

	@Disabled("<Operation> needs gml:id")
	@Test
	public void createValidCaamlv5() throws Exception {
		final String xml = createCaaml(CaamlVersion.V5);
		CaamlValidator.validateCaamlBulletin(xml, CaamlVersion.V5);
	}

	@Test
	public void createExpectedCaamlv5() throws Exception {
		final String expected = Resources
				.toString(Resources.getResource("2019-01-16.caaml.v5.xml"), StandardCharsets.UTF_8).replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V5);
		Assertions.assertEquals(expected, xml);
	}

	@Disabled
	@Test
	public void createOldCaamlFiles() throws Exception {
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
		for (LanguageCode language : avalancheReport.getRegion().getEnabledLanguages()) {
			Path path = Paths.get(String.format("/tmp/bulletins/%s/%s_%s%s", date, date, language, version.filenameSuffix()));
			String caaml = this.caaml.createCaaml(avalancheReport, language, version);
			LoggerFactory.getLogger(getClass()).info("Writing {}", path);
			Files.createDirectories(path.getParent());
			Files.writeString(path, caaml, StandardCharsets.UTF_8);
		}
	}

	private AvalancheReport loadFromURL(LocalDate date) throws Exception {
		URL url = URI.create(String.format("https://static.avalanche.report/bulletins/%s/avalanche_report.json", date)).toURL();
		LoggerFactory.getLogger(getClass()).info("Fetching bulletins from {}", url);
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(url);
		return AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
	}

	private AvalancheReport loadFromDatabase(LocalDate date) throws Exception {
		Instant instant = date.atStartOfDay(AlbinaUtil.localZone()).withZoneSameInstant(ZoneOffset.UTC).toInstant();
		List<AvalancheBulletin> bulletins = new AvalancheReportController().getPublishedBulletins(instant, regionRepository.getPublishBulletinRegions());
		AvalancheReport report = AvalancheReport.of(bulletins, regionEuregio, serverInstanceEuregio);
		Path path = Paths.get(String.format("/tmp/bulletins/%s/avalanche_report.json", date));
		Files.createDirectories(path.getParent());
		String json = objectMapper.cloneWithViewClass(JsonUtil.Views.Public.class).writeValueAsString(bulletins);
		Files.writeString(path, json, StandardCharsets.UTF_8);
		return report;
	}

	private void toCAAMLv6(String bulletinResource, String expectedCaamlResource) throws Exception {
		final URL resource = Resources.getResource(bulletinResource);
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);

		toCAAMLv6(avalancheReport, expectedCaamlResource, CaamlVersion.V6_JSON);
		toCAAMLv6(avalancheReport, expectedCaamlResource.replaceFirst(".json$", ".xml"), CaamlVersion.V6);
	}

	private void toCAAMLv6(AvalancheReport avalancheReport, String expectedCaamlResource, CaamlVersion version) throws IOException, SAXException {
		String caaml = this.caaml.createCaaml(avalancheReport, LanguageCode.en, version);
		// Files.write(Paths.get("src/test/resources/" + expectedCaamlResource), caaml.getBytes(StandardCharsets.UTF_8));
        String expected = Resources.toString(Resources.getResource(expectedCaamlResource), StandardCharsets.UTF_8);
		if (version == CaamlVersion.V6_JSON) {
			JsonAssert.assertJsonEquals(expected, caaml);
		} else {
			Assertions.assertEquals(expected, caaml);
		}
		CaamlValidator.validateCaamlBulletin(caaml, version);
	}

	@Test
	public void toCAAMLv6_a() throws Exception {
		toCAAMLv6("2019-01-16.json", "2019-01-16.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_b() throws Exception {
		toCAAMLv6("2019-01-17.json", "2019-01-17.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_c() {
		Assertions.assertThrowsExactly(SAXParseException.class, () -> toCAAMLv6("2022-11-10.dev.json", "2022-11-10.dev.caaml.v6.json"));
	}

	@Test
	public void toCAAMLv6_d() throws Exception {
		toCAAMLv6("2018-12-27.json", "2018-12-27.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_e() throws Exception {
		toCAAMLv6("2022-12-20.json", "2022-12-20.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_f() throws Exception {
		toCAAMLv6("2023-12-01.json", "2023-12-01.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_g() throws Exception {
		toCAAMLv6("2023-12-21.json", "2023-12-21.caaml.v6.json");
	}

	@Test
	public void toCAAMLv6_ES_AR() throws Exception {
		toCAAMLv6("aludes.aragon.es/2025-09-17.json", "aludes.aragon.es/2025-09-17.caaml.v6.json");
	}
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.StandardSystemProperty;
import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.model.LocalServerInstance;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.ImageTestUtils;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.PdfUtil;

@MicronautTest
public class MapUtilTest {

	@Inject
	AvalancheBulletinTestUtils avalancheBulletinTestUtils;

	@Inject
	RegionTestUtils regionTestUtils;

	private LocalServerInstance serverInstance;
	private Path folder;
	private Region regionAran;
	private Region regionAragon;
	private Region regionEuregio;
	private Region regionSouthTyrol;
	private Region regionTrentino;
	private Region regionTyrol;

	@BeforeEach
	public void setUp() throws Exception {
		folder = Paths.get("target/test-results");
		Files.createDirectories(folder);
		serverInstance = new LocalServerInstance(false, false, folder.toString(), "../avalanche-warning-maps/", folder.toString(), null, null, null);
		regionAran = regionTestUtils.regionAran();
		regionAragon = regionTestUtils.regionAragon();
		regionEuregio = regionTestUtils.regionEuregio();
		regionSouthTyrol = regionTestUtils.regionSouthTyrol();
		regionTrentino = regionTestUtils.regionTrentino();
		regionTyrol = regionTestUtils.regionTyrol();
	}

	private Path getRelativePath(Path path) {
		return Paths.get(folder.toString()).relativize(path);
	}

	@Test
	public void testOverviewMapFilename() {
		assertEquals("fd_EUREGIO_map.jpg",
				MapUtil.getOverviewMapFilename(regionEuregio, DaytimeDependency.fd, false));
		assertEquals("fd_AT-07_map.jpg",
				MapUtil.getOverviewMapFilename(regionTyrol, DaytimeDependency.fd, false));
		assertEquals("fd_AT-07_map_bw.jpg",
				MapUtil.getOverviewMapFilename(regionTyrol, DaytimeDependency.fd, true));
		assertEquals("am_AT-07_map.jpg", MapUtil.getOverviewMapFilename(regionTyrol, DaytimeDependency.am, false));
		assertEquals("pm_AT-07_map.jpg", MapUtil.getOverviewMapFilename(regionTyrol, DaytimeDependency.pm, false));
		assertEquals("fd_IT-32-BZ_map_bw.jpg",
				MapUtil.getOverviewMapFilename(regionSouthTyrol, DaytimeDependency.fd, true));
		assertEquals("fd_IT-32-TN_map_bw.jpg",
				MapUtil.getOverviewMapFilename(regionTrentino, DaytimeDependency.fd, true));
	}

	@Test
	public void testFilename() throws Exception {
		List<String> filenames = new ArrayList<>();
		for (Region region : Arrays.asList(regionAran, regionEuregio, regionSouthTyrol, regionTyrol, regionTrentino)) {
			for (MapLevel mapLevel : MapLevel.values()) {
				for (DaytimeDependency daytimeDependency : DaytimeDependency.values()) {
					filenames.add(MapUtil.filename(region, mapLevel, daytimeDependency, false, MapImageFormat.png));
				}
			}
		}
		final URL resource = Resources.getResource("2019-01-17.json");
		final AvalancheBulletin bulletin = avalancheBulletinTestUtils.readBulletins(resource).getFirst();
		for (Region region : Arrays.asList(regionAran, regionEuregio, regionSouthTyrol, regionTyrol, regionTrentino)) {
			if (!bulletin.affectsRegion(region)) {
				continue;
			}
			for (DaytimeDependency daytimeDependency : DaytimeDependency.values()) {
				filenames.add(MapUtil.filename(region, bulletin, daytimeDependency, false, MapImageFormat.png));
			}
		}
		assertEquals(Arrays.asList(
			"fd_ES-CT-L_thumbnail.png",
			"am_ES-CT-L_thumbnail.png",
			"pm_ES-CT-L_thumbnail.png",
			"fd_ES-CT-L_map.png",
			"am_ES-CT-L_map.png",
			"pm_ES-CT-L_map.png",
			"fd_ES-CT-L_overlay.png",
			"am_ES-CT-L_overlay.png",
			"pm_ES-CT-L_overlay.png",
			"fd_EUREGIO_thumbnail.png",
			"am_EUREGIO_thumbnail.png",
			"pm_EUREGIO_thumbnail.png",
			"fd_EUREGIO_map.png",
			"am_EUREGIO_map.png",
			"pm_EUREGIO_map.png",
			"fd_EUREGIO_overlay.png",
			"am_EUREGIO_overlay.png",
			"pm_EUREGIO_overlay.png",
			"fd_IT-32-BZ_thumbnail.png",
			"am_IT-32-BZ_thumbnail.png",
			"pm_IT-32-BZ_thumbnail.png",
			"fd_IT-32-BZ_map.png",
			"am_IT-32-BZ_map.png",
			"pm_IT-32-BZ_map.png",
			"fd_IT-32-BZ_overlay.png",
			"am_IT-32-BZ_overlay.png",
			"pm_IT-32-BZ_overlay.png",
			"fd_AT-07_thumbnail.png",
			"am_AT-07_thumbnail.png",
			"pm_AT-07_thumbnail.png",
			"fd_AT-07_map.png",
			"am_AT-07_map.png",
			"pm_AT-07_map.png",
			"fd_AT-07_overlay.png",
			"am_AT-07_overlay.png",
			"pm_AT-07_overlay.png",
			"fd_IT-32-TN_thumbnail.png",
			"am_IT-32-TN_thumbnail.png",
			"pm_IT-32-TN_thumbnail.png",
			"fd_IT-32-TN_map.png",
			"am_IT-32-TN_map.png",
			"pm_IT-32-TN_map.png",
			"fd_IT-32-TN_overlay.png",
			"am_IT-32-TN_overlay.png",
			"pm_IT-32-TN_overlay.png",
			"EUREGIO_6385c958-018d-4c89-aa67-5eddc31ada5a.png",
			"EUREGIO_6385c958-018d-4c89-aa67-5eddc31ada5a.png",
			"EUREGIO_6385c958-018d-4c89-aa67-5eddc31ada5a_PM.png",
			"AT-07_6385c958-018d-4c89-aa67-5eddc31ada5a.png",
			"AT-07_6385c958-018d-4c89-aa67-5eddc31ada5a.png",
			"AT-07_6385c958-018d-4c89-aa67-5eddc31ada5a_PM.png"
		), filenames);
	}

	@Test
	public void testMapyrusMaps() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);

		for (String name : Arrays.asList("fd_EUREGIO_thumbnail.png", "EUREGIO_f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png")) {
			byte[] expected = Resources.toByteArray(Resources.getResource(name));
			byte[] actual = Files.readAllBytes(avalancheReport.getMapsPath().resolve(name));
			ImageTestUtils.assertImageEquals(name, expected, actual, 0, 0, ignore -> {
			});
		}

		assertEquals("2019-01-17/2019-01-16_16-00-00/2019-01-17_EUREGIO_en.pdf",
			getRelativePath(new PdfUtil(avalancheReport, LanguageCode.en, false).getPath()).toString());
		new PdfUtil(avalancheReport, LanguageCode.en, false).createPdf();
	}

	@Test
	public void testMapyrusMapsTyrol() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final List<AvalancheBulletin> bulletinsTyrol = bulletins.stream()
			.filter(avalancheBulletin -> avalancheBulletin.affectsRegionOnlyPublished(regionTyrol))
			.collect(Collectors.toList());
		AvalancheReport avalancheReport = AvalancheReport.of(bulletinsTyrol, regionTyrol, serverInstance);
		avalancheReport.setBulletins(bulletinsTyrol, bulletins);
		MapUtil.createMapyrusMaps(avalancheReport);
		assertEquals("2019-01-17/2019-01-16_16-00-00/2019-01-17_AT-07_en.pdf",
			getRelativePath(new PdfUtil(avalancheReport, LanguageCode.en, false).getPath()).toString());
		new PdfUtil(avalancheReport, LanguageCode.en, false).createPdf();
	}

	@Test
	@Disabled
	public void testMapyrusMapsAragon() throws Exception {
		URL resource = Resources.getResource("aludes.aragon.es/2025-09-17.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAragon, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);

		byte[] expected = Resources.toByteArray(Resources.getResource("lauegi.report-2021-01-24/fd_ES-CT-L_thumbnail.png"));
		byte[] actual = Files.readAllBytes(avalancheReport.getMapsPath().resolve("fd_ES-CT-L_thumbnail.png"));
		ImageTestUtils.assertImageEquals("fd_ES-CT-L_thumbnail.png", expected, actual, 0, 0, ignore -> {
		});
		assertEquals("2021-01-24/2021-01-23_16-00-00/2021-01-24_ES-CT-L_en.pdf",
			getRelativePath(new PdfUtil(avalancheReport, LanguageCode.en, false).getPath()).toString());
		new PdfUtil(avalancheReport, LanguageCode.ca, false).createPdf();
	}

	@Test
	public void testMapyrusMapsAran() throws Exception {
		URL resource = Resources.getResource("lauegi.report-2021-01-24/2021-01-24.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);

		byte[] expected = Resources.toByteArray(Resources.getResource("lauegi.report-2021-01-24/fd_ES-CT-L_thumbnail.png"));
		byte[] actual = Files.readAllBytes(avalancheReport.getMapsPath().resolve("fd_ES-CT-L_thumbnail.png"));
		ImageTestUtils.assertImageEquals("fd_ES-CT-L_thumbnail.png", expected, actual, 0, 0, ignore -> {
		});
		assertEquals("2021-01-24/2021-01-23_16-00-00/2021-01-24_ES-CT-L_en.pdf",
			getRelativePath(new PdfUtil(avalancheReport, LanguageCode.en, false).getPath()).toString());
		new PdfUtil(avalancheReport, LanguageCode.ca, false).createPdf();
	}

	@Test
	@Disabled
	public void testMapyrusMapsAranVeryHigh() throws Exception {
		URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);
		new PdfUtil(avalancheReport, LanguageCode.ca, false).createPdf();
	}

	@Test
	@Disabled
	public void testMapyrusMapsAranMatrixInformation() throws Exception {
		URL resource = Resources.getResource("lauegi.report-2022-12-06/ES-CT-L.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionAran, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);
		new PdfUtil(avalancheReport, LanguageCode.en, false).createPdf();
	}

	@Test
	@Disabled("fix path")
	public void testPreviewMaps() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		MapUtil.createMapyrusMaps(AvalancheReport.of(bulletins, regionEuregio, serverInstance));

		byte[] expected = Resources.toByteArray(Resources.getResource("f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png"));
        byte[] actual = Files.readAllBytes(Path.of(StandardSystemProperty.JAVA_IO_TMPDIR.value() + "/2019-01-17/PREVIEW/f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png"));
		ImageTestUtils.assertImageEquals("f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png", expected, actual, 0, 0, ignore -> {
		});
	}

	@Test
	@Disabled("slow, only run testMapyrusMaps")
	public void testMapyrusMapsWithDaytimeDependency() throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);
		new PdfUtil(avalancheReport, LanguageCode.en, false).createPdf();
	}

	@Test
	@Disabled("slow, only run testMapyrusMaps")
	public void testMapyrusMapsDaylightSavingTime1() throws Exception {
		final URL resource = Resources.getResource("2020-03-29.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		MapUtil.createMapyrusMaps(AvalancheReport.of(bulletins, regionEuregio, serverInstance));
	}

	@Test
	@Disabled("slow, only run testMapyrusMaps")
	public void testMapyrusMapsDaylightSavingTime2() throws Exception {
		final URL resource = Resources.getResource("2020-03-30.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		MapUtil.createMapyrusMaps(AvalancheReport.of(bulletins, regionEuregio, serverInstance));
	}

	@Test
	public void testMayrusBindings() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		final Map<String, Object> bindings = MapUtil.createMayrusBindings(bulletins, DaytimeDependency.fd, false);
		String expected = "{\"AT-07-01-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-01-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-02-01-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-02-01-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-02-02-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-02-02-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-03-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-03-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-04-01-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-04-01-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-04-02-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-04-02-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-05-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-05-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-06-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-06-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-07-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-07-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-08-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-08-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-09-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-09-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-10-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-10-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-11-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-11-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-12-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-12-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-13-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-13-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-14-01-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-01-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-02-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-02-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-03-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-03-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-04-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-04-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-05-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-05-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-15-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-15-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-16-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-16-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-17-01-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-17-01-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-17-02-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-17-02-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-18-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-18-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-19-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-19-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-20-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-20-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-21-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-21-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-22-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-22-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-23-01-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-23-01-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-23-02-high\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-23-02-low\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-24-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-24-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-25-high\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-25-low\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-26-01-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-26-01-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-26-02-high\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-26-02-low\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-27-high\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-27-low\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-28-high\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-28-low\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-29-01-high\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"AT-07-29-01-low\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"AT-07-29-02-high\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"AT-07-29-02-low\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"AT-07-29-03-high\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"AT-07-29-03-low\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"IT-32-BZ-01-01-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-01-01-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-01-02-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-01-02-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-02-01-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-02-01-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-02-02-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-02-02-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-03-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-03-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-04-01-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-04-01-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-04-02-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-04-02-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-01-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-01-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-02-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-02-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-03-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-03-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-06-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-06-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-07-01-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-07-01-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-07-02-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-07-02-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-08-01-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-08-01-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-08-02-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-08-02-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-08-03-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-08-03-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-09-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-09-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-10-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-10-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-11-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-11-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-12-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-12-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-13-high\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-13-low\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-14-high\": \"2683b4d4-a4d3-48e0-ac4b-1e8a04676925\", \"IT-32-BZ-14-low\": \"2683b4d4-a4d3-48e0-ac4b-1e8a04676925\", \"IT-32-BZ-15-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-15-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-16-high\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-16-low\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-17-high\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-17-low\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-18-01-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-18-01-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-18-02-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-18-02-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-19-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-19-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-20-high\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-20-low\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-TN-01-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-01-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-02-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-02-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-03-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-03-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-04-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-04-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-05-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-05-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-06-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-06-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-07-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-07-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-08-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-08-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-09-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-09-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-10-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-10-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-11-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-11-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-12-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-12-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-13-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-13-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-14-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-14-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-15-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-15-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-16-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-16-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-17-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-17-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-18-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-18-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-19-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-19-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-20-high\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-20-low\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-21-high\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-21-low\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\"}";
		assertEquals(expected, bindings.get("bulletin_ids").toString());
		expected = "{\"AT-07-01-high\": 3, \"AT-07-02-01-high\": 3, \"AT-07-02-02-high\": 3, \"AT-07-03-high\": 3, \"AT-07-04-01-high\": 3, \"AT-07-04-02-high\": 3, \"AT-07-05-high\": 3, \"AT-07-06-high\": 3, \"AT-07-07-high\": 3, \"AT-07-08-high\": 3, \"AT-07-09-high\": 3, \"AT-07-10-high\": 3, \"AT-07-11-high\": 3, \"AT-07-12-high\": 3, \"AT-07-13-high\": 3, \"AT-07-14-01-high\": 3, \"AT-07-14-02-high\": 3, \"AT-07-14-03-high\": 3, \"AT-07-14-04-high\": 3, \"AT-07-14-05-high\": 3, \"AT-07-15-high\": 3, \"AT-07-16-high\": 3, \"AT-07-17-01-high\": 3, \"AT-07-17-02-high\": 3, \"AT-07-18-high\": 3, \"AT-07-19-high\": 3, \"AT-07-20-high\": 3, \"AT-07-21-high\": 3, \"AT-07-22-high\": 3, \"AT-07-23-01-high\": 3, \"AT-07-23-02-high\": 3, \"AT-07-24-high\": 3, \"AT-07-25-high\": 3, \"AT-07-26-01-high\": 3, \"AT-07-26-02-high\": 3, \"AT-07-27-high\": 3, \"AT-07-28-high\": 3, \"AT-07-29-01-high\": 2, \"AT-07-29-02-high\": 2, \"AT-07-29-03-high\": 2, \"IT-32-BZ-01-01-high\": 3, \"IT-32-BZ-01-02-high\": 3, \"IT-32-BZ-02-01-high\": 3, \"IT-32-BZ-02-02-high\": 3, \"IT-32-BZ-03-high\": 3, \"IT-32-BZ-04-01-high\": 3, \"IT-32-BZ-04-02-high\": 3, \"IT-32-BZ-05-01-high\": 3, \"IT-32-BZ-05-02-high\": 3, \"IT-32-BZ-05-03-high\": 3, \"IT-32-BZ-06-high\": 3, \"IT-32-BZ-07-01-high\": 3, \"IT-32-BZ-07-02-high\": 3, \"IT-32-BZ-08-01-high\": 2, \"IT-32-BZ-08-02-high\": 2, \"IT-32-BZ-08-03-high\": 2, \"IT-32-BZ-09-high\": 3, \"IT-32-BZ-10-high\": 3, \"IT-32-BZ-11-high\": 3, \"IT-32-BZ-12-high\": 3, \"IT-32-BZ-13-high\": 3, \"IT-32-BZ-14-high\": 3, \"IT-32-BZ-15-high\": 2, \"IT-32-BZ-16-high\": 1, \"IT-32-BZ-17-high\": 1, \"IT-32-BZ-18-01-high\": 2, \"IT-32-BZ-18-02-high\": 2, \"IT-32-BZ-19-high\": 2, \"IT-32-BZ-20-high\": 2, \"IT-32-TN-01-high\": 2, \"IT-32-TN-02-high\": 2, \"IT-32-TN-03-high\": 1, \"IT-32-TN-04-high\": 2, \"IT-32-TN-05-high\": 2, \"IT-32-TN-06-high\": 1, \"IT-32-TN-07-high\": 2, \"IT-32-TN-08-high\": 1, \"IT-32-TN-09-high\": 2, \"IT-32-TN-10-high\": 1, \"IT-32-TN-11-high\": 1, \"IT-32-TN-12-high\": 1, \"IT-32-TN-13-high\": 2, \"IT-32-TN-14-high\": 1, \"IT-32-TN-15-high\": 1, \"IT-32-TN-16-high\": 2, \"IT-32-TN-17-high\": 1, \"IT-32-TN-18-high\": 1, \"IT-32-TN-19-high\": 2, \"IT-32-TN-20-high\": 2, \"IT-32-TN-21-high\": 1}";
		assertEquals(expected, bindings.get("danger_h").toString());
		expected = "{\"AT-07-01-low\": 3, \"AT-07-02-01-low\": 3, \"AT-07-02-02-low\": 3, \"AT-07-03-low\": 3, \"AT-07-04-01-low\": 3, \"AT-07-04-02-low\": 3, \"AT-07-05-low\": 3, \"AT-07-06-low\": 3, \"AT-07-07-low\": 3, \"AT-07-08-low\": 3, \"AT-07-09-low\": 2, \"AT-07-10-low\": 3, \"AT-07-11-low\": 3, \"AT-07-12-low\": 3, \"AT-07-13-low\": 3, \"AT-07-14-01-low\": 2, \"AT-07-14-02-low\": 2, \"AT-07-14-03-low\": 2, \"AT-07-14-04-low\": 2, \"AT-07-14-05-low\": 2, \"AT-07-15-low\": 2, \"AT-07-16-low\": 2, \"AT-07-17-01-low\": 3, \"AT-07-17-02-low\": 3, \"AT-07-18-low\": 3, \"AT-07-19-low\": 2, \"AT-07-20-low\": 2, \"AT-07-21-low\": 2, \"AT-07-22-low\": 2, \"AT-07-23-01-low\": 2, \"AT-07-23-02-low\": 2, \"AT-07-24-low\": 3, \"AT-07-25-low\": 2, \"AT-07-26-01-low\": 3, \"AT-07-26-02-low\": 3, \"AT-07-27-low\": 2, \"AT-07-28-low\": 2, \"AT-07-29-01-low\": 1, \"AT-07-29-02-low\": 1, \"AT-07-29-03-low\": 1, \"IT-32-BZ-01-01-low\": 2, \"IT-32-BZ-01-02-low\": 2, \"IT-32-BZ-02-01-low\": 2, \"IT-32-BZ-02-02-low\": 2, \"IT-32-BZ-03-low\": 2, \"IT-32-BZ-04-01-low\": 2, \"IT-32-BZ-04-02-low\": 2, \"IT-32-BZ-05-01-low\": 2, \"IT-32-BZ-05-02-low\": 2, \"IT-32-BZ-05-03-low\": 2, \"IT-32-BZ-06-low\": 2, \"IT-32-BZ-07-01-low\": 2, \"IT-32-BZ-07-02-low\": 2, \"IT-32-BZ-08-01-low\": 1, \"IT-32-BZ-08-02-low\": 1, \"IT-32-BZ-08-03-low\": 1, \"IT-32-BZ-09-low\": 2, \"IT-32-BZ-10-low\": 2, \"IT-32-BZ-11-low\": 2, \"IT-32-BZ-12-low\": 2, \"IT-32-BZ-13-low\": 2, \"IT-32-BZ-14-low\": 1, \"IT-32-BZ-15-low\": 1, \"IT-32-BZ-16-low\": 1, \"IT-32-BZ-17-low\": 1, \"IT-32-BZ-18-01-low\": 1, \"IT-32-BZ-18-02-low\": 1, \"IT-32-BZ-19-low\": 1, \"IT-32-BZ-20-low\": 1, \"IT-32-TN-01-low\": 1, \"IT-32-TN-02-low\": 1, \"IT-32-TN-03-low\": 1, \"IT-32-TN-04-low\": 1, \"IT-32-TN-05-low\": 1, \"IT-32-TN-06-low\": 1, \"IT-32-TN-07-low\": 1, \"IT-32-TN-08-low\": 1, \"IT-32-TN-09-low\": 1, \"IT-32-TN-10-low\": 1, \"IT-32-TN-11-low\": 1, \"IT-32-TN-12-low\": 1, \"IT-32-TN-13-low\": 1, \"IT-32-TN-14-low\": 1, \"IT-32-TN-15-low\": 1, \"IT-32-TN-16-low\": 1, \"IT-32-TN-17-low\": 1, \"IT-32-TN-18-low\": 1, \"IT-32-TN-19-low\": 1, \"IT-32-TN-20-low\": 1, \"IT-32-TN-21-low\": 1}";
		assertEquals(expected, bindings.get("danger_l").toString());
		expected = "{\"AT-07-01-high\": 0, \"AT-07-02-01-high\": 0, \"AT-07-02-02-high\": 0, \"AT-07-03-high\": 0, \"AT-07-04-01-high\": 0, \"AT-07-04-02-high\": 0, \"AT-07-05-high\": 0, \"AT-07-06-high\": 0, \"AT-07-07-high\": 0, \"AT-07-08-high\": 0, \"AT-07-09-high\": 2200, \"AT-07-10-high\": 0, \"AT-07-11-high\": 0, \"AT-07-12-high\": 0, \"AT-07-13-high\": 0, \"AT-07-14-01-high\": 2200, \"AT-07-14-02-high\": 2200, \"AT-07-14-03-high\": 2200, \"AT-07-14-04-high\": 2200, \"AT-07-14-05-high\": 2200, \"AT-07-15-high\": 2200, \"AT-07-16-high\": 2200, \"AT-07-17-01-high\": 0, \"AT-07-17-02-high\": 0, \"AT-07-18-high\": 0, \"AT-07-19-high\": 2200, \"AT-07-20-high\": 2200, \"AT-07-21-high\": 2200, \"AT-07-22-high\": 2200, \"AT-07-23-01-high\": 2200, \"AT-07-23-02-high\": 2200, \"AT-07-24-high\": 0, \"AT-07-25-high\": 1600, \"AT-07-26-01-high\": 0, \"AT-07-26-02-high\": 0, \"AT-07-27-high\": 1600, \"AT-07-28-high\": 1600, \"AT-07-29-01-high\": 2000, \"AT-07-29-02-high\": 2000, \"AT-07-29-03-high\": 2000, \"IT-32-BZ-01-01-high\": 0, \"IT-32-BZ-01-02-high\": 0, \"IT-32-BZ-02-01-high\": 0, \"IT-32-BZ-02-02-high\": 0, \"IT-32-BZ-03-high\": 0, \"IT-32-BZ-04-01-high\": 0, \"IT-32-BZ-04-02-high\": 0, \"IT-32-BZ-05-01-high\": 0, \"IT-32-BZ-05-02-high\": 0, \"IT-32-BZ-05-03-high\": 0, \"IT-32-BZ-06-high\": 0, \"IT-32-BZ-07-01-high\": 0, \"IT-32-BZ-07-02-high\": 0, \"IT-32-BZ-08-01-high\": 2000, \"IT-32-BZ-08-02-high\": 2000, \"IT-32-BZ-08-03-high\": 2000, \"IT-32-BZ-09-high\": 0, \"IT-32-BZ-10-high\": 0, \"IT-32-BZ-11-high\": 0, \"IT-32-BZ-12-high\": 0, \"IT-32-BZ-13-high\": 0, \"IT-32-BZ-14-high\": 0, \"IT-32-BZ-15-high\": 2000, \"IT-32-BZ-16-high\": 0, \"IT-32-BZ-17-high\": 0, \"IT-32-BZ-18-01-high\": 2000, \"IT-32-BZ-18-02-high\": 2000, \"IT-32-BZ-19-high\": 2000, \"IT-32-BZ-20-high\": 2000, \"IT-32-TN-01-high\": 2500, \"IT-32-TN-02-high\": 2500, \"IT-32-TN-03-high\": 0, \"IT-32-TN-04-high\": 2500, \"IT-32-TN-05-high\": 2500, \"IT-32-TN-06-high\": 0, \"IT-32-TN-07-high\": 2500, \"IT-32-TN-08-high\": 0, \"IT-32-TN-09-high\": 2500, \"IT-32-TN-10-high\": 0, \"IT-32-TN-11-high\": 0, \"IT-32-TN-12-high\": 0, \"IT-32-TN-13-high\": 2500, \"IT-32-TN-14-high\": 0, \"IT-32-TN-15-high\": 0, \"IT-32-TN-16-high\": 2500, \"IT-32-TN-17-high\": 0, \"IT-32-TN-18-high\": 0, \"IT-32-TN-19-high\": 2500, \"IT-32-TN-20-high\": 2500, \"IT-32-TN-21-high\": 0}";
		assertEquals(expected, bindings.get("elevation_h").toString());
	}

	@Test
	public void testMapProductionRessource() {
		Path geoDataPath = Paths.get("geodata.Euregio/AT-07/");
		String filename = "test.png";
		String result = MapUtil.mapProductionResource(geoDataPath, filename).toString();
		assertEquals("geodata.Euregio" + System.getProperty("file.separator") + filename, result);
	}
}

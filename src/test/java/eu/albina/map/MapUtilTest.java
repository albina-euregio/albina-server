package eu.albina.map;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import eu.albina.ImageTestUtils;
import eu.albina.controller.ServerInstanceController;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;
import eu.albina.util.PdfUtil;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;

import javax.imageio.ImageIO;

public class MapUtilTest {

	private final Region regionEuregio = new Region("EUREGIO", "geodata.Euregio", 1464000, 1104000, 6047000, 5687000);
	private final Region regionTirol = new Region("AT-07", "geodata.Euregio", 1452000, 1116000, 6053000, 5829000);
	private final Region regionSouthTyrol = new Region("IT-32-BZ", "geodata.Euregio", 1400000, 1145000, 5939000, 5769000);
	private final Region regionTrentino = new Region("IT-32-TN", "geodata.Euregio", 1358000, 1133000, 5842000, 5692000);
	private final Region regionAran = new Region("ES-CT-L", "geodata.Aran", 120500, 66200, 5266900, 5215700);
	private final ServerInstance serverInstance = new ServerInstance();

	@Rule
	public TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

	@Before
	public void setUp() throws Exception {
		serverInstance.setMapsPath(folder.toString());
		serverInstance.setMapProductionUrl("../avalanche-warning-maps/");
		serverInstance.setPdfDirectory(folder.toString());
	}

	@Test
	public void testOverviewMapFilename() {
		assertEquals("fd_EUREGIO_map.jpg",
				MapUtil.getOverviewMapFilename(regionEuregio, DaytimeDependency.fd, false));
		assertEquals("fd_AT-07_map.jpg",
				MapUtil.getOverviewMapFilename(regionTirol, DaytimeDependency.fd, false));
		assertEquals("fd_AT-07_map_bw.jpg",
				MapUtil.getOverviewMapFilename(regionTirol, DaytimeDependency.fd, true));
		assertEquals("am_AT-07_map.jpg", MapUtil.getOverviewMapFilename(regionTirol, DaytimeDependency.am, false));
		assertEquals("pm_AT-07_map.jpg", MapUtil.getOverviewMapFilename(regionTirol, DaytimeDependency.pm, false));
		assertEquals("fd_IT-32-BZ_map_bw.jpg",
				MapUtil.getOverviewMapFilename(regionSouthTyrol, DaytimeDependency.fd, true));
		assertEquals("fd_IT-32-TN_map_bw.jpg",
				MapUtil.getOverviewMapFilename(regionTrentino, DaytimeDependency.fd, true));
	}

	private void assumeMapsPath() {
		Assume.assumeTrue(Files.isDirectory(Paths.get(serverInstance.getMapProductionUrl())));
	}

	@Test
	public void testMapyrusMaps() throws Exception {
		assumeMapsPath();
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionEuregio, serverInstance);

		for (String name : Arrays.asList("fd_EUREGIO_thumbnail.png", "f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png")) {
			BufferedImage expected = ImageIO.read(Resources.getResource(name));
			BufferedImage actual = ImageIO.read(new File(
				serverInstance.getMapsPath() + "/2019-01-17/2019-01-16_16-00-00/" + name));
			ImageTestUtils.assertImageEquals(expected, actual, 0, 0, ignore -> {
			});
		}
		PdfUtil.getInstance().createPdf(bulletins, LanguageCode.en, regionEuregio, serverInstance, false, false,
			"2019-01-17", "2019-01-16_16-00-00", false);
	}

	@Test
	public void testMapyrusMapsAran() throws Exception {
		assumeMapsPath();
		URL resource = Resources.getResource("lauegi.report-2021-01-24/2021-01-24.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionAran, serverInstance);

		BufferedImage expected = ImageIO.read(Resources.getResource("lauegi.report-2021-01-24/fd_ES-CT-L_thumbnail.png"));
		BufferedImage actual = ImageIO.read(new File(
			serverInstance.getMapsPath() + "/2021-01-24/2021-01-23_16-00-00/fd_ES-CT-L_thumbnail.png"));
		ImageTestUtils.assertImageEquals(expected, actual, 0, 0, ignore -> {
		});
		PdfUtil.getInstance().createPdf(bulletins, LanguageCode.ca, regionAran, serverInstance, false, false,
			"2021-01-24", "2021-01-23_16-00-00", false);
	}

	@Test
	@Ignore
	public void testMapyrusMapsAranVeryHigh() throws Exception {
		assumeMapsPath();
		URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionAran, serverInstance);
		PdfUtil.getInstance().createPdf(bulletins, LanguageCode.ca, regionAran, serverInstance, false, false,
			"2021-12-10", "2021-12-09_16-06-27", false);
	}

	@Test
	@Ignore("fix path")
	public void testPreviewMaps() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionEuregio, serverInstance);

		BufferedImage expected = ImageIO.read(Resources.getResource("f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png"));
		BufferedImage actual = ImageIO.read(new File(
			GlobalVariables.getTmpMapsPath() + "/2019-01-17/PREVIEW/f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png"));
		ImageTestUtils.assertImageEquals(expected, actual, 0, 0, ignore -> {
		});
	}

	@Test
	@Ignore("slow, only run testMapyrusMaps")
	public void testMapyrusMapsWithDaytimeDependency() throws Exception {
		assumeMapsPath();
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionEuregio, serverInstance);
	}

	@Test
	@Ignore("slow, only run testMapyrusMaps")
	public void testMapyrusMapsDaylightSavingTime1() throws Exception {
		assumeMapsPath();
		final URL resource = Resources.getResource("2020-03-29.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionEuregio, serverInstance);
	}

	@Test
	@Ignore("slow, only run testMapyrusMaps")
	public void testMapyrusMapsDaylightSavingTime2() throws Exception {
		assumeMapsPath();
		final URL resource = Resources.getResource("2020-03-30.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regionEuregio, serverInstance);
	}

	@Test
	public void testMayrusBindings() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Map<String, Object> bindings = MapUtil.createMayrusBindings(bulletins, DaytimeDependency.fd, false);
		String expected = "{\"AT-07-01-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-01-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-02-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-02-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-03-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-03-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-04-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-04-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-05-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-05-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-06-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-06-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-07-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-07-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-08-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-08-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-09-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-09-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-10-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-10-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-11-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-11-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-12-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-12-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-13-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-13-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-14-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-14-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-15-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-15-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-16-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-16-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-17-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-17-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-18-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-18-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-19-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-19-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-20-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-20-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-21-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-21-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-22-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-22-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-23-h\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-23-l\": \"f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd\", \"AT-07-24-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-24-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-25-h\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-25-l\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-26-h\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-26-l\": \"6385c958-018d-4c89-aa67-5eddc31ada5a\", \"AT-07-27-h\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-27-l\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-28-h\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-28-l\": \"cd6faf0f-5188-4904-91cc-524c3a446b0c\", \"AT-07-29-h\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"AT-07-29-l\": \"5bf03ed5-7cac-493b-aad4-f2bc4cb4ba34\", \"IT-32-BZ-01-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-01-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-02-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-02-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-03-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-03-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-04-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-04-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-05-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-06-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-06-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-07-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-07-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-08-h\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-08-l\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-09-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-09-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-10-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-10-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-11-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-11-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-12-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-12-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-13-h\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-13-l\": \"0d9c6af5-53cc-4c80-8901-065a3f5b3195\", \"IT-32-BZ-14-h\": \"2683b4d4-a4d3-48e0-ac4b-1e8a04676925\", \"IT-32-BZ-14-l\": \"2683b4d4-a4d3-48e0-ac4b-1e8a04676925\", \"IT-32-BZ-15-h\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-15-l\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-16-h\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-16-l\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-17-h\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-17-l\": \"67e2d91b-2335-45f5-abad-1dc48b86c777\", \"IT-32-BZ-18-h\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-18-l\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-19-h\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-19-l\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-20-h\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-BZ-20-l\": \"9d365268-1218-49b8-b05b-e4cc6de13402\", \"IT-32-TN-01-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-01-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-02-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-02-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-03-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-03-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-04-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-04-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-05-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-05-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-06-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-06-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-07-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-07-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-08-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-08-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-09-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-09-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-10-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-10-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-11-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-11-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-12-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-12-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-13-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-13-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-14-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-14-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-15-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-15-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-16-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-16-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-17-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-17-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-18-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-18-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-19-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-19-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-20-h\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-20-l\": \"853733e5-cb48-4cf1-91a2-ebde59dda31f\", \"IT-32-TN-21-h\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\", \"IT-32-TN-21-l\": \"24fce71f-ad60-47d6-b607-d7cefca5ba3f\"}";
		assertEquals(expected, bindings.get("bulletin_ids").toString());
		expected = "{\"AT-07-01-h\": 3, \"AT-07-02-h\": 3, \"AT-07-03-h\": 3, \"AT-07-04-h\": 3, \"AT-07-05-h\": 3, \"AT-07-06-h\": 3, \"AT-07-07-h\": 3, \"AT-07-08-h\": 3, \"AT-07-09-h\": 3, \"AT-07-10-h\": 3, \"AT-07-11-h\": 3, \"AT-07-12-h\": 3, \"AT-07-13-h\": 3, \"AT-07-14-h\": 3, \"AT-07-15-h\": 3, \"AT-07-16-h\": 3, \"AT-07-17-h\": 3, \"AT-07-18-h\": 3, \"AT-07-19-h\": 3, \"AT-07-20-h\": 3, \"AT-07-21-h\": 3, \"AT-07-22-h\": 3, \"AT-07-23-h\": 3, \"AT-07-24-h\": 3, \"AT-07-25-h\": 3, \"AT-07-26-h\": 3, \"AT-07-27-h\": 3, \"AT-07-28-h\": 3, \"AT-07-29-h\": 2, \"IT-32-BZ-01-h\": 3, \"IT-32-BZ-02-h\": 3, \"IT-32-BZ-03-h\": 3, \"IT-32-BZ-04-h\": 3, \"IT-32-BZ-05-h\": 3, \"IT-32-BZ-06-h\": 3, \"IT-32-BZ-07-h\": 3, \"IT-32-BZ-08-h\": 2, \"IT-32-BZ-09-h\": 3, \"IT-32-BZ-10-h\": 3, \"IT-32-BZ-11-h\": 3, \"IT-32-BZ-12-h\": 3, \"IT-32-BZ-13-h\": 3, \"IT-32-BZ-14-h\": 3, \"IT-32-BZ-15-h\": 2, \"IT-32-BZ-16-h\": 1, \"IT-32-BZ-17-h\": 1, \"IT-32-BZ-18-h\": 2, \"IT-32-BZ-19-h\": 2, \"IT-32-BZ-20-h\": 2, \"IT-32-TN-01-h\": 2, \"IT-32-TN-02-h\": 2, \"IT-32-TN-03-h\": 1, \"IT-32-TN-04-h\": 2, \"IT-32-TN-05-h\": 2, \"IT-32-TN-06-h\": 1, \"IT-32-TN-07-h\": 2, \"IT-32-TN-08-h\": 1, \"IT-32-TN-09-h\": 2, \"IT-32-TN-10-h\": 1, \"IT-32-TN-11-h\": 1, \"IT-32-TN-12-h\": 1, \"IT-32-TN-13-h\": 2, \"IT-32-TN-14-h\": 1, \"IT-32-TN-15-h\": 1, \"IT-32-TN-16-h\": 2, \"IT-32-TN-17-h\": 1, \"IT-32-TN-18-h\": 1, \"IT-32-TN-19-h\": 2, \"IT-32-TN-20-h\": 2, \"IT-32-TN-21-h\": 1}";
		assertEquals(expected, bindings.get("danger_h").toString());
		expected = "{\"AT-07-01-l\": 3, \"AT-07-02-l\": 3, \"AT-07-03-l\": 3, \"AT-07-04-l\": 3, \"AT-07-05-l\": 3, \"AT-07-06-l\": 3, \"AT-07-07-l\": 3, \"AT-07-08-l\": 3, \"AT-07-09-l\": 2, \"AT-07-10-l\": 3, \"AT-07-11-l\": 3, \"AT-07-12-l\": 3, \"AT-07-13-l\": 3, \"AT-07-14-l\": 2, \"AT-07-15-l\": 2, \"AT-07-16-l\": 2, \"AT-07-17-l\": 3, \"AT-07-18-l\": 3, \"AT-07-19-l\": 2, \"AT-07-20-l\": 2, \"AT-07-21-l\": 2, \"AT-07-22-l\": 2, \"AT-07-23-l\": 2, \"AT-07-24-l\": 3, \"AT-07-25-l\": 2, \"AT-07-26-l\": 3, \"AT-07-27-l\": 2, \"AT-07-28-l\": 2, \"AT-07-29-l\": 1, \"IT-32-BZ-01-l\": 2, \"IT-32-BZ-02-l\": 2, \"IT-32-BZ-03-l\": 2, \"IT-32-BZ-04-l\": 2, \"IT-32-BZ-05-l\": 2, \"IT-32-BZ-06-l\": 2, \"IT-32-BZ-07-l\": 2, \"IT-32-BZ-08-l\": 1, \"IT-32-BZ-09-l\": 2, \"IT-32-BZ-10-l\": 2, \"IT-32-BZ-11-l\": 2, \"IT-32-BZ-12-l\": 2, \"IT-32-BZ-13-l\": 2, \"IT-32-BZ-14-l\": 1, \"IT-32-BZ-15-l\": 1, \"IT-32-BZ-16-l\": 1, \"IT-32-BZ-17-l\": 1, \"IT-32-BZ-18-l\": 1, \"IT-32-BZ-19-l\": 1, \"IT-32-BZ-20-l\": 1, \"IT-32-TN-01-l\": 1, \"IT-32-TN-02-l\": 1, \"IT-32-TN-03-l\": 1, \"IT-32-TN-04-l\": 1, \"IT-32-TN-05-l\": 1, \"IT-32-TN-06-l\": 1, \"IT-32-TN-07-l\": 1, \"IT-32-TN-08-l\": 1, \"IT-32-TN-09-l\": 1, \"IT-32-TN-10-l\": 1, \"IT-32-TN-11-l\": 1, \"IT-32-TN-12-l\": 1, \"IT-32-TN-13-l\": 1, \"IT-32-TN-14-l\": 1, \"IT-32-TN-15-l\": 1, \"IT-32-TN-16-l\": 1, \"IT-32-TN-17-l\": 1, \"IT-32-TN-18-l\": 1, \"IT-32-TN-19-l\": 1, \"IT-32-TN-20-l\": 1, \"IT-32-TN-21-l\": 1}";
		assertEquals(expected, bindings.get("danger_l").toString());
		expected = "{\"AT-07-01-h\": 0, \"AT-07-02-h\": 0, \"AT-07-03-h\": 0, \"AT-07-04-h\": 0, \"AT-07-05-h\": 0, \"AT-07-06-h\": 0, \"AT-07-07-h\": 0, \"AT-07-08-h\": 0, \"AT-07-09-h\": 2200, \"AT-07-10-h\": 0, \"AT-07-11-h\": 0, \"AT-07-12-h\": 0, \"AT-07-13-h\": 0, \"AT-07-14-h\": 2200, \"AT-07-15-h\": 2200, \"AT-07-16-h\": 2200, \"AT-07-17-h\": 0, \"AT-07-18-h\": 0, \"AT-07-19-h\": 2200, \"AT-07-20-h\": 2200, \"AT-07-21-h\": 2200, \"AT-07-22-h\": 2200, \"AT-07-23-h\": 2200, \"AT-07-24-h\": 0, \"AT-07-25-h\": 1600, \"AT-07-26-h\": 0, \"AT-07-27-h\": 1600, \"AT-07-28-h\": 1600, \"AT-07-29-h\": 2000, \"IT-32-BZ-01-h\": 0, \"IT-32-BZ-02-h\": 0, \"IT-32-BZ-03-h\": 0, \"IT-32-BZ-04-h\": 0, \"IT-32-BZ-05-h\": 0, \"IT-32-BZ-06-h\": 0, \"IT-32-BZ-07-h\": 0, \"IT-32-BZ-08-h\": 2000, \"IT-32-BZ-09-h\": 0, \"IT-32-BZ-10-h\": 0, \"IT-32-BZ-11-h\": 0, \"IT-32-BZ-12-h\": 0, \"IT-32-BZ-13-h\": 0, \"IT-32-BZ-14-h\": 0, \"IT-32-BZ-15-h\": 2000, \"IT-32-BZ-16-h\": 0, \"IT-32-BZ-17-h\": 0, \"IT-32-BZ-18-h\": 2000, \"IT-32-BZ-19-h\": 2000, \"IT-32-BZ-20-h\": 2000, \"IT-32-TN-01-h\": 2500, \"IT-32-TN-02-h\": 2500, \"IT-32-TN-03-h\": 0, \"IT-32-TN-04-h\": 2500, \"IT-32-TN-05-h\": 2500, \"IT-32-TN-06-h\": 0, \"IT-32-TN-07-h\": 2500, \"IT-32-TN-08-h\": 0, \"IT-32-TN-09-h\": 2500, \"IT-32-TN-10-h\": 0, \"IT-32-TN-11-h\": 0, \"IT-32-TN-12-h\": 0, \"IT-32-TN-13-h\": 2500, \"IT-32-TN-14-h\": 0, \"IT-32-TN-15-h\": 0, \"IT-32-TN-16-h\": 2500, \"IT-32-TN-17-h\": 0, \"IT-32-TN-18-h\": 0, \"IT-32-TN-19-h\": 2500, \"IT-32-TN-20-h\": 2500, \"IT-32-TN-21-h\": 0}";
		assertEquals(expected, bindings.get("elevation_h").toString());

	}
}

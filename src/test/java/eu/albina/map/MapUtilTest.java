package eu.albina.map;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import eu.albina.ImageTestUtils;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.GlobalVariables;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Regions;

import javax.imageio.ImageIO;

public class MapUtilTest {

	@Rule
	public TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

	@Before
	public void setUp() throws Exception {
		GlobalVariables.loadConfigProperties();
		GlobalVariables.mapsPath = folder.toString();
		GlobalVariables.mapProductionUrl = "../avalanche-warning-maps/";
	}

	@Test
	public void testOverviewMapFilename() {
		assertEquals("fd_albina_map.jpg", MapUtil.getOverviewMapFilename(null, false, false, false));
		assertEquals("fd_tyrol_map.jpg",
				MapUtil.getOverviewMapFilename(GlobalVariables.codeTyrol, false, false, false));
		assertEquals("fd_tyrol_map_bw.jpg",
				MapUtil.getOverviewMapFilename(GlobalVariables.codeTyrol, false, false, true));
		assertEquals("am_tyrol_map.jpg", MapUtil.getOverviewMapFilename(GlobalVariables.codeTyrol, false, true, false));
		assertEquals("pm_tyrol_map.jpg", MapUtil.getOverviewMapFilename(GlobalVariables.codeTyrol, true, true, false));
		assertEquals("fd_southtyrol_map_bw.jpg",
				MapUtil.getOverviewMapFilename(GlobalVariables.codeSouthTyrol, false, false, true));
		assertEquals("fd_trentino_map_bw.jpg",
				MapUtil.getOverviewMapFilename(GlobalVariables.codeTrentino, false, false, true));
	}

	private void assumeMapsPath() {
		Assume.assumeTrue(Files.isDirectory(Paths.get(GlobalVariables.getMapProductionUrl())));
	}

	@Test
	public void testMapyrusMaps() throws Exception {
		assumeMapsPath();
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regions, AlbinaUtil.getPublicationTime(bulletins), false);

		for (String name : Arrays.asList("fd_albina_thumbnail.png", "f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png")) {
			BufferedImage expected = ImageIO.read(Resources.getResource(name));
			BufferedImage actual = ImageIO.read(new File(
				GlobalVariables.getMapsPath() + "/2019-01-17/2019-01-16_16-00-00/" + name));
			ImageTestUtils.assertImageEquals(expected, actual, 0, 0, ignore -> { });
		}
	}

	@Test
	@Ignore("fix path")
	public void testPreviewMaps() throws Exception {
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regions, AlbinaUtil.getPublicationTime(bulletins), true);

		BufferedImage expected = ImageIO.read(Resources.getResource("f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png"));
		BufferedImage actual = ImageIO.read(new File(
			GlobalVariables.getTmpMapsPath() + "/2019-01-17/PREVIEW/f6cf685e-2d1d-4d76-b1dc-b152dfa9b5dd.png"));
		ImageTestUtils.assertImageEquals(expected, actual, 0, 0, ignore -> { });
	}

	@Test
	@Ignore("slow, only run testMapyrusMaps")
	public void testMapyrusMapsWithDaytimeDependency() throws Exception {
		assumeMapsPath();
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regions, AlbinaUtil.getPublicationTime(bulletins), false);
	}

	@Test
	@Ignore("slow, only run testMapyrusMaps")
	public void testMapyrusMapsDaylightSavingTime1() throws Exception {
		assumeMapsPath();
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2020-03-29.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regions, AlbinaUtil.getPublicationTime(bulletins), false);
	}

	@Test
	@Ignore("slow, only run testMapyrusMaps")
	public void testMapyrusMapsDaylightSavingTime2() throws Exception {
		assumeMapsPath();
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2020-03-30.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		MapUtil.createMapyrusMaps(bulletins, regions, AlbinaUtil.getPublicationTime(bulletins), false);
	}

	@Test
	public void testRegionsFile() throws Exception {
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Path path = folder.newFile("regions.json").toPath();

		MapUtil.createBulletinRegions(bulletins, MapUtil.DaytimeDependency.fd, path, regions, false);
		final String actual = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

		final String expected = Resources.toString(Resources.getResource("2019-01-17.regions.json"),
				StandardCharsets.UTF_8);
		String expectedResult = new JSONObject(expected).toString(4);
		String actualResult = new JSONObject(actual).toString(4);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testMayrusBindings() throws Exception {
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Map<String, Object> bindings = MapUtil.createMayrusBindings(bulletins, MapUtil.DaytimeDependency.fd, false);
		String expected = "{\"AT-07-01-h\": 0, \"AT-07-02-h\": 0, \"AT-07-03-h\": 0, \"AT-07-04-h\": 0, \"AT-07-05-h\": 0, \"AT-07-06-h\": 0, \"AT-07-07-h\": 0, \"AT-07-08-h\": 0, \"AT-07-09-h\": 2, \"AT-07-10-h\": 0, \"AT-07-11-h\": 0, \"AT-07-12-h\": 0, \"AT-07-13-h\": 0, \"AT-07-14-h\": 2, \"AT-07-15-h\": 2, \"AT-07-16-h\": 2, \"AT-07-17-h\": 0, \"AT-07-18-h\": 0, \"AT-07-19-h\": 2, \"AT-07-20-h\": 2, \"AT-07-21-h\": 2, \"AT-07-22-h\": 2, \"AT-07-23-h\": 2, \"AT-07-24-h\": 0, \"AT-07-25-h\": 1, \"AT-07-26-h\": 0, \"AT-07-27-h\": 1, \"AT-07-28-h\": 1, \"AT-07-29-h\": 8, \"IT-32-BZ-01-h\": 7, \"IT-32-BZ-02-h\": 7, \"IT-32-BZ-03-h\": 7, \"IT-32-BZ-04-h\": 7, \"IT-32-BZ-05-h\": 7, \"IT-32-BZ-06-h\": 7, \"IT-32-BZ-07-h\": 7, \"IT-32-BZ-08-h\": 9, \"IT-32-BZ-09-h\": 7, \"IT-32-BZ-10-h\": 7, \"IT-32-BZ-11-h\": 7, \"IT-32-BZ-12-h\": 7, \"IT-32-BZ-13-h\": 7, \"IT-32-BZ-14-h\": 4, \"IT-32-BZ-15-h\": 9, \"IT-32-BZ-16-h\": 6, \"IT-32-BZ-17-h\": 6, \"IT-32-BZ-18-h\": 9, \"IT-32-BZ-19-h\": 9, \"IT-32-BZ-20-h\": 9, \"IT-32-TN-01-h\": 3, \"IT-32-TN-02-h\": 3, \"IT-32-TN-03-h\": 5, \"IT-32-TN-04-h\": 3, \"IT-32-TN-05-h\": 3, \"IT-32-TN-06-h\": 5, \"IT-32-TN-07-h\": 3, \"IT-32-TN-08-h\": 5, \"IT-32-TN-09-h\": 3, \"IT-32-TN-10-h\": 5, \"IT-32-TN-11-h\": 5, \"IT-32-TN-12-h\": 5, \"IT-32-TN-13-h\": 3, \"IT-32-TN-14-h\": 5, \"IT-32-TN-15-h\": 5, \"IT-32-TN-16-h\": 3, \"IT-32-TN-17-h\": 5, \"IT-32-TN-18-h\": 5, \"IT-32-TN-19-h\": 3, \"IT-32-TN-20-h\": 3, \"IT-32-TN-21-h\": 5}";
		assertEquals(expected, bindings.get("bul_id_h").toString());
		expected = expected.replace("-h", "-l");
		assertEquals(expected, bindings.get("bul_id_l").toString());
		expected = "{\"AT-07-01-h\": 3, \"AT-07-02-h\": 3, \"AT-07-03-h\": 3, \"AT-07-04-h\": 3, \"AT-07-05-h\": 3, \"AT-07-06-h\": 3, \"AT-07-07-h\": 3, \"AT-07-08-h\": 3, \"AT-07-09-h\": 3, \"AT-07-10-h\": 3, \"AT-07-11-h\": 3, \"AT-07-12-h\": 3, \"AT-07-13-h\": 3, \"AT-07-14-h\": 3, \"AT-07-15-h\": 3, \"AT-07-16-h\": 3, \"AT-07-17-h\": 3, \"AT-07-18-h\": 3, \"AT-07-19-h\": 3, \"AT-07-20-h\": 3, \"AT-07-21-h\": 3, \"AT-07-22-h\": 3, \"AT-07-23-h\": 3, \"AT-07-24-h\": 3, \"AT-07-25-h\": 3, \"AT-07-26-h\": 3, \"AT-07-27-h\": 3, \"AT-07-28-h\": 3, \"AT-07-29-h\": 2, \"IT-32-BZ-01-h\": 3, \"IT-32-BZ-02-h\": 3, \"IT-32-BZ-03-h\": 3, \"IT-32-BZ-04-h\": 3, \"IT-32-BZ-05-h\": 3, \"IT-32-BZ-06-h\": 3, \"IT-32-BZ-07-h\": 3, \"IT-32-BZ-08-h\": 2, \"IT-32-BZ-09-h\": 3, \"IT-32-BZ-10-h\": 3, \"IT-32-BZ-11-h\": 3, \"IT-32-BZ-12-h\": 3, \"IT-32-BZ-13-h\": 3, \"IT-32-BZ-14-h\": 3, \"IT-32-BZ-15-h\": 2, \"IT-32-BZ-16-h\": 1, \"IT-32-BZ-17-h\": 1, \"IT-32-BZ-18-h\": 2, \"IT-32-BZ-19-h\": 2, \"IT-32-BZ-20-h\": 2, \"IT-32-TN-01-h\": 2, \"IT-32-TN-02-h\": 2, \"IT-32-TN-03-h\": 1, \"IT-32-TN-04-h\": 2, \"IT-32-TN-05-h\": 2, \"IT-32-TN-06-h\": 1, \"IT-32-TN-07-h\": 2, \"IT-32-TN-08-h\": 1, \"IT-32-TN-09-h\": 2, \"IT-32-TN-10-h\": 1, \"IT-32-TN-11-h\": 1, \"IT-32-TN-12-h\": 1, \"IT-32-TN-13-h\": 2, \"IT-32-TN-14-h\": 1, \"IT-32-TN-15-h\": 1, \"IT-32-TN-16-h\": 2, \"IT-32-TN-17-h\": 1, \"IT-32-TN-18-h\": 1, \"IT-32-TN-19-h\": 2, \"IT-32-TN-20-h\": 2, \"IT-32-TN-21-h\": 1}";
		assertEquals(expected, bindings.get("danger_h").toString());
		expected = "{\"AT-07-01-l\": 3, \"AT-07-02-l\": 3, \"AT-07-03-l\": 3, \"AT-07-04-l\": 3, \"AT-07-05-l\": 3, \"AT-07-06-l\": 3, \"AT-07-07-l\": 3, \"AT-07-08-l\": 3, \"AT-07-09-l\": 2, \"AT-07-10-l\": 3, \"AT-07-11-l\": 3, \"AT-07-12-l\": 3, \"AT-07-13-l\": 3, \"AT-07-14-l\": 2, \"AT-07-15-l\": 2, \"AT-07-16-l\": 2, \"AT-07-17-l\": 3, \"AT-07-18-l\": 3, \"AT-07-19-l\": 2, \"AT-07-20-l\": 2, \"AT-07-21-l\": 2, \"AT-07-22-l\": 2, \"AT-07-23-l\": 2, \"AT-07-24-l\": 3, \"AT-07-25-l\": 2, \"AT-07-26-l\": 3, \"AT-07-27-l\": 2, \"AT-07-28-l\": 2, \"AT-07-29-l\": 1, \"IT-32-BZ-01-l\": 2, \"IT-32-BZ-02-l\": 2, \"IT-32-BZ-03-l\": 2, \"IT-32-BZ-04-l\": 2, \"IT-32-BZ-05-l\": 2, \"IT-32-BZ-06-l\": 2, \"IT-32-BZ-07-l\": 2, \"IT-32-BZ-08-l\": 1, \"IT-32-BZ-09-l\": 2, \"IT-32-BZ-10-l\": 2, \"IT-32-BZ-11-l\": 2, \"IT-32-BZ-12-l\": 2, \"IT-32-BZ-13-l\": 2, \"IT-32-BZ-14-l\": 1, \"IT-32-BZ-15-l\": 1, \"IT-32-BZ-16-l\": 1, \"IT-32-BZ-17-l\": 1, \"IT-32-BZ-18-l\": 1, \"IT-32-BZ-19-l\": 1, \"IT-32-BZ-20-l\": 1, \"IT-32-TN-01-l\": 1, \"IT-32-TN-02-l\": 1, \"IT-32-TN-03-l\": 1, \"IT-32-TN-04-l\": 1, \"IT-32-TN-05-l\": 1, \"IT-32-TN-06-l\": 1, \"IT-32-TN-07-l\": 1, \"IT-32-TN-08-l\": 1, \"IT-32-TN-09-l\": 1, \"IT-32-TN-10-l\": 1, \"IT-32-TN-11-l\": 1, \"IT-32-TN-12-l\": 1, \"IT-32-TN-13-l\": 1, \"IT-32-TN-14-l\": 1, \"IT-32-TN-15-l\": 1, \"IT-32-TN-16-l\": 1, \"IT-32-TN-17-l\": 1, \"IT-32-TN-18-l\": 1, \"IT-32-TN-19-l\": 1, \"IT-32-TN-20-l\": 1, \"IT-32-TN-21-l\": 1}";
		assertEquals(expected, bindings.get("danger_l").toString());
		expected = "{\"AT-07-01-h\": 0, \"AT-07-02-h\": 0, \"AT-07-03-h\": 0, \"AT-07-04-h\": 0, \"AT-07-05-h\": 0, \"AT-07-06-h\": 0, \"AT-07-07-h\": 0, \"AT-07-08-h\": 0, \"AT-07-09-h\": 2200, \"AT-07-10-h\": 0, \"AT-07-11-h\": 0, \"AT-07-12-h\": 0, \"AT-07-13-h\": 0, \"AT-07-14-h\": 2200, \"AT-07-15-h\": 2200, \"AT-07-16-h\": 2200, \"AT-07-17-h\": 0, \"AT-07-18-h\": 0, \"AT-07-19-h\": 2200, \"AT-07-20-h\": 2200, \"AT-07-21-h\": 2200, \"AT-07-22-h\": 2200, \"AT-07-23-h\": 2200, \"AT-07-24-h\": 0, \"AT-07-25-h\": 1600, \"AT-07-26-h\": 0, \"AT-07-27-h\": 1600, \"AT-07-28-h\": 1600, \"AT-07-29-h\": 2000, \"IT-32-BZ-01-h\": 0, \"IT-32-BZ-02-h\": 0, \"IT-32-BZ-03-h\": 0, \"IT-32-BZ-04-h\": 0, \"IT-32-BZ-05-h\": 0, \"IT-32-BZ-06-h\": 0, \"IT-32-BZ-07-h\": 0, \"IT-32-BZ-08-h\": 2000, \"IT-32-BZ-09-h\": 0, \"IT-32-BZ-10-h\": 0, \"IT-32-BZ-11-h\": 0, \"IT-32-BZ-12-h\": 0, \"IT-32-BZ-13-h\": 0, \"IT-32-BZ-14-h\": 0, \"IT-32-BZ-15-h\": 2000, \"IT-32-BZ-16-h\": 0, \"IT-32-BZ-17-h\": 0, \"IT-32-BZ-18-h\": 2000, \"IT-32-BZ-19-h\": 2000, \"IT-32-BZ-20-h\": 2000, \"IT-32-TN-01-h\": 2500, \"IT-32-TN-02-h\": 2500, \"IT-32-TN-03-h\": 0, \"IT-32-TN-04-h\": 2500, \"IT-32-TN-05-h\": 2500, \"IT-32-TN-06-h\": 0, \"IT-32-TN-07-h\": 2500, \"IT-32-TN-08-h\": 0, \"IT-32-TN-09-h\": 2500, \"IT-32-TN-10-h\": 0, \"IT-32-TN-11-h\": 0, \"IT-32-TN-12-h\": 0, \"IT-32-TN-13-h\": 2500, \"IT-32-TN-14-h\": 0, \"IT-32-TN-15-h\": 0, \"IT-32-TN-16-h\": 2500, \"IT-32-TN-17-h\": 0, \"IT-32-TN-18-h\": 0, \"IT-32-TN-19-h\": 2500, \"IT-32-TN-20-h\": 2500, \"IT-32-TN-21-h\": 0}";
		assertEquals(expected, bindings.get("elevation_h").toString());

	}
}

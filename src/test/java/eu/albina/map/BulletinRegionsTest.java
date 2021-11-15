package eu.albina.map;

import com.github.openjson.JSONObject;
import com.google.common.io.Resources;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Regions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BulletinRegionsTest {

	@Rule
	public TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();

	@Test
	public void testRegionsFile() throws Exception {
		final Regions regions = Regions.readRegions(Resources.getResource("regions.geojson"));
		final URL resource = Resources.getResource("2019-01-17.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Path path = folder.newFile("regions.json").toPath();

		BulletinRegions.createBulletinRegions(bulletins, DaytimeDependency.fd, path, regions, false);
		final String actual = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

		final String expected = Resources.toString(Resources.getResource("2019-01-17.regions.json"),
				StandardCharsets.UTF_8);
		String expectedResult = new JSONObject(expected).toString(4);
		String actualResult = new JSONObject(actual).toString(4);
		assertEquals(expectedResult, actualResult);
	}

}

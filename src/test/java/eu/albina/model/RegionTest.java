package eu.albina.model;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.github.openjson.JSONObject;
import com.google.common.io.Resources;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RegionTest {

	@Test
	public void toJSON() throws Exception {
		final URL resource = Resources.getResource("AT-07-08.geojson");
		final JSONObject expected = new JSONObject(Resources.toString(resource, StandardCharsets.UTF_8));

		final Region region = createRegion();
		final JSONObject actual = region.toJSON();
		assertEquals(expected.toString(4), actual.toString(4));
	}

	@Test
	public void readRegion() throws Exception {
		final Region expected = createRegion();
		final URL resource = Resources.getResource("AT-07-08.geojson");
		assertEquals(expected, Region.readRegion(resource));
	}

	private Region createRegion() throws Exception {
		final Region region = new Region();
		region.setId("AT-07-08");
		return region;
	}
}

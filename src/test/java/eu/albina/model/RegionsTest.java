package eu.albina.model;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.github.openjson.JSONObject;
import com.google.common.io.Resources;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RegionsTest {

	@Test
	public void toJSON() throws Exception {
		final URL resource = Resources.getResource("regions.geojson");
		final JSONObject expected = new JSONObject(Resources.toString(resource, StandardCharsets.UTF_8));

		final Regions regions = Regions.readRegions(resource);
		final JSONObject actual = regions.toJSON();
		assertEquals(expected.toString(4), actual.toString(4));
	}

	@Test
	public void readRegions() throws Exception {
		final URL resource = Resources.getResource("regions.geojson");
		final Regions regions = Regions.readRegions(resource);
		assertEquals(70, regions.size());
		assertEquals("AT-07-01", regions.get(0).getId());
	}

}

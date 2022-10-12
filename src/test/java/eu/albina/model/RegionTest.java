package eu.albina.model;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;
import com.google.common.io.Resources;

import org.junit.Assert;
import org.junit.Test;

public class RegionTest {

	@Test
	public void testCreateObjectFromJSONAndBack() throws Exception {
		final String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		Region region = Region.readRegion(Resources.getResource("region_AT-07.json"));
		Assert.assertEquals(
			new JSONObject(expected).toString(4),
			region.toJSON().toString(4)
		);
	}

	@Test
	public void testCreateObjectFromJSONAndBackJackson() throws Exception {
		String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		Region region = objectMapper.readValue(expected, Region.class);
		String json = objectMapper.writeValueAsString(region);
		Assert.assertEquals(
			new JSONObject(expected).toString(4),
			new JSONObject(json).toString(4)
		);
	}
}

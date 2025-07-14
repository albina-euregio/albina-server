package eu.albina.model;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class RegionTest {

	@Test
	public void testCreateObjectFromJSONAndBack() throws Exception {
		final String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		Region region = Region.readRegion(Resources.getResource("region_AT-07.json"));
		JSONAssert.assertEquals(expected, region.toJSON().toString(), JSONCompareMode.LENIENT);
	}

	@Test
	public void testCreateObjectFromJSONAndBackJackson() throws Exception {
		String expected = Resources.toString(Resources.getResource("region_AT-07.json"), StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		Region region = objectMapper.readValue(expected, Region.class);
		String json = objectMapper.writeValueAsString(region);
		JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
	}
}

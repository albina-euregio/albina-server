// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.google.common.io.Resources;
import eu.albina.util.JsonUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@MicronautTest
public class AvalancheBulletinTest {

	@Inject
	ObjectMapper objectMapper;

	public static List<AvalancheBulletin> readBulletinsUsingJackson(final URL resource) throws IOException {
		final ObjectMapper objectMapper = ObjectMapper.create(Map.of(), "eu.albina"); // FIXME
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		final AvalancheBulletin[] bulletins = objectMapper.readValue(validBulletinStringFromResource, AvalancheBulletin[].class);
		return List.of(bulletins);
	}

	@BeforeEach
	void setUp() {
		JsonAssert.setOptions(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_FIELDS);
	}

	@Test
	public void testCreateObjectFromJSONAndBack1() throws Exception {
		runTest(Resources.getResource("2023-12-01.json"), JsonUtil.Views.Public.class);
	}

	@Test
	public void testCreateObjectFromJSONAndBack2() throws Exception {
		runTest(Resources.getResource("2023-12-21.json"), JsonUtil.Views.Public.class);
	}

	@Test
	public void testCreateObjectFromJSONAndBack3() throws Exception {
		runTest(Resources.getResource("2024-01-28.json"), JsonUtil.Views.Public.class);
	}

	@Test
	public void testCreateObjectFromJSONAndBack4() throws Exception {
		runTest(Resources.getResource("2025-03-14.json"), JsonUtil.Views.Public.class);
	}

	@Test
	public void testCreateObjectFromJSONAndBackInternal() throws Exception {
		runTest(Resources.getResource("2025-03-14.internal.json"), JsonUtil.Views.Internal.class);
	}

	private void runTest(URL bulletin, Class<?> view) throws IOException {
		String expected = Resources.toString(bulletin, StandardCharsets.UTF_8);
		List<AvalancheBulletin> avalancheBulletins = readBulletinsUsingJackson(bulletin);
		String actual3 = objectMapper.cloneWithViewClass(view).writeValueAsString(avalancheBulletins);
		JsonAssert.assertJsonEquals(expected, actual3);
	}

	@Test
	public void testSortByDangerRating() throws IOException {
		List<AvalancheBulletin> bulletins = new ArrayList<>(readBulletinsUsingJackson(Resources.getResource("2030-02-16_1.json")));
		Collections.sort(bulletins);
		List<Integer> actual = bulletins.stream().map(AvalancheBulletin::getHighestDangerRatingDouble).collect(Collectors.toList());
		Assertions.assertEquals(List.of(14, 10, 8, 6, 4), actual);
	}
}

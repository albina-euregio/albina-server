// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.google.common.io.Resources;
import eu.albina.util.JsonUtil;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AvalancheBulletinTest {

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

	private static void runTest(URL bulletin, Class<?> view) throws IOException, JSONException {
		String expected = Resources.toString(bulletin, StandardCharsets.UTF_8);
		List<AvalancheBulletin> avalancheBulletins = AvalancheBulletin.readBulletinsUsingJackson(bulletin);
		String actual3 = JsonUtil.writeValueUsingJackson(avalancheBulletins, view);
		JSONAssert.assertEquals(expected, actual3, JSONCompareMode.NON_EXTENSIBLE);
	}

	@Test
	public void testSortByDangerRating() throws IOException {
		List<AvalancheBulletin> bulletins = new ArrayList<>(AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2030-02-16_1.json")));
		Collections.sort(bulletins);
		List<Integer> actual = bulletins.stream().map(AvalancheBulletin::getHighestDangerRatingDouble).collect(Collectors.toList());
		Assertions.assertEquals(List.of(14, 10, 8, 6, 4), actual);
	}
}

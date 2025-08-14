// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.google.common.io.Resources;
import eu.albina.util.JsonUtil;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		List<AvalancheBulletin> bulletins = Arrays.asList(
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin.json")),
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin2.json")),
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin3.json")),
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin4.json"))
		);

		Collections.sort(bulletins);

		for (AvalancheBulletin avalancheBulletin : bulletins) {
			System.out.println(avalancheBulletin.getHighestDangerRatingDouble());
		}
	}
}

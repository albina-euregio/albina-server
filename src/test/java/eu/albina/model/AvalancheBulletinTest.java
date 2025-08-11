// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.google.common.io.Resources;

public class AvalancheBulletinTest {

	@Test
	public void testCreateObjectFromJSONAndBack() throws Exception {
		final String expected = Resources.toString(Resources.getResource("validBulletin.json"), StandardCharsets.UTF_8);
		AvalancheBulletin b = AvalancheBulletin.readBulletin(Resources.getResource("validBulletin.json"));
		JSONAssert.assertEquals(expected, b.toJSON().toString(), JSONCompareMode.LENIENT);
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

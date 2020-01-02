/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.model;

import com.google.common.io.Resources;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AvalancheBulletinTest {

	@Ignore
	@Test
	public void testCreateObjectFromJSONAndBack() throws IOException {
		final String expected = Resources.toString(Resources.getResource("validBulletin.json"), StandardCharsets.UTF_8);
		AvalancheBulletin b = AvalancheBulletin.readBulletin(Resources.getResource("validBulletin.json").openStream());
		JSONAssert.assertEquals(expected, b.toJSON(), JSONCompareMode.NON_EXTENSIBLE);
	}

	@Test
	public void testSortByDangerRating() throws IOException {
		List<AvalancheBulletin> bulletins = Arrays.asList(
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin.json").openStream()),
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin2.json").openStream()),
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin3.json").openStream()),
			AvalancheBulletin.readBulletin(Resources.getResource("validBulletin4.json").openStream())
		);

		Collections.sort(bulletins);

		for (AvalancheBulletin avalancheBulletin : bulletins) {
			System.out.println(avalancheBulletin.getHighestDangerRatingDouble());
		}
	}
}

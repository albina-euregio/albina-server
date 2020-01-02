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
package eu.albina.json;

import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AvalancheBulletinJsonValidatorTest {

	@Test
	public void testValidateAvalancheBulletinJSONValid() throws IOException {
		final URL resource = Resources.getResource("validBulletin.json");
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		assertEquals(0, JsonValidator.validateAvalancheBulletin(validBulletinStringFromResource).length());
	}

	@Test
	public void testValidateAvalancheBulletinJSONInvalid() throws IOException {
		final URL resource = Resources.getResource("invalidBulletin.json");
		final String invalidBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		assertEquals(1, JsonValidator.validateAvalancheBulletin(invalidBulletinStringFromResource).length());
	}

}

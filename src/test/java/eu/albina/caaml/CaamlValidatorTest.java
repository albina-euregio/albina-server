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
package eu.albina.caaml;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Resources;
import org.junit.Test;
import org.xml.sax.SAXException;

public class CaamlValidatorTest {

	@Test
	public void testValidateAvalancheBulletinCaamlValid() throws Exception {
		final URL resource = Resources.getResource("validBulletin.xml");
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		CaamlValidator.validateCaamlBulletin(validBulletinStringFromResource);
	}

	@Test(expected = SAXException.class)
	public void testValidateAvalancheBulletinCaamlInvalid() throws SAXException, IOException {
		final URL resource = Resources.getResource("invalidBulletin.xml");
		final String invalidBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		CaamlValidator.validateCaamlBulletin(invalidBulletinStringFromResource);
	}

	@Test
	public void testValidateAvalancheBulletinCaamlUnclear() throws Exception {
		final URL resource = Resources.getResource("unclearBulletin.xml");
		final String unclearBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		CaamlValidator.validateCaamlBulletin(unclearBulletinStringFromResource);
	}

}

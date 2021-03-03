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
package eu.albina.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.TemplateException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleHtmlUtilTest {

	private static final Logger logger = LoggerFactory.getLogger(SimpleHtmlUtilTest.class);

	private List<AvalancheBulletin> bulletins;
	private List<AvalancheBulletin> bulletinsAmPm;

	@Before
	public void setUp() throws IOException {
		// HibernateUtil.getInstance().setUp();

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletinsAmPm = new ArrayList<AvalancheBulletin>();
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_1.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_2.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_3.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_4.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_5.json")));
		bulletinsAmPm.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_6.json")));
		bulletinsAmPm.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_7.json")));
	}

	@After
	public void shutDown() {
		// HibernateUtil.getInstance().shutDown();
	}

	@Test
	public void createSimpleHtmlFreemarker() throws IOException, URISyntaxException, TemplateException {
		String expected = Resources.toString(Resources.getResource("2030-02-16.simple.html"), StandardCharsets.UTF_8).trim();
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(bulletins, LanguageCode.de, "");
		Assert.assertEquals(expected, htmlString);
	}
}

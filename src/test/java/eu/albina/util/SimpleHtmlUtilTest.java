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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.TemplateException;

public class SimpleHtmlUtilTest {

	private ServerInstance serverInstance;
	private Region regionEuregio;
	private Region regionAran;

	@Before
	public void setUp() throws Exception {
		serverInstance = new ServerInstance();
		serverInstance.setName("ALBINA-TEST");
		serverInstance.setHtmlDirectory("/mnt/simple_local/");
		serverInstance.setMapsPath("/mnt/albina_files_local/");
		serverInstance.setServerImagesUrl("/mnt/images/");
		serverInstance.setHtmlDirectory("/mnt/simple_local");
		regionEuregio = new Region();
		regionEuregio.setId("EUREGIO");
		regionEuregio.setSimpleHtmlTemplateName("simple-bulletin.min.html");
		regionAran = new Region();
		regionAran.setId("ES-CT-L");
		regionAran.setSimpleHtmlTemplateName("simple-bulletin.aran.html");
	}

	@Test
	public void createSimpleHtmlString() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("2019-01-17.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(bulletins, LanguageCode.de, regionEuregio, serverInstance).replaceAll("\\s*<", "\n<");
		String expected = Resources.toString(Resources.getResource("2019-01-17.simple.html"), StandardCharsets.UTF_8);
		Assert.assertEquals(expected.trim(), htmlString.trim());
	}

	@Test
	public void createSimpleHtmlStringAran() throws IOException, URISyntaxException, TemplateException {
		URL resource = Resources.getResource("lauegi.report-2021-01-24/2021-01-24.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(bulletins, LanguageCode.ca, regionAran, serverInstance);
		String expected = Resources.toString(Resources.getResource("lauegi.report-2021-01-24/2021-01-24.simple.html"), StandardCharsets.UTF_8);
		Assert.assertEquals(expected.trim(), htmlString.trim());
	}
}


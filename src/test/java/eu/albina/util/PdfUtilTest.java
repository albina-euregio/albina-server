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
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PdfUtilTest {

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
	public void createPdf() throws IOException, URISyntaxException {
		// PdfUtil.getInstance().createOverviewPdfs(bulletins);
		// PdfUtil.getInstance().createOverviewPdfs(bulletinsAmPm);
		// PdfUtil.getInstance().createRegionPdfs(bulletins, GlobalVariables.codeTyrol);

		PdfUtil.getInstance().createPdf(bulletins, LanguageCode.de, GlobalVariables.codeTyrol, false, false,
				"2030-02-16", "2030-02-16_00-00-00");
	}

	@Ignore
	@Test
	public void createSpecificPdfs() throws IOException, URISyntaxException {
		String filename = "2030-02-16";
		int count = 5;
		List<AvalancheBulletin> list = loadBulletins(filename, count);
		PdfUtil.getInstance().createRegionPdfs(list, GlobalVariables.codeEuregio, "2030-02-16", "2030-02-16_00-00-00");
	}

	private List<AvalancheBulletin> loadBulletins(String filename, int count) throws IOException {
		List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
		for (int i = 1; i <= count; i++) {
			bulletins = new ArrayList<AvalancheBulletin>();
			URL resource = Resources.getResource(filename + "_" + i + ".json");
			AvalancheBulletin bulletin = AvalancheBulletin.readBulletin(resource);
			result.add(bulletin);
		}
		return result;
	}
}

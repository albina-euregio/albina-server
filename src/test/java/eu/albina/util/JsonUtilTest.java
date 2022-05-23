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
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonUtilTest {

	private List<AvalancheBulletin> bulletins;
	private ServerInstance serverInstanceEuregio;
	private Region regionTirol;

	@Before
	public void setUp() throws IOException {
		serverInstanceEuregio = new ServerInstance();
		serverInstanceEuregio.setHtmlDirectory("/mnt/simple_local/");
		serverInstanceEuregio.setMapsPath("/mnt/albina_files_local/");
		serverInstanceEuregio.setPdfDirectory("/mnt/albina_files_local/");

		regionTirol = new Region();
		regionTirol.setId("AT-07");

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_1.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_2.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_3.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_4.json")));
		bulletins.add(AvalancheBulletin.readBulletin(Resources.getResource("2030-02-16_5.json")));
	}

	@Ignore
	@Test
	public void createJsonTest() throws TransformerException, IOException {
		JsonUtil.createJsonFile(bulletins, regionTirol, "2019-12-30", "2019-12-30_17-15-30", serverInstanceEuregio);
	}
}

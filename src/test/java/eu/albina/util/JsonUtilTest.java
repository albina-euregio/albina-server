// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;

public class JsonUtilTest {

	private List<AvalancheBulletin> bulletins;
	private ServerInstance serverInstanceEuregio;
	private Region regionTirol;

	@BeforeEach
	public void setUp() throws IOException {
		serverInstanceEuregio = new ServerInstance();

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

	@Test
	public void createJsonTest(@TempDir Path folder) throws TransformerException, IOException {
		serverInstanceEuregio.setHtmlDirectory(folder.toString());
		serverInstanceEuregio.setMapsPath(folder.toString());
		serverInstanceEuregio.setPdfDirectory(folder.toString());
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTirol, serverInstanceEuregio);
		avalancheReport.createJsonFile();
	}
}

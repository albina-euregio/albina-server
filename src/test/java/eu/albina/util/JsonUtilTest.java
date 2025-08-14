// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import eu.albina.RegionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.io.Resources;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.ServerInstance;

public class JsonUtilTest {

	@Test
	public void createJsonTest(@TempDir Path folder) throws IOException {
		ServerInstance serverInstanceEuregio = new ServerInstance();
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletinsUsingJackson(Resources.getResource("2030-02-16_1.json"));
		serverInstanceEuregio.setHtmlDirectory(folder.toString());
		serverInstanceEuregio.setMapsPath(folder.toString());
		serverInstanceEuregio.setPdfDirectory(folder.toString());
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, RegionTestUtils.regionTyrol, serverInstanceEuregio);
		avalancheReport.createJsonFile();
	}
}

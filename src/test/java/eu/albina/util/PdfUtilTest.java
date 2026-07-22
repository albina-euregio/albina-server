// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import com.google.common.io.Resources;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.map.MapUtilTest;
import eu.albina.model.AvalancheReport;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.enumerations.LanguageCode;

@MicronautTest
public class PdfUtilTest {

	@Inject
	AvalancheBulletinTestUtils avalancheBulletinTestUtils;

	@Inject
	RegionTestUtils regionTestUtils;

	/**
	 * @see MapUtilTest#testMapyrusMaps()
	 */
	@Test
	public void createPdf() throws Exception {
		var resource = Resources.getResource("2024-01-28.json");
		var bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		var regionEuregio = regionTestUtils.regionEuregio();
		var serverInstance = new LocalServerInstance(false, false, "https://static.avalanche.report/bulletins", null, "/tmp/bulletins/", null, null);
		var avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstance);
		new PdfUtil(avalancheReport, LanguageCode.en, false).createPdf();
	}

	/**
	 * @see MapUtilTest#testMapyrusMapsAran()
	 */
	@Test
	@Disabled
	public void createPdfAran() {
	}

}

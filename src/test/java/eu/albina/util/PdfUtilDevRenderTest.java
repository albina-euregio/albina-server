// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.nio.file.Path;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.model.AvalancheReport;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.enumerations.LanguageCode;

/**
 * Renders PDFs from bulletins of dev.avalanche.report for manual inspection.
 * <p>
 * Expects the bulletins in /tmp/pdf-redesign/*.json, e.g. from
 * https://dev.avalanche.report/api/bulletins/json?date=2026-10-18T15:00:00Z&amp;regions=AT-07&amp;regions=IT-32-BZ&amp;regions=IT-32-TN&amp;lang=en
 * Maps are read from /tmp/pdf-redesign/bulletins/{date}/, or fetched from dev.avalanche.report.
 */
@Disabled("manual inspection, requires bulletins in /tmp/pdf-redesign")
@MicronautTest
public class PdfUtilDevRenderTest {

	@Inject
	AvalancheBulletinTestUtils avalancheBulletinTestUtils;

	@Inject
	RegionTestUtils regionTestUtils;

	@Test
	public void createPdf() throws Exception {
		createPdf("/tmp/pdf-redesign/bulletins.json");
	}

	@Test
	public void createPdfDaytimeDependency() throws Exception {
		createPdf("/tmp/pdf-redesign/bulletins-ampm.json");
	}

	@Test
	public void createPdfWeather() throws Exception {
		createPdf("/tmp/pdf-redesign/bulletins-weather.json", "/tmp/pdf-redesign/pdf-weather/");
	}

	private void createPdf(String file) throws Exception {
		createPdf(file, "/tmp/pdf-redesign/pdf/");
	}

	private void createPdf(String file, String pdfDirectory) throws Exception {
		var bulletins = avalancheBulletinTestUtils.readBulletins(Path.of(file).toUri().toURL());
		var regionEuregio = regionTestUtils.regionEuregio();
		regionEuregio.setStaticUrl("https://dev.avalanche.report");
		var serverInstance = new LocalServerInstance(false, false, "/tmp/pdf-redesign/bulletins", null, pdfDirectory, null, null);
		var avalancheReport = AvalancheReport.of(bulletins, regionEuregio, serverInstance);
		System.out.println("PDF: " + new PdfUtil(avalancheReport, LanguageCode.en, false).createPdf());
	}
}

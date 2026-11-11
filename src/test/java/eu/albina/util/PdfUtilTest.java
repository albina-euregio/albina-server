// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.google.common.io.Resources;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.map.MapUtilTest;
import eu.albina.model.AvalancheBulletin;
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
	 * Aragon uses the compact layout ({@link eu.albina.model.Region#isPdfCompactLayout()}).
	 * Maps are read from /tmp/aragon-maps/{date}/, missing maps are replaced by grey placeholders.
	 */
	@Test
	public void createPdfAragon() throws Exception {
		var resource = Resources.getResource("aludes.aragon.es/2025-09-17.json");
		var bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		var regionAragon = regionTestUtils.regionAragon();
		var serverInstance = new LocalServerInstance(false, false, "/tmp/aragon-maps", null, "/tmp/bulletins/", null, null);
		var avalancheReport = AvalancheReport.of(bulletins, regionAragon, serverInstance);
		Path maps = avalancheReport.getMapsPathForPDF();
		Files.createDirectories(maps);
		for (String bw : new String[]{"", "_bw"}) {
			createPlaceholderMap(maps.resolve("fd_ES-AR_map" + bw + ".jpg"), 1890, 948);
			for (AvalancheBulletin bulletin : bulletins) {
				createPlaceholderMap(maps.resolve("ES-AR_" + bulletin.getId() + bw + ".jpg"), 354, 178);
			}
		}
		new PdfUtil(avalancheReport, LanguageCode.es, false).createPdf();
		new PdfUtil(avalancheReport, LanguageCode.es, true).createPdf();
	}

	private static void createPlaceholderMap(Path path, int width, int height) throws IOException {
		if (Files.exists(path)) {
			return;
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillRect(0, 0, width, height);
		graphics.dispose();
		ImageIO.write(image, "jpg", path.toFile());
	}

	/**
	 * @see MapUtilTest#testMapyrusMapsAran()
	 */
	@Test
	@Disabled
	public void createPdfAran() {
	}

}

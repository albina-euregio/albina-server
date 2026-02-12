package eu.albina.controller.publication;

import com.google.common.io.Resources;
import eu.albina.AvalancheBulletinTestUtils;
import eu.albina.RegionTestUtils;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.LocalServerInstance;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.GlobalVariables;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class AvalancheReportMultichannelMessageTest {

	@Inject
	AvalancheBulletinTestUtils avalancheBulletinTestUtils;

	@Inject
	RegionTestUtils regionTestUtils;

	@Inject
	private GlobalVariables globalVariables;

	@Test
	void test() throws Exception {
		LocalServerInstance serverInstance = globalVariables.getLocalServerInstance("/mnt/bulletins/", "/mnt/bulletins/");
		Region region = regionTestUtils.regionTyrol();

		URL resource = Resources.getResource("2025-03-14.json");
		List<AvalancheBulletin> bulletins = avalancheBulletinTestUtils.readBulletins(resource);
		AvalancheReport report = AvalancheReport.of(bulletins, region, serverInstance);
		MultichannelMessage message = MultichannelMessage.of(report, LanguageCode.de);
		assertEquals("Lawinenvorhersage für Tirol, Freitag, 14. März 2025", message.getSubject());
		assertEquals("Lawinenvorhersage für Freitag, 14. März 2025: https://lawinen.report/bulletin/2025-03-14", message.getSocialMediaText());
		assertEquals("https://lawinen.report/bulletin/2025-03-14", message.getWebsiteUrl());
		assertEquals("https://static.avalanche.report/bulletins/2025-03-14/2025-03-13_16-00-00/fd_AT-07_map.jpg", message.getAttachmentUrl());
	}
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import static eu.albina.RegionTestUtils.regionTyrol;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import eu.albina.model.AvalancheBulletinTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.caaml.v6.AvalancheBulletins;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

import eu.albina.caaml.Caaml6;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.enumerations.LanguageCode;

@MicronautTest
class TextToSpeechTest {

	@Inject
	TextToSpeech textToSpeech;

	private static void toCAAMLv6(String bulletinResource) throws Exception {
		URL resource = Resources.getResource(bulletinResource);
		List<AvalancheBulletin> bulletins = AvalancheBulletinTest.readBulletinsUsingJackson(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, regionTyrol, null);
		for (LanguageCode lang : avalancheReport.getRegion().getTTSLanguages()) {
			AvalancheBulletins caaml = Caaml6.toCAAML(avalancheReport, lang);
			String ssml = caaml.getBulletins().stream()
				.map(bulletin -> new TextToSpeech.ScriptEngine(bulletin).createScript())
				.collect(Collectors.joining(String.format("%n%n")));
			String expectedResource = bulletinResource.replaceAll(".json$", String.format(".%s.ssml", lang));
			// java.nio.file.Files.writeString(java.nio.file.Path.of("src/test/resources/" + expectedResource), ssml);
			String expected = Resources.toString(Resources.getResource(expectedResource), StandardCharsets.UTF_8);
			Assertions.assertEquals(expected, ssml);
		}
	}

	@Test
	public void test20190116() throws Exception {
		toCAAMLv6("2019-01-16.json");
	}

	@Test
	public void test20190117() throws Exception {
		toCAAMLv6("2019-01-17.json");
	}

	@Test
	public void test20181227() throws Exception {
		toCAAMLv6("2018-12-27.json");
	}

	@Test
	public void test20221220() throws Exception {
		toCAAMLv6("2022-12-20.json");
	}

	@Test
	public void test20231201() throws Exception {
		toCAAMLv6("2023-12-01.json");
	}

	@Test
	public void test20240128() throws Exception {
		toCAAMLv6("2024-01-28.json");
	}

	@Test
	@Disabled
	public void test20231201mp3() throws Exception {
		// GOOGLE_APPLICATION_CREDENTIALS
		URL resource = Resources.getResource("2023-12-01.json");
		List<AvalancheBulletin> bulletins = AvalancheBulletinTest.readBulletinsUsingJackson(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);
		AvalancheBulletins caaml = Caaml6.toCAAML(avalancheReport, LanguageCode.de);
		org.caaml.v6.AvalancheBulletin bulletin = caaml.getBulletins().getFirst();
		byte[] mp3 = textToSpeech.createAudioFile(bulletin);
		Path path = Path.of(bulletin.getBulletinID() + ".mp3");
		Files.write(path, mp3);
	}

}

package eu.albina.util;

import com.google.common.io.Resources;
import eu.albina.caaml.Caaml6;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.enumerations.LanguageCode;
import org.caaml.v6.AvalancheBulletins;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

class TextToSpeechTest {

	private static void toCAAMLv6(String bulletinResource) throws Exception {
		URL resource = Resources.getResource(bulletinResource);
		List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);
		AvalancheBulletins caaml = Caaml6.toCAAML(avalancheReport, LanguageCode.en);
		String ssml = caaml.getBulletins().stream().map(TextToSpeech::createScript).collect(Collectors.joining(String.format("%n%n")));
		String expectedResource = bulletinResource.replaceAll(".json$", ".ssml");
		// java.nio.file.Files.writeString(java.nio.file.Path.of("src/test/resources/" + expectedResource), ssml);
		String expected = Resources.toString(Resources.getResource(expectedResource), StandardCharsets.UTF_8);
		Assertions.assertEquals(expected, ssml);
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

}

package eu.albina.model;

import com.google.common.io.Resources;
import eu.albina.model.enumerations.LanguageCode;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class AvalancheReportTest {

	private static String buildCAAML(String resourceName) throws IOException {
		final URL resource = Resources.getResource(resourceName);
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final AvalancheReport avalancheReport = AvalancheReport.of(bulletins, null, null);
		return avalancheReport.toCAAMLv6String_2022(LanguageCode.en);
	}

	@Test
	public void toCAAMLv6_2022a() throws Exception {
		final String caaml = buildCAAML("2019-01-16.json");
		final String expected = Resources.toString(Resources.getResource("2019-01-16.caaml.v6.json"), StandardCharsets.UTF_8);
		assertEquals(expected.trim(), caaml.trim());
	}

	@Test
	public void toCAAMLv6_2022b() throws Exception {
		final String caaml = buildCAAML("2019-01-17.json");
		final String expected = Resources.toString(Resources.getResource("2019-01-17.caaml.v6.json"), StandardCharsets.UTF_8);
		assertEquals(expected.trim(), caaml.trim());
	}
}

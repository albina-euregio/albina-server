package eu.albina.util;

import com.google.common.io.Resources;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class LinkUtilTest {
	@Test
	public void testMapsUrl() throws Exception {
		try (AutoCloseable ignore = GlobalVariablesTest.withLauegiVariables()) {
			URL resource = Resources.getResource("lauegi.report-2021-12-10/2021-12-10.json");
			List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
			assertEquals("https://www.lauegi.report/bulletin/2021-12-10", LinkUtil.getBulletinUrl(bulletins, LanguageCode.ca));
			assertEquals("https://static.lauegi.report/albina_files", LinkUtil.getMapsUrl(LanguageCode.ca));
		}
	}
}

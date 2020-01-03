package eu.albina.util;

import com.google.common.io.Resources;
import eu.albina.caaml.CaamlValidator;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import org.junit.Test;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.List;

public class XmlUtilTest {

	@Test
	public void createValidCaaml() throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc = XmlUtil.createCaaml(bulletins, LanguageCode.en);
		final String xml = XmlUtil.convertDocToString(doc);
		CaamlValidator.validateCaamlBulletin(xml);
	}
}

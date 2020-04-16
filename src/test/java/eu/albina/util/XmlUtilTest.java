package eu.albina.util;

import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.io.Resources;

import eu.albina.caaml.CaamlValidator;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtilTest {

	@Ignore
	@Test
	public void createValidCaaml() throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc = XmlUtil.createCaaml(bulletins, LanguageCode.en);
		final String xml = XmlUtil.convertDocToString(doc);
		CaamlValidator.validateCaamlBulletin(xml);
	}

	@Ignore
	@Test
	public void createValidCaamlLocal() throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc = XmlUtil.createCaamlV6(bulletins, LanguageCode.en);
		final String xml = XmlUtil.convertDocToString(doc);
		CaamlValidator.validateCaamlBulletinLocalV6(xml);
		System.out.println(xml);
	}
}

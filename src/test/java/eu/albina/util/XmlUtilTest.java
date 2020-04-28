package eu.albina.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import eu.albina.caaml.CaamlVersion;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.io.Resources;

import eu.albina.caaml.CaamlValidator;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtilTest {

	private String createCaaml(CaamlVersion v5) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc = XmlUtil.createCaaml(bulletins, LanguageCode.en, v5);
		return XmlUtil.convertDocToString(doc);
	}

	@Ignore
	@Test
	public void createValidCaaml() throws Exception {
		final String xml = createCaaml(CaamlVersion.V5);
		CaamlValidator.validateCaamlBulletin(xml, CaamlVersion.V5);
	}

	@Ignore
	@Test
	public void createValidCaamlLocal() throws Exception {
		final String xml = createCaaml(CaamlVersion.V6);
		CaamlValidator.validateCaamlBulletinLocalV6(xml);
	}

	@Test
	public void createExpectedCaaml() throws Exception {
		final String expected = Resources.toString(Resources.getResource("2019-01-16.caaml.v5.xml"), StandardCharsets.UTF_8)
			.replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V5);
		Assert.assertEquals(expected, xml);
	}

	@Test
	public void createExpectedCaamlV6() throws Exception {
		final String expected = Resources.toString(Resources.getResource("2019-01-16.caaml.v6.xml"), StandardCharsets.UTF_8)
			.replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V6);
		Assert.assertEquals(expected, xml);
	}
}

package eu.albina.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.io.Resources;

import eu.albina.caaml.CaamlValidator;
import eu.albina.caaml.CaamlVersion;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtilTest {

	private String createCaaml(CaamlVersion version) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc;
		switch (version) {
		case V6:
			doc = XmlUtil.createCaamlv6(bulletins, LanguageCode.en);
			break;
		case V5:
			doc = XmlUtil.createCaamlv5(bulletins, LanguageCode.en);
			break;

		default:
			doc = XmlUtil.createCaamlv5(bulletins, LanguageCode.en);
			break;
		}
		return XmlUtil.convertDocToString(doc);
	}

	@Ignore
	@Test
	public void createValidCaamlv5() throws Exception {
		final String xml = createCaaml(CaamlVersion.V5);
		CaamlValidator.validateCaamlBulletin(xml, CaamlVersion.V5);
	}

	@Ignore
	@Test
	public void createValidCaamlv6() throws Exception {
		final String xml = createCaaml(CaamlVersion.V6);
		CaamlValidator.validateCaamlBulletinLocalV6(xml);
	}

	@Ignore
	@Test
	public void createExpectedCaamlv5() throws Exception {
		final String expected = Resources
				.toString(Resources.getResource("2019-01-16.caaml.v5.xml"), StandardCharsets.UTF_8).replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V5);
		Assert.assertEquals(expected, xml);
	}

	@Ignore
	@Test
	public void createExpectedCaamlV6() throws Exception {
		final String expected = Resources
				.toString(Resources.getResource("2019-01-16.caaml.v6.xml"), StandardCharsets.UTF_8).replace("\t", "  ");
		final String xml = createCaaml(CaamlVersion.V6);
		Assert.assertEquals(expected, xml);
	}
}
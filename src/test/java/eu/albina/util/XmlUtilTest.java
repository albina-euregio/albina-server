package eu.albina.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.io.Resources;

import eu.albina.caaml.CaamlValidator;
import eu.albina.caaml.CaamlVersion;
import eu.albina.controller.AvalancheReportController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class XmlUtilTest {

	private String createCaaml(CaamlVersion version) throws Exception {
		final URL resource = Resources.getResource("2019-01-16.json");
		final List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(resource);
		final Document doc = XmlUtil.createCaaml(bulletins, LanguageCode.en, version);
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

	@Ignore
	@Test
	public void createOldCaamlFiles() throws Exception {
		HibernateUtil.getInstance().setUp();
		for (LocalDate date = new LocalDate("2019-11-16"); date
				.isBefore(new LocalDate("2020-05-04")); date = date.plusDays(1)) {
			List<AvalancheBulletin> result = AvalancheReportController.getInstance().getPublishedBulletins(
					date.toDateTimeAtStartOfDay().withZone(DateTimeZone.UTC), GlobalVariables.regionsEuregio);
			for (LanguageCode language : GlobalVariables.languages) {
				Path path = Paths.get("/tmp/albina_files" + "/" + date + "/" + date + "_" + language + "_CAAMLv6.xml");
				Document caamlDoc = XmlUtil.createCaaml(result, language, CaamlVersion.V6);
				String caaml = XmlUtil.convertDocToString(caamlDoc);
				LoggerFactory.getLogger(getClass()).info("Writing {}", path);
				Files.write(path, caaml.getBytes(StandardCharsets.UTF_8));
			}
		}
	}
}

/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheBulletinSortByDangerRating;
import eu.albina.controller.SubscriberController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Subscriber;
import eu.albina.model.enumerations.LanguageCode;
import freemarker.template.TemplateException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UtilTest {

	private static Logger logger = LoggerFactory.getLogger(UtilTest.class);

	private List<AvalancheBulletin> bulletins;
	private List<AvalancheBulletin> bulletinsAmPm;

	private String imgBaseUrl = "D:/norbert/workspaces/albina-euregio/albina-server/src/test/resources/images/";
	private List<String> names = new ArrayList<String>();
	private List<String> passwords = new ArrayList<String>();
	private List<String> recipients = new ArrayList<String>();

	@Before
	public void setUp() {
		// HibernateUtil.getInstance().setUp();

		names.add("Alberto Trenti");
		names.add("Sergio Benigni");
		names.add("Paolo Cestari");
		names.add("Marco Gadotti");
		names.add("Walter Beozzo");
		names.add("Gianluca Tognoni");
		names.add("Günther Geier");
		names.add("Fabio Gheser");
		names.add("Lukas Rastner");
		names.add("Alex Boninsegna");
		names.add("Rudi Mair");
		names.add("Patrick Nairz");
		names.add("Lukas Ruetz");
		names.add("Matthias Walcher");
		names.add("Karel Kriz");
		names.add("Alexander Pucher");
		names.add("Daniel Nell");
		names.add("Matthias Fink");
		names.add("Christoph Mitterer");
		names.add("Norbert Lanzanasto");
		names.add("Jürg Schweizer");
		names.add("Matthias Gerber");
		names.add("Thomas Stucki");
		names.add("Kurt Winkler");
		names.add("Ulrich Niederer");
		names.add("Marc Ruesch");
		names.add("Arno Studeregger");
		names.add("Alfred Ortner");
		names.add("Jonathan Flunger");
		names.add("Felix Mast");
		names.add("Andreas Riegler");
		names.add("Simon Legner");

		passwords.add("Alberto");
		passwords.add("Sergio");
		passwords.add("Paolo");
		passwords.add("Marco");
		passwords.add("Walter");
		passwords.add("Gianluca");
		passwords.add("Günther");
		passwords.add("Fabio");
		passwords.add("Lukas");
		passwords.add("Alex");
		passwords.add("Rudi");
		passwords.add("Patrick");
		passwords.add("Lukas");
		passwords.add("Matthias");
		passwords.add("Karel");
		passwords.add("Alexander");
		passwords.add("Daniel");
		passwords.add("Matthias");
		passwords.add("Christoph");
		passwords.add("Norbert");
		passwords.add("Jürg");
		passwords.add("Matthias");
		passwords.add("Thomas");
		passwords.add("Kurt");
		passwords.add("Ulrich");
		passwords.add("Marc");
		passwords.add("Arno");
		passwords.add("Alfred");
		passwords.add("Jonathan");
		passwords.add("Felix");
		passwords.add("Andreas");
		passwords.add("Simon");

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		bulletinsAmPm = new ArrayList<AvalancheBulletin>();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("2030-02-16_1.json");
		StringBuilder bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		String validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin);

		is = classloader.getResourceAsStream("2030-02-16_2.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin2 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin2);

		is = classloader.getResourceAsStream("2030-02-16_3.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin3 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin3);

		is = classloader.getResourceAsStream("2030-02-16_4.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin4 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin4);

		is = classloader.getResourceAsStream("2030-02-16_5.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin5 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletins.add(bulletin5);

		is = classloader.getResourceAsStream("2030-02-16_6.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin6 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletinsAmPm.add(bulletin6);

		is = classloader.getResourceAsStream("2030-02-16_7.json");
		bulletinStringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = in.readLine()) != null) {
				bulletinStringBuilder.append(line);
			}
		} catch (Exception e) {
			logger.warn("Error parsing bulletin!");
		}
		validBulletinStringFromResource = bulletinStringBuilder.toString();
		AvalancheBulletin bulletin7 = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
		bulletinsAmPm.add(bulletin7);

		recipients.add("n.lanzanasto@gmail.com");
		recipients.add("norbert.lanzanasto@tirol.gv.at");
		// recipients.add("mitterer.chris@gmail.com");
		// recipients.add("chris.mitterer@tirol.gv.at");
	}

	@After
	public void shutDown() {
		// HibernateUtil.getInstance().shutDown();
	}

	@Ignore
	@Test
	public void createFreemarker() throws IOException, URISyntaxException {
		String html = EmailUtil.getInstance().createBulletinEmailHtml(bulletins, LanguageCode.de, "AT-07", false,
				false);
		System.out.println(html);
	}

	@Ignore
	@Test
	public void sendEmail() throws MessagingException, IOException, URISyntaxException {
		// TODO test this test
		ArrayList<String> regions = new ArrayList<String>();
		regions.add("AT-07");
		EmailUtil.getInstance().sendBulletinEmails(bulletins, regions, false);
	}

	@Ignore
	@Test
	public void createMaps() {
		MapUtil.createDangerRatingMaps(bulletins);
	}

	@Ignore
	@Test
	public void addSubscriber() throws KeyManagementException, CertificateException, NoSuchAlgorithmException,
			KeyStoreException, AlbinaException, IOException, Exception {
		ArrayList<String> regions = new ArrayList<String>();
		regions.add("AT-07");

		Subscriber subscriber = new Subscriber();
		subscriber.setEmail("n.lanzanasto@gmail.com");
		subscriber.setLanguage(LanguageCode.it);
		subscriber.setRegions(regions);

		SubscriberController.getInstance().createSubscriberRapidmail(subscriber);
	}

	@Ignore
	@Test
	public void sendMessengerPeopleNewsletter() throws IOException, URISyntaxException {
		// TODO test this test
		List<String> regions = new ArrayList<String>();
		regions.add(GlobalVariables.codeTrentino);
		MessengerPeopleUtil.getInstance().sendBulletinNewsletters(bulletins, regions, false);
	}

	@Ignore
	@Test
	public void createSimpleHtmlFreemarker() throws IOException, URISyntaxException, TemplateException {
		String htmlString = SimpleHtmlUtil.getInstance().createSimpleHtmlString(bulletinsAmPm, LanguageCode.de, "");
		System.out.println(htmlString);
	}

	@Ignore
	@Test
	public void createPdf() throws IOException, URISyntaxException {
		// PdfUtil.getInstance().createOverviewPdfs(bulletins);
		// PdfUtil.getInstance().createOverviewPdfs(bulletinsAmPm);
		// PdfUtil.getInstance().createRegionPdfs(bulletins, "AT-07");#
		PdfUtil.getInstance().createPdf(bulletins, LanguageCode.de, "AT-07", false, false);
	}

	@Ignore
	@Test
	public void createSpecificPdfs() throws IOException, URISyntaxException {
		String filename = "2030-02-16";
		int count = 5;
		List<AvalancheBulletin> list = loadBulletins(filename, count);
		PdfUtil.getInstance().createOverviewPdfs(list);
	}

	@Test
	public void encodeImageAndPassword() {
		for (int i = 31; i < 32; i++) {
			File f = new File(imgBaseUrl + names.get(i) + ".jpg");
			String encodstring = encodeFileToBase64Binary(f);
			String pwd = BCrypt.hashpw(passwords.get(i), BCrypt.gensalt());
			logger.warn(names.get(i));
			logger.warn("Image: " + encodstring);
			logger.warn("Password: " + pwd);
		}
	}

	@Ignore
	@Test
	public void testIsLatest() {
		DateTime dateTime = (new DateTime()).minusDays(0);
		System.out.println(AlbinaUtil.isLatest(dateTime));
	}

	@Ignore
	@Test
	public void createStaticWidget() throws IOException, URISyntaxException {
		StaticWidgetUtil.getInstance().createStaticWidget(bulletins, LanguageCode.en);
	}

	@Ignore
	@Test
	public void sortBulletinsTest() {
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			System.out.println(avalancheBulletin.getHighestDangerRating());
		}
		System.out.println("Sorting ...");
		Collections.sort(bulletins, new AvalancheBulletinSortByDangerRating());
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			System.out.println(avalancheBulletin.getHighestDangerRating());
		}
	}

	private static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
			fileInputStreamReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encodedfile;
	}

	private List<AvalancheBulletin> loadBulletins(String filename, int count) {
		List<AvalancheBulletin> result = new ArrayList<AvalancheBulletin>();
		for (int i = 1; i <= count; i++) {
			bulletins = new ArrayList<AvalancheBulletin>();
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream is = classloader.getResourceAsStream(filename + "_" + i + ".json");
			StringBuilder bulletinStringBuilder = new StringBuilder();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = in.readLine()) != null) {
					bulletinStringBuilder.append(line);
				}
			} catch (Exception e) {
				logger.warn("Error parsing bulletin!");
			}
			String validBulletinStringFromResource = bulletinStringBuilder.toString();
			AvalancheBulletin bulletin = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource));
			result.add(bulletin);
		}
		return result;
	}
}

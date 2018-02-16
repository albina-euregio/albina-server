package eu.albina.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UtilTest {

	private static Logger logger = LoggerFactory.getLogger(UtilTest.class);

	private List<AvalancheBulletin> bulletins;

	private String imgBaseUrl = "D:/norbert/workspaces/albina-euregio/albina-server/src/test/resources/images/";
	private List<String> names = new ArrayList<String>();
	private List<String> passwords = new ArrayList<String>();

	@Before
	public void setUp() {
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
		names.add("Stella Gschossmann");
		names.add("Gabriel Gätz");
		names.add("Alexander Ungerer");
		names.add("Karel Kriz");
		names.add("Alexander Pucher");
		names.add("Daniel Nell");
		names.add("Matthias Fink");
		names.add("Christoph Mitterer");
		names.add("Norbert Lanzanasto");
		names.add("Alberto Dalmaso");
		names.add("Jürg Schweizer");
		names.add("Matthias Gerber");
		names.add("Thomas Stucki");
		names.add("Kurt Winkler");
		names.add("Ulrich Niederer");
		names.add("Marc Ruesch");

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
		passwords.add("Stella");
		passwords.add("Gabriel");
		passwords.add("Alexander");
		passwords.add("Karel");
		passwords.add("Alexander");
		passwords.add("Daniel");
		passwords.add("Matthias");
		passwords.add("Christoph");
		passwords.add("Norbert");
		passwords.add("Alberto");
		passwords.add("Jürg");
		passwords.add("Matthias");
		passwords.add("Thomas");
		passwords.add("Kurt");
		passwords.add("Ulrich");
		passwords.add("Marc");

		// Load valid avalanche bulletin JSON from resources
		bulletins = new ArrayList<AvalancheBulletin>();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("validBulletin.json");
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
		AvalancheBulletin bulletin = new AvalancheBulletin(new JSONObject(validBulletinStringFromResource), null);
		bulletins.add(bulletin);
	}

	@Test
	public void createFreemarker() throws IOException, URISyntaxException {
		AlbinaUtil.createFreemarkerConfigurationInstance();
		AlbinaUtil.createEmailHtml(bulletins, LanguageCode.de);
	}

	@Ignore
	@Test
	public void encodeImageAndPassword() {
		for (int i = 0; i < 30; i++) {
			File f = new File(imgBaseUrl + names.get(i) + ".jpg");
			String encodstring = encodeFileToBase64Binary(f);
			String pwd = BCrypt.hashpw(passwords.get(i), BCrypt.gensalt());
			logger.warn(names.get(i));
			logger.warn("Image: " + encodstring);
			logger.warn("Password: " + pwd);
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encodedfile;
	}
}

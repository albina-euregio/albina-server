package eu.albina.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UtilTest {

	private static Logger logger = LoggerFactory.getLogger(UtilTest.class);

	private String imgBaseUrl = "C:/Users/Norbert Lanzanasto/Google Drive/albina/Administration/ProjektKommunikation/FotosProjectMembers/150x150/";
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
		names.add("Karel Kriz");
		names.add("Alexander Pucher");
		names.add("Daniel Nell");
		names.add("Matthias Fink");
		names.add("Christoph Mitterer");
		names.add("Norbert Lanzanasto");
		names.add("Alberto Dalmaso");

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
		passwords.add("Karel");
		passwords.add("Alexander");
		passwords.add("Daniel");
		passwords.add("Matthias");
		passwords.add("Christoph");
		passwords.add("Norbert");
		passwords.add("Alberto");
	}

	@Ignore
	@Test
	public void encodeImageAndPassword() {
		int i = 18;
		File f = new File(imgBaseUrl + names.get(i) + ".jpg");
		String encodstring = encodeFileToBase64Binary(f);
		String pwd = BCrypt.hashpw(passwords.get(i), BCrypt.gensalt());
		logger.warn(names.get(i));
		logger.warn("Image: " + encodstring);
		logger.warn("Password: " + pwd);
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

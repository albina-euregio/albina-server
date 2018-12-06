package eu.albina.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.socialmedia.MessengerPeopleProcessorController;
import eu.albina.controller.socialmedia.RegionConfigurationController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.model.socialmedia.RegionConfiguration;

public class MessengerPeopleUtil {

	private static MessengerPeopleUtil instance = null;

	private static final Logger logger = LoggerFactory.getLogger(MessengerPeopleUtil.class);

	public static MessengerPeopleUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new MessengerPeopleUtil();
		}
		return instance;
	}

	public void sendBulletinNewsletters(List<AvalancheBulletin> bulletins, List<String> regions) {
		for (LanguageCode lang : GlobalVariables.languages) {
			try {
				DateTime date = AlbinaUtil.getDate(bulletins);
				String message = GlobalVariables.getMessengerPeopleText(lang, date);

				String attachmentUrl = "";
				attachmentUrl = GlobalVariables.getMapsPath() + AlbinaUtil.getValidityDate(bulletins)
						+ "/fd_albina_map.jpg";

				sendBulletinNewsletter(message, attachmentUrl, lang, regions);
			} catch (UnsupportedEncodingException e) {
				logger.error("Bulletin newsletter could not be sent: " + e.getMessage());
				e.printStackTrace();
			} catch (AlbinaException e) {
				logger.error("Bulletin newsletter could not be sent: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("Bulletin newsletter could not be sent: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void sendBulletinNewsletter(String message, String attachmentUrl, LanguageCode lang, List<String> regions)
			throws AlbinaException, IOException {
		MessengerPeopleProcessorController ctMp = MessengerPeopleProcessorController.getInstance();
		for (String region : regions) {
			RegionConfiguration rc = RegionConfigurationController.getInstance().getRegionConfiguration(region);
			ctMp.sendNewsLetter(rc.getMessengerPeopleConfig(), lang.toString(), message, attachmentUrl);
		}
	}
}
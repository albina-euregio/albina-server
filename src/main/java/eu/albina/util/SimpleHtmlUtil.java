package eu.albina.util;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class SimpleHtmlUtil {

	private static final Logger logger = LoggerFactory.getLogger(SimpleHtmlUtil.class);

	private static SimpleHtmlUtil instance = null;

	public static final Color blueColor = new Color(0, 172, 251);
	public static final Color greyLightColor = new Color(201, 201, 201);
	public static final Color greyDarkColor = new Color(85, 95, 96);
	public static final Color whiteColor = new Color(255, 255, 255);
	public static final Color greyVeryVeryLightColor = new Color(242, 247, 250);
	public static final Color dangerLevel1Color = new Color(197, 255, 118);
	public static final Color dangerLevel2Color = new Color(255, 255, 70);
	public static final Color dangerLevel3Color = new Color(255, 152, 44);
	public static final Color dangerLevel4Color = new Color(255, 0, 23);
	public static final Color dangerLevel5ColorRed = new Color(255, 0, 23);
	public static final Color dangerLevel5ColorBlack = new Color(0, 0, 0);

	protected SimpleHtmlUtil() throws IOException, URISyntaxException {
	}

	public static SimpleHtmlUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new SimpleHtmlUtil();
		}
		return instance;
	}

	/**
	 * Create static widgets containing overview information for the EUREGIO for
	 * print media.
	 * 
	 * @param bulletins
	 *            The bulletins to create the PDF of.
	 */
	public void createSimpleHtml(List<AvalancheBulletin> bulletins) {
		for (LanguageCode lang : GlobalVariables.languages)
			createSimpleHtml(bulletins, lang);
	}

	// LANG
	public void createSimpleHtml(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		StringBuilder sb = new StringBuilder();
	}
}

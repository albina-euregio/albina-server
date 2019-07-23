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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.AttributedString;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public class StaticWidgetUtil {

	private static final Logger logger = LoggerFactory.getLogger(StaticWidgetUtil.class);

	private static StaticWidgetUtil instance = null;

	public static final String OPEN_SANS_REGULAR = "/src/main/resources/fonts/open-sans/OpenSans-Regular.ttf";
	public static final String OPEN_SANS_BOLD = "/src/main/resources/fonts/open-sans/OpenSans-Bold.ttf";
	public static final String OPEN_SANS_LIGHT = "/src/main/resources/fonts/open-sans/OpenSans-Light.ttf";

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

	public static final Font openSansRegularFont = new Font("Open Sans", Font.PLAIN, 24);
	public static final Font openSansBoldFont = new Font("Open Sans", Font.BOLD, 24);
	public static final Font openSansBoldBigFont = new Font("Open Sans", Font.BOLD, 30);

	protected StaticWidgetUtil() throws IOException, URISyntaxException {
	}

	public static StaticWidgetUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new StaticWidgetUtil();
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
	public void createStaticWidgets(List<AvalancheBulletin> bulletins) {
		for (LanguageCode lang : GlobalVariables.languages)
			createStaticWidget(bulletins, lang);
	}

	// LANG
	public void createStaticWidget(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		try {
			int width = 600;
			int height = 800;

			DangerRating highestDangerRating = GlobalVariables.getHighestDangerRating(bulletins);
			String date = AlbinaUtil.getShortDate(bulletins, lang);

			// TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
			// into integer pixels
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D ig2 = bi.createGraphics();
			ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			ig2.setPaint(whiteColor);
			ig2.fill(new Rectangle2D.Double(0, 0, 600, 800));

			ig2.setPaint(greyVeryVeryLightColor);
			ig2.fill(new Rectangle2D.Double(0, 570, 600, 230));

			ig2.setPaint(greyLightColor);
			ig2.fill(new Rectangle2D.Double(0, 170, 600, 1));

			ig2.setPaint(greyLightColor);
			ig2.fill(new Rectangle2D.Double(0, 569, 600, 1));

			BufferedImage ci = resize(
					loadImageFromUrl(GlobalVariables.getServerImagesUrlLocalhost() + "logo/color/colorbar.gif"), 600,
					15);

			BufferedImage overviewThumbnail;
			if (AlbinaUtil.hasDaytimeDependency(bulletins))
				overviewThumbnail = resizeWidth(
						loadImageFromUrl(GlobalVariables.getMapsPath() + AlbinaUtil.getValidityDateString(bulletins)
								+ "/" + AlbinaUtil.getPublicationTime(bulletins) + "/fd_albina_thumbnail.jpg"),
						600);
			else
				overviewThumbnail = resizeHeight(
						loadImageFromUrl(GlobalVariables.getMapsPath() + AlbinaUtil.getValidityDateString(bulletins)
								+ "/" + AlbinaUtil.getPublicationTime(bulletins) + "/fd_albina_thumbnail.jpg"),
						400);

			if (highestDangerRating != DangerRating.very_high) {
				ig2.setPaint(getDangerRatingColor(highestDangerRating));
				ig2.fill(new Rectangle2D.Double(0, 570, 15, 230));
			} else {
				for (int j = 0; j < 33; j++) {
					for (int i = 0; i < 2; i++) {
						if (i % 2 == 0) {
							if (j % 2 == 0)
								ig2.setPaint(dangerLevel5ColorBlack);
							else
								ig2.setPaint(dangerLevel5ColorRed);
						} else {
							if (j % 2 == 0)
								ig2.setPaint(dangerLevel5ColorRed);
							else
								ig2.setPaint(dangerLevel5ColorBlack);
						}
						ig2.fill(new Rectangle2D.Double(0 + i * 7, 570 + j * 7, 7, 7));
					}
				}
			}

			ig2.setPaint(greyDarkColor);

			BufferedImage logo;
			String firstLine;
			String secondLine;
			String thirdLine;
			String fourthLine;
			AttributedString asFirstLine;
			AttributedString asSecondLine;
			AttributedString asThirdLine;
			AttributedString asFourthLine;
			switch (lang) {
			case de:
				logo = loadImageFromFile("logo/color/lawinen_report.png");

				firstLine = "Für " + date + " maximal";
				asFirstLine = new AttributedString(firstLine);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 4, 4 + date.length());
				asFirstLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 4, 4 + date.length());

				secondLine = GlobalVariables.getDangerRatingTextMiddle(highestDangerRating, lang);
				asSecondLine = new AttributedString(secondLine);
				asSecondLine.addAttribute(TextAttribute.FONT, openSansBoldBigFont);
				asSecondLine.addAttribute(TextAttribute.FOREGROUND, getDangerRatingTextColor(highestDangerRating));

				thirdLine = "Die komplette Lawinenvorhersage und wo's";
				asThirdLine = new AttributedString(thirdLine);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 14, 31);
				asThirdLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 14, 31);

				fourthLine = "besser ist, findet ihr auf:";
				asFourthLine = new AttributedString(fourthLine);
				asFourthLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				break;
			case it:
				logo = loadImageFromFile("logo/color/valanghe_report.png");

				firstLine = "Per " + date + " al massimo";
				asFirstLine = new AttributedString(firstLine);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 4, 4 + date.length());
				asFirstLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 4, 4 + date.length());

				secondLine = GlobalVariables.getDangerRatingTextLong(highestDangerRating, lang);
				asSecondLine = new AttributedString(secondLine);
				asSecondLine.addAttribute(TextAttribute.FONT, openSansBoldBigFont);
				asSecondLine.addAttribute(TextAttribute.FOREGROUND, getDangerRatingTextColor(highestDangerRating));

				thirdLine = "Il bollettino valanghe completo e dov'è";
				asThirdLine = new AttributedString(thirdLine);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 3, 23);
				asThirdLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 3, 23);

				fourthLine = "meglio, trovi su:";
				asFourthLine = new AttributedString(fourthLine);
				asFourthLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				break;
			case en:
				logo = loadImageFromFile("logo/color/avalanche_report.png");

				firstLine = "On " + date + " at maximum";
				asFirstLine = new AttributedString(firstLine);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 3, 4 + date.length());
				asFirstLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 3, 4 + date.length());

				secondLine = GlobalVariables.getDangerRatingTextLong(highestDangerRating, lang);
				asSecondLine = new AttributedString(secondLine);
				asSecondLine.addAttribute(TextAttribute.FONT, openSansBoldBigFont);
				asSecondLine.addAttribute(TextAttribute.FOREGROUND, getDangerRatingTextColor(highestDangerRating));

				thirdLine = "The complete avalanche forecast and where it's";
				asThirdLine = new AttributedString(thirdLine);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 13, 31);
				asThirdLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 13, 31);

				fourthLine = "going to be better:";
				asFourthLine = new AttributedString(fourthLine);
				asFourthLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				break;
			default:
				logo = loadImageFromFile("logo/color/avalanche_report.png");

				firstLine = "On " + date + " at maximum";
				asFirstLine = new AttributedString(firstLine);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asFirstLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 3, 4 + date.length());
				asFirstLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 3, 4 + date.length());

				secondLine = GlobalVariables.getDangerRatingTextLong(highestDangerRating, lang);
				asSecondLine = new AttributedString(secondLine);
				asSecondLine.addAttribute(TextAttribute.FONT, openSansBoldBigFont);
				asSecondLine.addAttribute(TextAttribute.FOREGROUND, getDangerRatingTextColor(highestDangerRating));

				thirdLine = "The complete avalanche forecast and where it's";
				asThirdLine = new AttributedString(thirdLine);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				asThirdLine.addAttribute(TextAttribute.FONT, openSansBoldFont, 13, 31);
				asThirdLine.addAttribute(TextAttribute.FOREGROUND, blueColor, 13, 31);

				fourthLine = "going to be better:";
				asFourthLine = new AttributedString(fourthLine);
				asFourthLine.addAttribute(TextAttribute.FONT, openSansRegularFont);
				break;
			}

			// Danger rating headline
			FontRenderContext frc = ig2.getFontRenderContext();
			Rectangle2D textBound = openSansBoldBigFont.getStringBounds(secondLine, frc);
			double headlineWidth = textBound.getWidth();
			double headlineHeight = textBound.getHeight();
			ig2.setPaint(getDangerRatingBackgroundColor(highestDangerRating));
			ig2.fill(new Rectangle2D.Double(30, 615, headlineWidth + 15, headlineHeight));

			ig2.setPaint(greyDarkColor);
			ig2.drawString(asFirstLine.getIterator(), 30, 605);
			ig2.drawString(asSecondLine.getIterator(), 30, 645);
			ig2.drawString(asThirdLine.getIterator(), 30, 690);
			ig2.drawString(asFourthLine.getIterator(), 30, 718);

			// Blue button at bottom
			String urlLine = GlobalVariables.getCapitalUrl(lang);
			AttributedString asUrlLine = new AttributedString(urlLine);
			asUrlLine.addAttribute(TextAttribute.FONT, openSansBoldFont);
			asUrlLine.addAttribute(TextAttribute.FOREGROUND, whiteColor);
			ig2.drawString(asUrlLine.getIterator(), 40, 770);

			frc = ig2.getFontRenderContext();
			textBound = openSansBoldFont.getStringBounds(urlLine, frc);
			double urlWidth = textBound.getWidth();
			double urlHeight = textBound.getHeight();
			ig2.setPaint(blueColor);
			ig2.fill(new Rectangle2D.Double(30, 740, urlWidth + 20, urlHeight + 10));

			ig2.setPaint(greyDarkColor);
			ig2.drawString(asFirstLine.getIterator(), 30, 605);
			ig2.drawString(asSecondLine.getIterator(), 30, 645);
			ig2.drawString(asThirdLine.getIterator(), 30, 690);
			ig2.drawString(asFourthLine.getIterator(), 30, 718);
			ig2.drawString(asUrlLine.getIterator(), 40, 770);

			logo = resizeHeight(logo, 110);
			ig2.drawImage(ci, 0, 0, null);
			ig2.drawImage(logo, 70, 35, null);

			if (AlbinaUtil.hasDaytimeDependency(bulletins))
				ig2.drawImage(overviewThumbnail, 0, 220, null);
			else
				ig2.drawImage(overviewThumbnail, 100, 170, null);

			BufferedImage interregLogo = loadImageFromFile("logo/color/interreg.png");
			// BufferedImage interregLogo =
			// loadImage(GlobalVariables.getServerImagesUrl() +
			// "logo/interreg_italia-oesterreich_02_RGB.png");
			interregLogo = resizeHeight(interregLogo, 110);
			ig2.drawImage(interregLogo, 350, 45, null);

			String filename = GlobalVariables.getPdfDirectory() + AlbinaUtil.getValidityDateString(bulletins) + "/"
					+ AlbinaUtil.getFilenameDate(bulletins, lang) + ".png";
			ImageIO.write(bi, "PNG", new File(filename));
			// ImageIO.write(bi, "PNG", new File("./yourImageName.PNG"));
			// ImageIO.write(bi, "JPEG", new File("c:\\yourImageName.JPG"));
			// ImageIO.write(bi, "gif", new File("c:\\yourImageName.GIF"));
			// ImageIO.write(bi, "BMP", new File("c:\\yourImageName.BMP"));

			AlbinaUtil.setFilePermissions(filename);
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	private static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	private static BufferedImage resizeWidth(BufferedImage img, int newW) {
		double oldW = img.getWidth();
		double oldH = img.getHeight();
		double factor = (oldH / oldW);
		double newHDouble = factor * newW;
		int newH = (int) newHDouble;

		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	private static BufferedImage resizeHeight(BufferedImage img, int newH) {
		double oldW = img.getWidth();
		double oldH = img.getHeight();
		double factor = (oldW / oldH);
		double newWDouble = factor * newH;
		int newW = (int) newWDouble;

		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	private BufferedImage loadImageFromUrl(String path) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new URL(path).openStream());
		} catch (IOException e) {
			logger.error("Error loading image: " + path);
			e.printStackTrace();
		}
		return img;
	}

	private BufferedImage loadImageFromFile(String path) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(getClass().getResource(GlobalVariables.getLocalImagesPath() + path));
		} catch (IOException e) {
			logger.error("Error loading image: " + GlobalVariables.getLocalImagesPath() + path);
			e.printStackTrace();
		}
		return img;
	}

	private Color getDangerRatingColor(DangerRating dangerRating) {
		switch (dangerRating) {
		case low:
			return dangerLevel1Color;
		case moderate:
			return dangerLevel2Color;
		case considerable:
			return dangerLevel3Color;
		case high:
			return dangerLevel4Color;
		case very_high:
			return dangerLevel5ColorRed;
		default:
			return whiteColor;
		}
	}

	private Color getDangerRatingBackgroundColor(DangerRating dangerRating) {
		switch (dangerRating) {
		case low:
			return dangerLevel1Color;
		case moderate:
			return dangerLevel2Color;
		case considerable:
			return greyVeryVeryLightColor;
		case high:
			return greyVeryVeryLightColor;
		case very_high:
			return greyVeryVeryLightColor;
		default:
			return greyVeryVeryLightColor;
		}
	}

	private Color getDangerRatingTextColor(DangerRating dangerRating) {
		switch (dangerRating) {
		case low:
			return greyDarkColor;
		case moderate:
			return greyDarkColor;
		case considerable:
			return dangerLevel3Color;
		case high:
			return dangerLevel4Color;
		case very_high:
			return dangerLevel5ColorRed;
		default:
			return greyDarkColor;
		}
	}
}

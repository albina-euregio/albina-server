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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class AvalancheBulletinEventHandler implements IEventHandler {

	private List<AvalancheBulletin> bulletins;
	private LanguageCode lang;
	private boolean grayscale;

	public static final String OPEN_SANS_REGULAR = "./src/main/resources/fonts/open-sans/OpenSans-Regular.ttf";
	public static final String OPEN_SANS_BOLD = "./src/main/resources/fonts/open-sans/OpenSans-Bold.ttf";
	public static final String OPEN_SANS_LIGHT = "./src/main/resources/fonts/open-sans/OpenSans-Light.ttf";

	public static final Color blueColor = new DeviceRgb(0, 172, 251);
	// TODO add correct bw color value
	public static final Color blueColorBw = new DeviceRgb(142, 142, 142);
	public static final Color greyDarkColor = new DeviceRgb(85, 95, 96);
	public static final Color whiteColor = new DeviceRgb(255, 255, 255);
	public static final Color greyVeryVeryLightColor = new DeviceRgb(242, 247, 250);

	public AvalancheBulletinEventHandler(LanguageCode lang, List<AvalancheBulletin> bulletins, boolean grayscale) {
		this.lang = lang;
		this.bulletins = bulletins;
		this.grayscale = grayscale;
	}

	public void handleEvent(Event event) {
		try {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdfDoc = docEvent.getDocument();
			PdfPage page = docEvent.getPage();
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

			Color blue;
			if (grayscale)
				blue = blueColorBw;
			else
				blue = blueColor;

			PdfFontFactory.registerDirectory(GlobalVariables.getLocalFontsPath());
			PdfFont openSansRegularFont;
			PdfFont openSansBoldFont;
			PdfFont openSansLightFont;
			openSansRegularFont = PdfFontFactory.createRegisteredFont("opensans", PdfEncodings.WINANSI, true);
			openSansBoldFont = PdfFontFactory.createRegisteredFont("opensans-bold", PdfEncodings.WINANSI, true);
			openSansLightFont = PdfFontFactory.createRegisteredFont("opensans-light", PdfEncodings.WINANSI, true);
			// fallback if font is not found
			if (openSansRegularFont == null || openSansBoldFont == null) {
				openSansRegularFont = PdfFontFactory.createRegisteredFont("helvetica", PdfEncodings.WINANSI, true);
				openSansBoldFont = PdfFontFactory.createRegisteredFont("helvetica-bold", PdfEncodings.WINANSI, true);
				openSansLightFont = PdfFontFactory.createRegisteredFont("helvetica", PdfEncodings.WINANSI, true);
			}

			// Add headline
			String headline = GlobalVariables.getHeadlineText(lang);
			pdfCanvas.beginText().setFontAndSize(openSansLightFont, 14).moveText(20, pageSize.getTop() - 40)
					.setColor(greyDarkColor, true).showText(headline).endText();
			String date = AlbinaUtil.getDate(bulletins, lang);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 16).moveText(20, pageSize.getTop() - 60)
					.setColor(blue, true).showText(date).endText();

			String publicationDate = AlbinaUtil.getPublicationDate(bulletins, lang);
			if (!publicationDate.isEmpty())
				pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, pageSize.getTop() - 75)
						.setColor(greyDarkColor, true)
						.showText(GlobalVariables.getPublishedText(lang) + publicationDate).endText();

			Canvas canvas = new Canvas(pdfCanvas, pdfDoc, page.getPageSize());

			// Add copyright
			String copyright = GlobalVariables.getCopyrightText(lang);
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, 20).setColor(blue, true)
					.showText(copyright).endText();

			String urlString = GlobalVariables.getCapitalUrl(lang);
			Rectangle buttonRectangle = new Rectangle(pageSize.getWidth() - 150, 12, 130, 24);
			pdfCanvas.rectangle(buttonRectangle).setColor(blue, true).fill();
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 8)
					.moveText(buttonRectangle.getLeft() + 15, buttonRectangle.getBottom() + 9)
					.setColor(whiteColor, true).showText(urlString).endText();

			// Draw lines
			pdfCanvas.setLineWidth(1).setStrokeColor(blue).moveTo(0, pageSize.getHeight() - 90)
					.lineTo(pageSize.getWidth(), pageSize.getHeight() - 90).stroke();
			pdfCanvas.setLineWidth(1).setStrokeColor(blue).moveTo(0, 48).lineTo(pageSize.getWidth(), 48).stroke();

			// Add CI
			Image ciImg;
			if (grayscale)
				ciImg = PdfUtil.getInstance().getImage("logo/grey/colorbar.gif");
			else
				ciImg = PdfUtil.getInstance().getImage("logo/color/colorbar.gif");
			ciImg.scaleAbsolute(pageSize.getWidth(), 4);
			ciImg.setFixedPosition(0, pageSize.getHeight() - 4);
			canvas.add(ciImg);

			// Add logo
			Image logoImg = PdfUtil.getInstance().getImage(GlobalVariables.getLogoPath(lang, grayscale));
			logoImg.scaleToFit(130, 55);
			logoImg.setFixedPosition(pageSize.getWidth() - 100, pageSize.getHeight() - 72);
			canvas.add(logoImg);

			// Add INTERREG logo
			Image interregImg = PdfUtil.getInstance().getImage(GlobalVariables.getInterregLogoPath(grayscale));
			interregImg.scaleToFit(130, 45);
			interregImg.setFixedPosition(20, 0);
			canvas.add(interregImg);

			// Add page number
			int pageNumber = docEvent.getDocument().getPageNumber(page);
			String pageText = String.format(GlobalVariables.getPageNumberText(lang), pageNumber);
			double width = openSansRegularFont.getContentWidth(new PdfString(pageText)) * 0.001f * 12 / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 12)
					.moveText(pageSize.getWidth() / 2 - width / 2, 20).setColor(greyDarkColor, true).showText(pageText)
					.endText();

			canvas.close();

			pdfCanvas.release();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}

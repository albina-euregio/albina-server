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
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.Region;
import eu.albina.model.enumerations.LanguageCode;

import static eu.albina.util.PdfUtil.blueColorBw;
import static eu.albina.util.PdfUtil.greyDarkColor;
import static eu.albina.util.PdfUtil.redColor;
import static eu.albina.util.PdfUtil.whiteColor;

public class AvalancheBulletinEventHandler implements IEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(AvalancheBulletinEventHandler.class);

	private final List<AvalancheBulletin> bulletins;
	private final Region region;
	private final LanguageCode lang;
	private final boolean grayscale;
	private final boolean preview;
	private final boolean isAran;

	public AvalancheBulletinEventHandler(LanguageCode lang, Region region, List<AvalancheBulletin> bulletins, boolean grayscale, boolean preview) {
		this.lang = lang;
		this.region = region;
		this.bulletins = bulletins;
		this.grayscale = grayscale;
		this.preview = preview;
		// TODO remove
		this.isAran = GlobalVariables.codeAran.equals(region.getId());
	}

	@Override
	public void handleEvent(Event event) {
		try {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdfDoc = docEvent.getDocument();
			PdfPage page = docEvent.getPage();
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

			Color blue = grayscale ? blueColorBw : PdfUtil.getInstance().getColor(region.getPdfColor());

			PdfFont openSansRegularFont = PdfUtil.createFont("fonts/open-sans/OpenSans-Regular.ttf");
			PdfFont openSansBoldFont = PdfUtil.createFont("fonts/open-sans/OpenSans-Bold.ttf");
			PdfFont openSansLightFont = PdfUtil.createFont("fonts/open-sans/OpenSans-Light.ttf");

			// Add headline
			String headline = isAran ? "Centre de Lauegi Val d'Aran" : lang.getBundleString("avalanche-report.name");
			pdfCanvas.beginText().setFontAndSize(openSansLightFont, 14).moveText(20, pageSize.getTop() - 40)
					.setColor(greyDarkColor, true).showText(headline).endText();
			String date = AlbinaUtil.getDate(bulletins, lang);
			if (preview) {
				String preview = lang.getBundleString("avalanche-report.preview");
				pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 16).moveText(20, pageSize.getTop() - 60)
				.setColor(redColor, true).showText(date + preview).endText();
			} else {
				pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 16).moveText(20, pageSize.getTop() - 60)
				.setColor(blue, true).showText(date).endText();
			}

			String publicationDate = AlbinaUtil.getPublicationDate(bulletins, lang);
			if (!publicationDate.isEmpty()) {
				if (AlbinaUtil.isUpdate(bulletins))
					pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, pageSize.getTop() - 75)
							.setColor(greyDarkColor, true).showText(lang.getBundleString("updated") + publicationDate)
							.endText();
				else
					pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, pageSize.getTop() - 75)
							.setColor(greyDarkColor, true).showText(lang.getBundleString("published") + publicationDate)
							.endText();
			}

			Canvas canvas = new Canvas(pdfCanvas, pdfDoc, page.getPageSize());

			// Add copyright
			String copyright = GlobalVariables.getCopyrightText(lang);
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, 20).setColor(blue, true)
					.showText(copyright).endText();

			String urlString = isAran ? "WWW.LAUEGI.REPORT" : lang.getBundleString("avalanche-report.url.capitalized");
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
				ciImg = PdfUtil.getInstance().getImage("images/logo/grey/colorbar.gif");
			else if (isAran)
				ciImg = PdfUtil.getInstance().getImage("images/logo/color/colorbar.Aran.gif");
			else
				ciImg = PdfUtil.getInstance().getImage("images/logo/color/colorbar.gif");
			ciImg.scaleAbsolute(pageSize.getWidth(), 4);
			ciImg.setFixedPosition(0, pageSize.getHeight() - 4);
			canvas.add(ciImg);

			// Add logo
			Image logoImg;
			if (grayscale)
				logoImg = PdfUtil.getInstance().getImage("images/" + lang.getBundleString("avalanche-report.logo.path.bw"));
			else if (isAran)
				logoImg = PdfUtil.getInstance().getImage("images/logo/color/lauegi.png");
			else
				logoImg = PdfUtil.getInstance().getImage("images/" + lang.getBundleString("avalanche-report.logo.path"));
			logoImg.scaleToFit(130, 55);
			logoImg.setFixedPosition(pageSize.getWidth() - 110, pageSize.getHeight() - 75);
			canvas.add(logoImg);

			// Add EUREGIO logo
			if (!isAran) {
				Image euregioImg = PdfUtil.getInstance().getImage("images/" + GlobalVariables.getEuregioLogoPath(grayscale));
				euregioImg.scaleToFit(120, 40);
				euregioImg.setFixedPosition(15, 5);
				canvas.add(euregioImg);
			}

			// Add page number
			int pageNumber = docEvent.getDocument().getPageNumber(page);
			String pageText = MessageFormat.format(lang.getBundleString("pdf.page-number"), pageNumber);
			double width = openSansRegularFont.getContentWidth(new PdfString(pageText)) * 0.001f * 12 / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 9)
					.moveText(pageSize.getWidth() / 2 - width / 2, 20).setColor(greyDarkColor, true).showText(pageText)
					.endText();

			canvas.close();
			pdfCanvas.release();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}
}

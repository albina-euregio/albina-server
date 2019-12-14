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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.DocumentRenderer;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheSituation;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public class PdfUtil {

	private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

	private static PdfUtil instance = null;

	public static final String OPEN_SANS_REGULAR = "/src/main/resources/fonts/open-sans/OpenSans-Regular.ttf";
	public static final String OPEN_SANS_BOLD = "/src/main/resources/fonts/open-sans/OpenSans-Bold.ttf";
	public static final String OPEN_SANS_LIGHT = "/src/main/resources/fonts/open-sans/OpenSans-Light.ttf";

	public static final Color blueColor = new DeviceRgb(0, 172, 251);
	public static final Color blackColor = new DeviceRgb(0, 0, 0);
	public static final Color greyDarkColor = new DeviceRgb(85, 95, 96);
	public static final Color whiteColor = new DeviceRgb(255, 255, 255);
	public static final Color greyVeryVeryLightColor = new DeviceRgb(242, 247, 250);

	public static final Color dangerLevel1Color = new DeviceRgb(197, 255, 118);
	public static final Color dangerLevel2Color = new DeviceRgb(255, 255, 70);
	public static final Color dangerLevel3Color = new DeviceRgb(255, 152, 44);
	public static final Color dangerLevel4Color = new DeviceRgb(255, 0, 23);
	public static final Color dangerLevel5ColorRed = new DeviceRgb(255, 0, 23);
	public static final Color dangerLevel5ColorBlack = new DeviceRgb(0, 0, 0);

	public static final Color blueColorBw = new DeviceRgb(142, 142, 142);
	public static final Color dangerLevel1ColorBw = new DeviceRgb(239, 239, 239);
	public static final Color dangerLevel2ColorBw = new DeviceRgb(216, 216, 216);
	public static final Color dangerLevel3ColorBw = new DeviceRgb(176, 176, 176);
	public static final Color dangerLevel4ColorBw = new DeviceRgb(136, 136, 136);
	public static final Color dangerLevel5ColorRedBw = new DeviceRgb(136, 136, 136);
	public static final Color dangerLevel5ColorBlackBw = new DeviceRgb(70, 70, 70);
	public static final Color greyVeryVeryLightColorBw = new DeviceRgb(246, 246, 246);

	private static PdfFont openSansRegularFont;
	private static PdfFont openSansBoldFont;

	protected PdfUtil() throws IOException, URISyntaxException {
		initialize();
	}

	private void initialize() throws IOException {
		PdfFontFactory.registerDirectory("./src/main/resources/fonts/open-sans");
	}

	public static PdfUtil getInstance() throws IOException, URISyntaxException {
		if (instance == null) {
			instance = new PdfUtil();
		}
		return instance;
	}

	/**
	 * Create a PDF containing all the information for the EUREGIO.
	 * 
	 * @param bulletins
	 *            The bulletins to create the PDF of.
	 * @param publicationTimeString
	 *            the time of publication
	 * @param validityDateString
	 *            the start of the validity of the report
	 */
	public boolean createOverviewPdfs(List<AvalancheBulletin> bulletins, String validityDateString,
			String publicationTimeString) {
		boolean result = true;
		boolean daytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		for (LanguageCode lang : GlobalVariables.languages) {
			if (!createPdf(bulletins, lang, null, false, daytimeDependency, validityDateString, publicationTimeString))
				result = false;
			if (!createPdf(bulletins, lang, null, true, daytimeDependency, validityDateString, publicationTimeString))
				result = false;
		}
		return result;
	}

	public boolean createPdf(List<AvalancheBulletin> bulletins, LanguageCode lang, String region, boolean grayscale,
			boolean daytimeDependency, String validityDateString, String publicationTimeString) {
		PdfDocument pdf;
		PdfWriter writer;

		try {
			String filename;

			// TODO use correct region string
			if (region != null) {
				if (grayscale) {
					filename = GlobalVariables.getPdfDirectory() + "/" + validityDateString + "/"
							+ publicationTimeString + "/" + validityDateString + "_" + region + "_" + lang.toString()
							+ "_bw.pdf";
					writer = new PdfWriter(filename);
				} else {
					filename = GlobalVariables.getPdfDirectory() + "/" + validityDateString + "/"
							+ publicationTimeString + "/" + validityDateString + "_" + region + "_" + lang.toString()
							+ ".pdf";
					writer = new PdfWriter(filename);
				}
			} else {
				if (grayscale) {
					filename = GlobalVariables.getPdfDirectory() + "/" + validityDateString + "/"
							+ publicationTimeString + "/" + validityDateString + "_" + lang.toString() + "_bw.pdf";
					writer = new PdfWriter(filename);
				} else {
					filename = GlobalVariables.getPdfDirectory() + "/" + validityDateString + "/"
							+ publicationTimeString + "/" + validityDateString + "_" + lang.toString() + ".pdf";
					writer = new PdfWriter(filename);
				}
			}

			pdf = new PdfDocument(writer);

			// PdfFontFactory.registerDirectory("./src/main/resources/fonts/open-sans");
			PdfFontFactory.registerDirectory(GlobalVariables.getLocalFontsPath());
			// for (String font : PdfFontFactory.getRegisteredFonts()) {
			// System.out.println(font);
			// }
			openSansRegularFont = PdfFontFactory.createRegisteredFont("opensans", PdfEncodings.WINANSI, true);
			openSansBoldFont = PdfFontFactory.createRegisteredFont("opensans-bold", PdfEncodings.WINANSI, true);
			// fallback if font is not found
			if (openSansRegularFont == null || openSansBoldFont == null) {
				openSansRegularFont = PdfFontFactory.createRegisteredFont("helvetica", PdfEncodings.WINANSI, true);
				openSansBoldFont = PdfFontFactory.createRegisteredFont("helvetica-bold", PdfEncodings.WINANSI, true);
			}

			pdf.addEventHandler(PdfDocumentEvent.END_PAGE,
					new AvalancheBulletinEventHandler(lang, bulletins, grayscale));
			Document document = new Document(pdf);
			document.setRenderer(new DocumentRenderer(document));
			document.setMargins(110, 30, 60, 50);

			createPdfFrontPage(bulletins, lang, document, pdf, region, grayscale, daytimeDependency);

			for (AvalancheBulletin avalancheBulletin : bulletins) {
				createPdfBulletinPage(avalancheBulletin, lang, document, pdf,
						AlbinaUtil.getTendencyDate(bulletins, lang), writer, grayscale,
						AlbinaUtil.getPublicationTime(bulletins));
			}

			document.close();

			AlbinaUtil.setFilePermissions(filename);
			return true;
		} catch (com.itextpdf.io.IOException | IOException e) {
			logger.error("PDF could not be created: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Create PDFs for each province (TN, BZ, TI) containing an overview map and the
	 * detailed information about each aggregated region touching the province.
	 * 
	 * @param bulletins
	 *            The bulletins to create the region PDFs of.
	 * @param region
	 *            The region to create the PDFs for.
	 * @param publicationTimeString
	 *            the time of publication
	 * @param validityDateString
	 *            the start of the validity of the report
	 */
	public boolean createRegionPdfs(List<AvalancheBulletin> bulletins, String region, String validityDateString,
			String publicationTimeString) {
		boolean daytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);
		boolean result = true;

		ArrayList<AvalancheBulletin> regionBulletins = new ArrayList<AvalancheBulletin>();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			if (avalancheBulletin.affectsRegionOnlyPublished(region))
				regionBulletins.add(avalancheBulletin);
		}

		if (!regionBulletins.isEmpty())
			for (LanguageCode lang : GlobalVariables.languages) {
				if (!createPdf(regionBulletins, lang, region, false, daytimeDependency, validityDateString,
						publicationTimeString))
					result = false;
				if (!createPdf(regionBulletins, lang, region, true, daytimeDependency, validityDateString,
						publicationTimeString))
					result = false;
			}

		return result;
	}

	private void createPdfBulletinPage(AvalancheBulletin avalancheBulletin, LanguageCode lang, Document document,
			PdfDocument pdf, String tendencyDate, PdfWriter writer, boolean grayscale, String publicationTime)
			throws IOException {
		document.add(new AreaBreak());

		float leadingHeadline = 1.f;
		float leadingText = 1.2f;
		float paddingLeft = 10.f;
		float regionMapSize = 100;

		float[] columnWidths = { 1 };
		Table table = new Table(columnWidths).setBorder(null);
		table.setWidthPercent(100);
		Cell cell;

		Paragraph dangerRatingHeadline = new Paragraph(
				GlobalVariables.getDangerRatingTextMiddle(avalancheBulletin.getHighestDangerRating(), lang))
						.setFont(openSansBoldFont).setFontSize(14)
						.setFontColor(getDangerRatingTextColor(avalancheBulletin.getHighestDangerRating(), grayscale))
						.setMultipliedLeading(leadingHeadline);
		cell = new Cell(1, 10).add(dangerRatingHeadline);
		cell.setBackgroundColor(getDangerRatingBackgroundColor(avalancheBulletin.getHighestDangerRating(), grayscale));
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setPaddingLeft(paddingLeft);
		cell.setBorder(Border.NO_BORDER);
		cell.setBorderLeft(
				new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
		table.addCell(cell);

		if (avalancheBulletin.isHasDaytimeDependency()) {
			float[] secondColumnWidths = { 1, 1, 1 };
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			Paragraph am = new Paragraph("AM:").setFont(openSansBoldFont).setFontSize(14).setFontColor(blackColor);
			cell = new Cell(1, 1).add(am);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addCell(cell);
			ImageData regionAMImageDate;
			if (grayscale)
				regionAMImageDate = ImageDataFactory
						.create(GlobalVariables.getMapsPath() + "/" + avalancheBulletin.getValidityDateString() + "/"
								+ publicationTime + "/" + avalancheBulletin.getId() + "_bw.jpg");
			else
				regionAMImageDate = ImageDataFactory
						.create(GlobalVariables.getMapsPath() + "/" + avalancheBulletin.getValidityDateString() + "/"
								+ publicationTime + "/" + avalancheBulletin.getId() + ".jpg");
			Image regionAMImg = new Image(regionAMImageDate);
			regionAMImg.scaleToFit(regionMapSize, regionMapSize);
			regionAMImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionAMImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, false, lang, tendencyDate, pdf, document, writer, grayscale));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 10);
			cell.add(secondTable);
			cell.setVerticalAlignment(VerticalAlignment.TOP);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setPaddingRight(5);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(
					new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
			table.addCell(cell);

			secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			secondTable.setBorderTop(new SolidBorder(blackColor, 1));
			Paragraph pm = new Paragraph("PM:").setFont(openSansBoldFont).setFontSize(14).setFontColor(blackColor);
			cell = new Cell(1, 1).add(pm);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addCell(cell);
			ImageData regionPMImageDate;
			if (grayscale)
				regionPMImageDate = ImageDataFactory
						.create(GlobalVariables.getMapsPath() + "/" + avalancheBulletin.getValidityDateString() + "/"
								+ publicationTime + "/" + avalancheBulletin.getId() + "_PM_bw.jpg");
			else
				regionPMImageDate = ImageDataFactory
						.create(GlobalVariables.getMapsPath() + "/" + avalancheBulletin.getValidityDateString() + "/"
								+ publicationTime + "/" + avalancheBulletin.getId() + "_PM.jpg");
			Image regionPMImg = new Image(regionPMImageDate);
			regionPMImg.scaleToFit(regionMapSize, regionMapSize);
			regionPMImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionPMImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, true, lang, tendencyDate, pdf, document, writer, grayscale));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 10);
			cell.add(secondTable);
			cell.setVerticalAlignment(VerticalAlignment.TOP);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setPaddingRight(5);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(
					new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
			table.addCell(cell);
		} else {
			float[] secondColumnWidths = { 1, 1 };
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			ImageData regionImageDate;
			if (grayscale)
				regionImageDate = ImageDataFactory
						.create(GlobalVariables.getMapsPath() + "/" + avalancheBulletin.getValidityDateString() + "/"
								+ publicationTime + "/" + avalancheBulletin.getId() + "_bw.jpg");
			else
				regionImageDate = ImageDataFactory
						.create(GlobalVariables.getMapsPath() + "/" + avalancheBulletin.getValidityDateString() + "/"
								+ publicationTime + "/" + avalancheBulletin.getId() + ".jpg");
			Image regionImg = new Image(regionImageDate);
			regionImg.scaleToFit(regionMapSize, regionMapSize);
			regionImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, false, lang, tendencyDate, pdf, document, writer, grayscale));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 10);
			cell.add(secondTable);
			cell.setVerticalAlignment(VerticalAlignment.TOP);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setPaddingRight(5);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(
					new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
			table.addCell(cell);
		}

		// avalanche activity
		if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null) {
			Paragraph avActivityHighlights = new Paragraph(
					replaceLinebreaks(avalancheBulletin.getAvActivityHighlightsIn(lang))).setFont(openSansRegularFont)
							.setFontSize(14).setFontColor(blackColor).setMultipliedLeading(leadingHeadline);
			cell = new Cell(1, 10).add(avActivityHighlights);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(
					new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
			table.addCell(cell);
		}

		if (avalancheBulletin.getAvActivityCommentIn(lang) != null) {
			Paragraph avActivityComment = new Paragraph(
					replaceLinebreaks(avalancheBulletin.getAvActivityCommentIn(lang))).setFont(openSansRegularFont)
							.setFontSize(10).setFontColor(blackColor).setMultipliedLeading(leadingText)
							.setMarginBottom(5);
			cell = new Cell(1, 10).add(avActivityComment);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(
					new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
			table.addCell(cell);
		}

		// snowpack structure and tendency
		if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null
				|| avalancheBulletin.getSnowpackStructureCommentIn(lang) != null
				|| avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null
				|| avalancheBulletin.getTendencyCommentIn(lang) != null) {

			if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null
					|| avalancheBulletin.getSnowpackStructureCommentIn(lang) != null
					|| avalancheBulletin.getSnowpackStructureHighlightsIn(lang) != null) {
				cell = new Cell(1, 10).add(new Paragraph(GlobalVariables.getSnowpackHeadline(lang))
						.setFont(openSansRegularFont).setFontSize(14).setFontColor(blackColor).setMarginTop(5)
						.setMultipliedLeading(leadingHeadline));
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				if (grayscale) {
					cell.setBorderLeft(new SolidBorder(blueColorBw, 4));
					cell.setBackgroundColor(greyVeryVeryLightColorBw);
				} else {
					cell.setBorderLeft(new SolidBorder(blueColor, 4));
					cell.setBackgroundColor(greyVeryVeryLightColor);
				}
				table.addCell(cell);

				// add danger patterns
				if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null) {
					float[] dangerPatternColumnWidths = { 1, 1, 1 };
					Table dangerPatternTable = new Table(dangerPatternColumnWidths);
					Paragraph dangerPatternHeadline = new Paragraph(GlobalVariables.getDangerPatternsHeadline(lang))
							.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
					cell = new Cell(1, 1).add(dangerPatternHeadline);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setBorder(Border.NO_BORDER);
					dangerPatternTable.addCell(cell);

					if (avalancheBulletin.getDangerPattern1() != null) {
						Paragraph paragraph = new Paragraph(
								AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern1(), lang))
										.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor);
						;
						cell = new RoundedCornersCell(1, 1).add(paragraph);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setBorder(Border.NO_BORDER);
						// cell.setBackgroundColor(whiteColor);
						dangerPatternTable.addCell(cell);
					}
					if (avalancheBulletin.getDangerPattern2() != null) {
						Paragraph paragraph = new Paragraph(
								AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern2(), lang))
										.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor);
						;
						cell = new RoundedCornersCell(1, 1).add(paragraph);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setBorder(Border.NO_BORDER);
						// cell.setBackgroundColor(whiteColor);
						dangerPatternTable.addCell(cell);
					}

					cell = new Cell(1, 10).add(dangerPatternTable);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setPaddingLeft(paddingLeft);
					cell.setBorder(Border.NO_BORDER);
					if (grayscale) {
						cell.setBorderLeft(new SolidBorder(blueColorBw, 4));
						cell.setBackgroundColor(greyVeryVeryLightColorBw);
					} else {
						cell.setBorderLeft(new SolidBorder(blueColor, 4));
						cell.setBackgroundColor(greyVeryVeryLightColor);
					}
					table.addCell(cell);
				}

				if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null) {
					Paragraph snowpackStructureComment = new Paragraph(
							replaceLinebreaks(avalancheBulletin.getSnowpackStructureCommentIn(lang)))
									.setFont(openSansRegularFont).setFontSize(10).setFontColor(blackColor)
									.setMultipliedLeading(leadingText);
					cell = new Cell(1, 10).add(snowpackStructureComment);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setPaddingLeft(paddingLeft);
					cell.setBorder(Border.NO_BORDER);
					if (grayscale) {
						cell.setBorderLeft(new SolidBorder(blueColorBw, 4));
						cell.setBackgroundColor(greyVeryVeryLightColorBw);
					} else {
						cell.setBorderLeft(new SolidBorder(blueColor, 4));
						cell.setBackgroundColor(greyVeryVeryLightColor);
					}
					table.addCell(cell);
				}
			}

			if (avalancheBulletin.getTendencyCommentIn(lang) != null) {
				Paragraph tendencyHeadline = new Paragraph(GlobalVariables.getTendencyHeadline(lang))
						.setFont(openSansRegularFont).setFontSize(14).setFontColor(blackColor).setMarginTop(10)
						.setMultipliedLeading(leadingHeadline);
				cell = new Cell(1, 10).add(tendencyHeadline);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				if (grayscale) {
					cell.setBorderLeft(new SolidBorder(blueColorBw, 4));
					cell.setBackgroundColor(greyVeryVeryLightColorBw);
				} else {
					cell.setBorderLeft(new SolidBorder(blueColor, 4));
					cell.setBackgroundColor(greyVeryVeryLightColor);
				}
				table.addCell(cell);

				Paragraph tendencyComment = new Paragraph(
						replaceLinebreaks(avalancheBulletin.getTendencyCommentIn(lang))).setFont(openSansRegularFont)
								.setFontSize(10).setFontColor(blackColor).setMultipliedLeading(leadingText)
								.setMarginBottom(5);
				cell = new Cell(1, 10).add(tendencyComment);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				if (grayscale) {
					cell.setBorderLeft(new SolidBorder(blueColorBw, 4));
					cell.setBackgroundColor(greyVeryVeryLightColorBw);
				} else {
					cell.setBorderLeft(new SolidBorder(blueColor, 4));
					cell.setBackgroundColor(greyVeryVeryLightColor);
				}
				table.addCell(cell);
			}
		}

		document.add(table).setLeftMargin(50);
	}

	private Table createSymbols(AvalancheBulletin avalancheBulletin, boolean isAfternoon, LanguageCode lang,
			String tendencyDate, PdfDocument pdf, Document document, PdfWriter writer, boolean grayscale)
			throws MalformedURLException {
		AvalancheBulletinDaytimeDescription daytimeBulletin;
		int height = 30;

		float[] columnWidths = { 1 };
		Table table = new Table(columnWidths).setBorder(Border.NO_BORDER);

		if (isAfternoon)
			daytimeBulletin = avalancheBulletin.getAfternoon();
		else
			daytimeBulletin = avalancheBulletin.getForenoon();

		float[] firstRowColumnWidths = { 1, 1, 1, 1 };
		Table firstRowTable = new Table(firstRowColumnWidths);

		Paragraph firstRow = new Paragraph("").setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);

		Image regionImg;
		if (grayscale)
			regionImg = getImage("warning_pictos/grey/level_"
					+ AlbinaUtil.getWarningLevelId(daytimeBulletin, avalancheBulletin.isHasElevationDependency())
					+ ".png");
		else
			regionImg = getImage("warning_pictos/color/level_"
					+ AlbinaUtil.getWarningLevelId(daytimeBulletin, avalancheBulletin.isHasElevationDependency())
					+ ".png");
		if (regionImg != null) {
			regionImg.scaleToFit(70, 30);
			firstRow.add(regionImg);
		}

		Cell cell = new Cell(1, 1).add(firstRow);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setBorder(Border.NO_BORDER);
		cell.setHeight(height);
		firstRowTable.addCell(cell);

		cell = new Cell(1, 1);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setBorder(Border.NO_BORDER);
		cell.setHeight(height);
		cell.setPaddingRight(10);
		if (avalancheBulletin.isHasElevationDependency()) {
			if (!(isAfternoon && avalancheBulletin.getAfternoon().getDangerRatingAbove()
					.equals(avalancheBulletin.getAfternoon().getDangerRatingBelow()))
					&& !(!isAfternoon && avalancheBulletin.getForenoon().getDangerRatingAbove()
							.equals(avalancheBulletin.getForenoon().getDangerRatingBelow()))) {
				if (avalancheBulletin.getTreeline()) {
					Paragraph paragraph = new Paragraph(GlobalVariables.getTreelineString(lang))
							.setFontColor(blackColor).setFontSize(8).setFont(openSansBoldFont);
					paragraph.setRelativePosition(-2, 2, 0, 0);
					cell.add(paragraph);
				} else if (avalancheBulletin.getElevation() > 0) {
					Paragraph paragraph = new Paragraph(avalancheBulletin.getElevation() + "m").setFontColor(blackColor)
							.setFontSize(8).setFont(openSansBoldFont);
					paragraph.setRelativePosition(-2, 2, 0, 0);
					cell.add(paragraph);
				}
			}
		}
		firstRowTable.addCell(cell);

		if (avalancheBulletin.getTendency() != null) {
			cell = new Cell(1, 1);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.setHeight(height);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(blackColor, 0.5f));

			Paragraph paragraph = new Paragraph().setFontSize(8).setFontColor(blackColor).setMarginLeft(10)
					.setMultipliedLeading(1.0f);
			paragraph.add(new Text(GlobalVariables.getTendencyText(avalancheBulletin.getTendency(), lang))
					.setFont(openSansBoldFont));
			paragraph.add(new Text("\n"));
			paragraph.add(new Text(tendencyDate).setFont(openSansRegularFont));
			cell.add(paragraph);
			firstRowTable.addCell(cell);

			cell = new Cell(1, 1);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.setHeight(height);
			cell.setBorder(Border.NO_BORDER);
			Image tendencyImg = getImage(
					GlobalVariables.getTendencySymbolPath(avalancheBulletin.getTendency(), grayscale));
			if (tendencyImg != null) {
				tendencyImg.scaleToFit(25, 20);
				tendencyImg.setMarginLeft(5);
				cell.add(tendencyImg);
			}

			firstRowTable.addCell(cell);
		}

		cell = new Cell(1, 1).add(firstRowTable);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);

		cell = new Cell(1, 1).add(createAvalancheSituations(daytimeBulletin, lang, pdf, document, writer, isAfternoon,
				avalancheBulletin.isHasDaytimeDependency(), grayscale));
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);

		return table;
	}

	private Table createAvalancheSituations(AvalancheBulletinDaytimeDescription daytimeBulletin, LanguageCode lang,
			PdfDocument pdf, Document document, PdfWriter writer, boolean isAfternoon, boolean hasDaytime,
			boolean grayscale) throws MalformedURLException {
		float[] columnWidths = { 1, 1, 1, 1, 1, 1, 1, 1 };
		Table table = new Table(columnWidths);

		if (daytimeBulletin.getAvalancheSituation1() != null
				&& daytimeBulletin.getAvalancheSituation1().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation1(), lang, table, false, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}
		if (daytimeBulletin.getAvalancheSituation2() != null
				&& daytimeBulletin.getAvalancheSituation2().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation2(), lang, table, true, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}

		return table;
	}

	public Image getImage(String path) {
		try {
			ImageData imageData = ImageDataFactory.create(GlobalVariables.getServerImagesUrlLocalhost() + path);
			return new Image(imageData);
		} catch (IOException e) {
			logger.warn("Image could not be loaded: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private void createAvalancheSituation(AvalancheSituation avalancheSituation, LanguageCode lang, Table table,
			boolean isSecond, Document document, PdfWriter writer, boolean isAfternoon, boolean hasDaytime,
			boolean grayscale) throws MalformedURLException {
		float[] avalancheSituationColumnWidths = { 1 };
		Table avalancheSituationTable;
		Paragraph paragraph;
		Image img;
		Cell cell;
		int padding = 0;

		if (avalancheSituation != null) {
			if (avalancheSituation.getAvalancheSituation() != null) {
				avalancheSituationTable = new Table(avalancheSituationColumnWidths).setBorder(Border.NO_BORDER);
				avalancheSituationTable.setMarginLeft(0);
				avalancheSituationTable.setMarginTop(5);
				avalancheSituationTable.setWidth(60);
				avalancheSituationTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
				img = getImage(GlobalVariables.getAvalancheSituationSymbolPath(avalancheSituation, grayscale));
				if (img != null) {
					img.scaleToFit(60, 35);
					if (isSecond)
						img.setMarginLeft(5);
					cell = new Cell(1, 1).add(img);
					cell.setBorder(Border.NO_BORDER);
					cell.setWidth(60);
					avalancheSituationTable.addCell(cell);
				}
				if (isSecond)
					avalancheSituationTable.setBorderLeft(new SolidBorder(blackColor, 0.5f));
				paragraph = new Paragraph(avalancheSituation.getAvalancheSituation().toString(lang))
						.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor)
						.setMultipliedLeading(1.0f);
				if (isSecond)
					paragraph.setMarginLeft(5);
				cell = new Cell(1, 1).add(paragraph);
				cell.setBorder(Border.NO_BORDER);
				avalancheSituationTable.addCell(cell);

				cell = new Cell(1, 1);
				cell.setBorder(Border.NO_BORDER);
				cell.setPadding(padding);
				cell.add(avalancheSituationTable);
				table.addCell(cell);
			}

			if (avalancheSituation.getAspects() != null && avalancheSituation.getAspects().size() > 0) {
				Set<Aspect> aspects = avalancheSituation.getAspects();

				int result = 0b00000000;
				Iterator<Aspect> iterator = aspects.iterator();
				while (iterator.hasNext()) {
					switch (iterator.next()) {
					case N:
						result = result | 0b10000000;
						break;
					case NE:
						result = result | 0b01000000;
						break;
					case E:
						result = result | 0b00100000;
						break;
					case SE:
						result = result | 0b00010000;
						break;
					case S:
						result = result | 0b00001000;
						break;
					case SW:
						result = result | 0b00000100;
						break;
					case W:
						result = result | 0b00000010;
						break;
					case NW:
						result = result | 0b00000001;
						break;

					default:
						break;
					}
				}

				img = getImage(GlobalVariables.getAspectSymbolPath(result, grayscale));
				if (img != null) {
					img.scaleToFit(30, 30);
					cell = new Cell(1, 1).add(img);
					cell.setBorder(Border.NO_BORDER);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					table.addCell(cell);
				}
			}

			float[] elevationColumnWidths = { 1 };
			Table elevationTable = new Table(elevationColumnWidths);

			if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
				if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
					// elevation high and low set
					if (grayscale)
						img = getImage("elevation/grey/levels_middle_two.png");
					else
						img = getImage("elevation/color/levels_middle_two.png");
					if (img != null) {
						img.scaleToFit(70, 25);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(img);
						table.addCell(cell);
					}
					if (avalancheSituation.getTreelineHigh()) {
						Paragraph paragraph2 = new Paragraph(GlobalVariables.getTreelineString(lang))
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
						paragraph2.setRelativePosition(-6, 2, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					} else if (avalancheSituation.getElevationHigh() > 0) {
						Paragraph paragraph2 = new Paragraph(avalancheSituation.getElevationHigh() + "m")
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
						paragraph2.setRelativePosition(-6, 2, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					}
					if (avalancheSituation.getTreelineLow()) {
						Paragraph paragraph2 = new Paragraph(GlobalVariables.getTreelineString(lang))
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
						paragraph2.setRelativePosition(-6, -3, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					} else if (avalancheSituation.getElevationLow() > 0) {
						Paragraph paragraph2 = new Paragraph(avalancheSituation.getElevationLow() + "m")
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
						paragraph2.setRelativePosition(-6, -3, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					}
				} else {
					// elevation high set
					if (grayscale)
						img = getImage("elevation/grey/levels_below.png");
					else
						img = getImage("elevation/color/levels_below.png");
					if (img != null) {
						img.scaleToFit(70, 25);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(img);
						table.addCell(cell);
					}

					if (avalancheSituation.getTreelineHigh()) {
						paragraph = new Paragraph(GlobalVariables.getTreelineString(lang)).setFont(openSansBoldFont)
								.setFontSize(8).setFontColor(blackColor);
						paragraph.setRelativePosition(-6, -4, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.add(paragraph);
						elevationTable.addCell(cell);
					} else if (avalancheSituation.getElevationHigh() > 0) {
						paragraph = new Paragraph(avalancheSituation.getElevationHigh() + "m").setFont(openSansBoldFont)
								.setFontSize(8).setFontColor(blackColor);
						paragraph.setRelativePosition(-6, -4, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.add(paragraph);
						elevationTable.addCell(cell);
					}
				}
			} else if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
				// elevation low set
				if (grayscale)
					img = getImage("elevation/grey/levels_above.png");
				else
					img = getImage("elevation/color/levels_above.png");
				if (img != null) {
					img.scaleToFit(70, 25);
					img.setMarginLeft(5);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.setPadding(padding);
					cell.add(img);
					table.addCell(cell);
				}

				if (avalancheSituation.getTreelineLow()) {
					paragraph = new Paragraph(GlobalVariables.getTreelineString(lang)).setFont(openSansBoldFont)
							.setFontSize(8).setFontColor(blackColor);
					paragraph.setRelativePosition(-6, 4, 0, 0);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.add(paragraph);
					elevationTable.addCell(cell);
				} else if (avalancheSituation.getElevationLow() > 0) {
					paragraph = new Paragraph(avalancheSituation.getElevationLow() + "m").setFont(openSansBoldFont)
							.setFontSize(8).setFontColor(blackColor);
					paragraph.setRelativePosition(-6, 4, 0, 0);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.add(paragraph);
					elevationTable.addCell(cell);
				}
			} else {
				// no elevation set
				if (grayscale)
					img = getImage("elevation/grey/levels_all.png");
				else
					img = getImage("elevation/color/levels_all.png");
				if (img != null) {
					img.scaleToFit(70, 25);
					img.setMarginLeft(5);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.setPadding(padding);
					cell.add(img);
					table.addCell(cell);
				}
			}

			cell = new Cell(1, 1);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.setBorder(Border.NO_BORDER);
			cell.add(elevationTable);
			cell.setPadding(padding);
			table.addCell(cell);
		}
	}

	private Color getDangerRatingColor(DangerRating dangerRating, boolean grayscale) {
		if (grayscale) {
			switch (dangerRating) {
			case low:
				return dangerLevel1ColorBw;
			case moderate:
				return dangerLevel2ColorBw;
			case considerable:
				return dangerLevel3ColorBw;
			case high:
				return dangerLevel4ColorBw;
			case very_high:
				return dangerLevel5ColorRedBw;
			default:
				return whiteColor;
			}
		} else {
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
	}

	private Color getDangerRatingBackgroundColor(DangerRating dangerRating, boolean grayscale) {
		if (grayscale) {
			switch (dangerRating) {
			case low:
				return dangerLevel1ColorBw;
			case moderate:
				return dangerLevel2ColorBw;
			case considerable:
				return whiteColor;
			case high:
				return whiteColor;
			case very_high:
				return whiteColor;
			default:
				return whiteColor;
			}
		} else {
			switch (dangerRating) {
			case low:
				return dangerLevel1Color;
			case moderate:
				return dangerLevel2Color;
			case considerable:
				return whiteColor;
			case high:
				return whiteColor;
			case very_high:
				return whiteColor;
			default:
				return whiteColor;
			}
		}
	}

	private Color getDangerRatingTextColor(DangerRating dangerRating, boolean grayscale) {
		if (grayscale) {
			switch (dangerRating) {
			case low:
				return blackColor;
			case moderate:
				return blackColor;
			case considerable:
				return dangerLevel3ColorBw;
			case high:
				return dangerLevel4ColorBw;
			case very_high:
				return dangerLevel5ColorRedBw;
			default:
				return blackColor;
			}
		} else {
			switch (dangerRating) {
			case low:
				return blackColor;
			case moderate:
				return blackColor;
			case considerable:
				return dangerLevel3Color;
			case high:
				return dangerLevel4Color;
			case very_high:
				return dangerLevel5ColorRed;
			default:
				return blackColor;
			}
		}
	}

	private void createPdfFrontPage(List<AvalancheBulletin> bulletins, LanguageCode lang, Document document,
			PdfDocument pdf, String region, boolean grayscale, boolean daytimeDependency) throws MalformedURLException {
		PdfPage page = pdf.addNewPage();
		Rectangle pageSize = page.getPageSize();
		PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
		Canvas canvas = new Canvas(pdfCanvas, pdf, page.getPageSize());

		int mapY;
		int mapWidth;
		int mapHeight;

		// Add overview maps
		if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
			if (region != null) {
				mapY = 130;
				mapWidth = 400;
				mapHeight = mapWidth / 3 * 2;
			} else {
				mapY = 130;
				mapWidth = 270;
				mapHeight = mapWidth;
			}

			ImageData overviewMapAMImageData = ImageDataFactory.create(GlobalVariables.getMapsPath() + "/"
					+ AlbinaUtil.getValidityDateString(bulletins) + "/" + AlbinaUtil.getPublicationTime(bulletins) + "/"
					+ MapUtil.getOverviewMapFilename(region, false, true, grayscale));
			Image overviewMapAMImg = new Image(overviewMapAMImageData);
			overviewMapAMImg.scaleToFit(mapWidth, 500);
			overviewMapAMImg.setFixedPosition(pageSize.getWidth() / 2 - mapWidth / 2, mapY + mapHeight + 40);
			canvas.add(overviewMapAMImg);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14)
					.moveText(pageSize.getWidth() / 2 - 240, mapY + mapHeight * 2 + 50).setColor(blackColor, true)
					.showText(GlobalVariables.getAMText(lang)).endText();

			ImageData overviewMapPMImageData = ImageDataFactory.create(GlobalVariables.getMapsPath() + "/"
					+ AlbinaUtil.getValidityDateString(bulletins) + "/" + AlbinaUtil.getPublicationTime(bulletins) + "/"
					+ MapUtil.getOverviewMapFilename(region, true, true, grayscale));
			Image overviewMapPMImg = new Image(overviewMapPMImageData);
			overviewMapPMImg.scaleToFit(mapWidth, 500);
			overviewMapPMImg.setFixedPosition(pageSize.getWidth() / 2 - mapWidth / 2, mapY);
			canvas.add(overviewMapPMImg);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14)
					.moveText(pageSize.getWidth() / 2 - 240, mapY + mapHeight + 10).setColor(blackColor, true)
					.showText(GlobalVariables.getPMText(lang)).endText();
		} else {
			ImageData overviewMapImageData = ImageDataFactory.create(GlobalVariables.getMapsPath() + "/"
					+ AlbinaUtil.getValidityDateString(bulletins) + "/" + AlbinaUtil.getPublicationTime(bulletins) + "/"
					+ MapUtil.getOverviewMapFilename(region, false, daytimeDependency, grayscale));
			Image overviewMapImg = new Image(overviewMapImageData);
			if (region != null) {
				mapY = 290;
				overviewMapImg.scaleToFit(500, 500);
				overviewMapImg.setFixedPosition(pageSize.getWidth() / 2 - 250, mapY);
			} else {
				mapY = 250;
				overviewMapImg.scaleToFit(420, 500);
				overviewMapImg.setFixedPosition(pageSize.getWidth() / 2 - 210, mapY);
			}
			canvas.add(overviewMapImg);
		}

		int legendY = mapY - 40;

		// add legend
		int legendEntryWidth = 50;
		int legendEntryHeight = 8;

		if (grayscale) {
			Rectangle dangerLevel1Rectangle = new Rectangle(
					pageSize.getWidth() / 2 - 2 * legendEntryWidth - legendEntryWidth / 2, legendY, legendEntryWidth,
					legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel1Rectangle).setColor(dangerLevel1ColorBw, true).fill();
			Rectangle dangerLevel2Rectangle = new Rectangle(
					pageSize.getWidth() / 2 - legendEntryWidth - legendEntryWidth / 2, legendY, legendEntryWidth,
					legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel2Rectangle).setColor(dangerLevel2ColorBw, true).fill();
			Rectangle dangerLevel3Rectangle = new Rectangle(pageSize.getWidth() / 2 - legendEntryWidth / 2, legendY,
					legendEntryWidth, legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel3Rectangle).setColor(dangerLevel3ColorBw, true).fill();
			Rectangle dangerLevel4Rectangle = new Rectangle(pageSize.getWidth() / 2 + legendEntryWidth / 2, legendY,
					legendEntryWidth, legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel4Rectangle).setColor(dangerLevel4ColorBw, true).fill();

			for (int j = 0; j < 2; j++) {
				for (int i = 0; i < 12; i++) {
					Rectangle dangerLevel5Rectangle = new Rectangle(
							pageSize.getWidth() / 2 + legendEntryWidth + legendEntryWidth / 2 + i * 4, legendY + j * 4,
							4, 4);
					if ((i + j) % 2 == 0)
						pdfCanvas.rectangle(dangerLevel5Rectangle).setColor(dangerLevel5ColorRedBw, true).fill();
					else
						pdfCanvas.rectangle(dangerLevel5Rectangle).setColor(dangerLevel5ColorBlackBw, true).fill();
				}
			}
		} else {
			Rectangle dangerLevel1Rectangle = new Rectangle(
					pageSize.getWidth() / 2 - 2 * legendEntryWidth - legendEntryWidth / 2, legendY, legendEntryWidth,
					legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel1Rectangle).setColor(dangerLevel1Color, true).fill();
			Rectangle dangerLevel2Rectangle = new Rectangle(
					pageSize.getWidth() / 2 - legendEntryWidth - legendEntryWidth / 2, legendY, legendEntryWidth,
					legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel2Rectangle).setColor(dangerLevel2Color, true).fill();
			Rectangle dangerLevel3Rectangle = new Rectangle(pageSize.getWidth() / 2 - legendEntryWidth / 2, legendY,
					legendEntryWidth, legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel3Rectangle).setColor(dangerLevel3Color, true).fill();
			Rectangle dangerLevel4Rectangle = new Rectangle(pageSize.getWidth() / 2 + legendEntryWidth / 2, legendY,
					legendEntryWidth, legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel4Rectangle).setColor(dangerLevel4Color, true).fill();

			for (int j = 0; j < 2; j++) {
				for (int i = 0; i < 12; i++) {
					Rectangle dangerLevel5Rectangle = new Rectangle(
							pageSize.getWidth() / 2 + legendEntryWidth + legendEntryWidth / 2 + i * 4, legendY + j * 4,
							4, 4);
					if ((i + j) % 2 == 0)
						pdfCanvas.rectangle(dangerLevel5Rectangle).setColor(dangerLevel5ColorRed, true).fill();
					else
						pdfCanvas.rectangle(dangerLevel5Rectangle).setColor(dangerLevel5ColorBlack, true).fill();
				}
			}
		}

		float width;
		float fontSize = 8;
		int y = legendY - 10;
		width = openSansBoldFont.getContentWidth(new PdfString("1")) * 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
				.moveText(pageSize.getWidth() / 2 - 2 * legendEntryWidth - width, y).setColor(blackColor, true)
				.showText("1").endText();
		width = openSansBoldFont.getContentWidth(new PdfString("2")) * 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
				.moveText(pageSize.getWidth() / 2 - legendEntryWidth - width, y).setColor(blackColor, true)
				.showText("2").endText();
		width = openSansBoldFont.getContentWidth(new PdfString("3")) * 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize).moveText(pageSize.getWidth() / 2 - width, y)
				.setColor(blackColor, true).showText("3").endText();
		width = openSansBoldFont.getContentWidth(new PdfString("4")) * 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
				.moveText(pageSize.getWidth() / 2 + legendEntryWidth - width, y).setColor(blackColor, true)
				.showText("4").endText();
		width = openSansBoldFont.getContentWidth(new PdfString("5")) * 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
				.moveText(pageSize.getWidth() / 2 + 2 * legendEntryWidth - width, y).setColor(blackColor, true)
				.showText("5").endText();

		y = y - 9;
		width = openSansRegularFont
				.getContentWidth(new PdfString(GlobalVariables.getDangerRatingTextShort(DangerRating.low, lang)))
				* 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 - 2 * legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(GlobalVariables.getDangerRatingTextShort(DangerRating.low, lang)).endText();
		width = openSansRegularFont
				.getContentWidth(new PdfString(GlobalVariables.getDangerRatingTextShort(DangerRating.moderate, lang)))
				* 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 - legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(GlobalVariables.getDangerRatingTextShort(DangerRating.moderate, lang)).endText();
		width = openSansRegularFont.getContentWidth(
				new PdfString(GlobalVariables.getDangerRatingTextShort(DangerRating.considerable, lang))) * 0.001f
				* fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize).moveText(pageSize.getWidth() / 2 - width, y)
				.setColor(blackColor, true)
				.showText(GlobalVariables.getDangerRatingTextShort(DangerRating.considerable, lang)).endText();
		width = openSansRegularFont
				.getContentWidth(new PdfString(GlobalVariables.getDangerRatingTextShort(DangerRating.high, lang)))
				* 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 + legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(GlobalVariables.getDangerRatingTextShort(DangerRating.high, lang)).endText();
		width = openSansRegularFont
				.getContentWidth(new PdfString(GlobalVariables.getDangerRatingTextShort(DangerRating.very_high, lang)))
				* 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 + 2 * legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(GlobalVariables.getDangerRatingTextShort(DangerRating.very_high, lang)).endText();

		/*
		 * // Add avalanche danger scale float marginRight = 25.f; float marginLeft =
		 * 15.f; int tableFontSize = 7; int dangerRatingFontSize = 12;
		 * 
		 * float[] columnWidths = { 1, 1 }; Table table = new
		 * Table(columnWidths).setAutoLayout().setBorder(Border.NO_BORDER).setMarginTop(
		 * 290)
		 * .setMarginLeft(marginLeft).setMarginRight(marginRight).setWidthPercent(100);
		 * 
		 * Paragraph symbolHeadline = new
		 * Paragraph(GlobalVariables.getDangerRatingHeadline(lang))
		 * .setFont(openSansBoldFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); Cell cell = new Cell(1, 1).add(symbolHeadline);
		 * cell.setPaddingLeft(5); cell.setPaddingRight(5);
		 * cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * Paragraph characteristicsHeadline = new
		 * Paragraph(GlobalVariables.getCharacteristicsHeadline(lang))
		 * .setFont(openSansBoldFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell = new Cell(1, 1).add(characteristicsHeadline);
		 * cell.setPaddingLeft(5); cell.setPaddingRight(5);
		 * cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * Paragraph dangerRatingText = new
		 * Paragraph("5").setFont(openSansBoldFont).setFontSize(dangerRatingFontSize)
		 * .setFontColor(greyDarkColor); cell = new Cell(1, 1).add(dangerRatingText);
		 * dangerRatingText = new
		 * Paragraph(GlobalVariables.getDangerRatingTextShort(DangerRating.very_high,
		 * lang)) .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(dangerRatingText);
		 * cell.setTextAlignment(TextAlignment.CENTER);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * Paragraph characteristicsText = new Paragraph(
		 * GlobalVariables.getDangerRatingVeryHighCharacteristicsTextBold(lang)).setFont
		 * (openSansBoldFont) .setFontSize(tableFontSize).setFontColor(greyDarkColor);
		 * cell = new Cell(1, 1).add(characteristicsText); characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingVeryHighCharacteristicsText(lang))
		 * .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(characteristicsText); cell.setPaddingLeft(5);
		 * cell.setPaddingRight(5); cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * dangerRatingText = new
		 * Paragraph("4").setFont(openSansBoldFont).setFontSize(dangerRatingFontSize)
		 * .setFontColor(greyDarkColor); cell = new Cell(1, 1).add(dangerRatingText);
		 * dangerRatingText = new
		 * Paragraph(GlobalVariables.getDangerRatingTextShort(DangerRating.high, lang))
		 * .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(dangerRatingText);
		 * cell.setTextAlignment(TextAlignment.CENTER);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingHighCharacteristicsTextBold(lang))
		 * .setFont(openSansBoldFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell = new Cell(1, 1).add(characteristicsText);
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingHighCharacteristicsText(lang))
		 * .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(characteristicsText); cell.setPaddingLeft(5);
		 * cell.setPaddingRight(5); cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * dangerRatingText = new
		 * Paragraph("3").setFont(openSansBoldFont).setFontSize(dangerRatingFontSize)
		 * .setFontColor(greyDarkColor); cell = new Cell(1, 1).add(dangerRatingText);
		 * dangerRatingText = new
		 * Paragraph(GlobalVariables.getDangerRatingTextShort(DangerRating.considerable,
		 * lang)) .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(dangerRatingText);
		 * cell.setTextAlignment(TextAlignment.CENTER);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingConsiderableCharacteristicsTextBold(
		 * lang)) .setFont(openSansBoldFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell = new Cell(1, 1).add(characteristicsText);
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingConsiderableCharacteristicsText(lang
		 * )) .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(characteristicsText); cell.setPaddingLeft(5);
		 * cell.setPaddingRight(5); cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * dangerRatingText = new
		 * Paragraph("2").setFont(openSansBoldFont).setFontSize(dangerRatingFontSize)
		 * .setFontColor(greyDarkColor); cell = new Cell(1, 1).add(dangerRatingText);
		 * dangerRatingText = new
		 * Paragraph(GlobalVariables.getDangerRatingTextShort(DangerRating.moderate,
		 * lang)) .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(dangerRatingText);
		 * cell.setTextAlignment(TextAlignment.CENTER);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingModerateCharacteristicsTextBold(lang
		 * )) .setFont(openSansBoldFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell = new Cell(1, 1).add(characteristicsText);
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingModerateCharacteristicsText(lang))
		 * .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(characteristicsText); cell.setPaddingLeft(5);
		 * cell.setPaddingRight(5); cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * dangerRatingText = new
		 * Paragraph("1").setFont(openSansBoldFont).setFontSize(dangerRatingFontSize)
		 * .setFontColor(greyDarkColor); cell = new Cell(1, 1).add(dangerRatingText);
		 * dangerRatingText = new
		 * Paragraph(GlobalVariables.getDangerRatingTextShort(DangerRating.low, lang))
		 * .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(dangerRatingText);
		 * cell.setTextAlignment(TextAlignment.CENTER);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingLowCharacteristicsTextBold(lang))
		 * .setFont(openSansBoldFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell = new Cell(1, 1).add(characteristicsText);
		 * characteristicsText = new
		 * Paragraph(GlobalVariables.getDangerRatingLowCharacteristicsText(lang))
		 * .setFont(openSansRegularFont).setFontSize(tableFontSize).setFontColor(
		 * greyDarkColor); cell.add(characteristicsText); cell.setPaddingLeft(5);
		 * cell.setPaddingRight(5); cell.setTextAlignment(TextAlignment.LEFT);
		 * cell.setVerticalAlignment(VerticalAlignment.MIDDLE); cell.setBorder(new
		 * SolidBorder(greyDarkColor, 0.5f)); table.addCell(cell);
		 * 
		 * document.add(table);
		 */
		canvas.close();
		pdfCanvas.release();
	}

	private String replaceLinebreaks(String text) {
		return text.replaceAll("[ ]*<br\\/>[ ]*", "\n");
	}

	private String replaceLinks(String text) {
		// TODO implement
		return null;
	}
}

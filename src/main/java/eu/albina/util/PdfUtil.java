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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.io.Resources;

import eu.albina.controller.ServerInstanceController;
import eu.albina.map.MapUtil;
import eu.albina.model.ServerInstance;
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
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
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
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;

public class PdfUtil {

	private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

	private static PdfUtil instance = null;

	public static final Color blackColor = Color.BLACK;
	public static final Color greyDarkColor = new DeviceRgb(85, 95, 96);
	public static final Color whiteColor = Color.WHITE;
	public static final Color greyVeryVeryLightColor = new DeviceRgb(242, 247, 250);
	public static final Color redColor = Color.RED;

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

	protected PdfUtil() {
	}

	public static PdfUtil getInstance() {
		if (instance == null) {
			instance = new PdfUtil();
		}
		return instance;
	}

	public void createPdf(List<AvalancheBulletin> bulletins, LanguageCode lang, Region region, ServerInstance serverInstance, boolean grayscale,
			boolean daytimeDependency, String validityDateString, String publicationTimeString, boolean preview) throws IOException {
		String pdfPath;
		String mapsPath;
		if (preview) {
			pdfPath = GlobalVariables.getTmpPdfDirectory();
			mapsPath = GlobalVariables.getTmpMapsPath();
		} else {
			pdfPath = serverInstance.getPdfDirectory();
			mapsPath = serverInstance.getMapsPath();
		}
		String filename = getFilename(lang, region, grayscale, validityDateString, publicationTimeString, pdfPath);

		try (PdfWriter writer = new PdfWriter(filename, new WriterProperties().addXmpMetadata());
			 PdfDocument pdf = new PdfDocument(writer);
			 Document document = new Document(pdf)) {
			pdf.setTagged();
			pdf.getCatalog().setLang(new PdfString(lang.toString()));
			pdf.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
			PdfDocumentInfo info = pdf.getDocumentInfo();
			info.setTitle(lang.getBundleString("avalanche-report.name"));

			openSansRegularFont = createFont("fonts/open-sans/OpenSans-Regular.ttf");
			openSansBoldFont = createFont("fonts/open-sans/OpenSans-Bold.ttf");

			pdf.addEventHandler(PdfDocumentEvent.END_PAGE,
					new AvalancheBulletinEventHandler(lang, region, bulletins, grayscale, preview));
			document.setRenderer(new DocumentRenderer(document));
			document.setMargins(110, 30, 60, 50);

			createPdfFrontPage(bulletins, lang, document, pdf, region, grayscale, daytimeDependency, validityDateString,
					publicationTimeString, mapsPath);

			for (AvalancheBulletin avalancheBulletin : bulletins) {
				createPdfBulletinPage(avalancheBulletin, region, lang, document, pdf,
						AlbinaUtil.getTendencyDate(bulletins, lang), writer, grayscale, validityDateString,
						publicationTimeString, mapsPath);
			}

			AlbinaUtil.setFilePermissions(filename);
		} catch (com.itextpdf.io.IOException e) {
			throw new IOException(e);
		}
	}

	private String getFilename(LanguageCode lang, Region region, boolean grayscale, String validityDateString, String publicationTimeString, String pdfPath) {
		return pdfPath + System.getProperty("file.separator")
					+ validityDateString + System.getProperty("file.separator") + publicationTimeString
					+ System.getProperty("file.separator") + validityDateString + "_" + region.getId() + "_"
					+ lang.toString() + (grayscale ? "_bw" : "") + ".pdf";
	}

	public static PdfFont createFont(String resource) throws IOException {
		final byte[] ttf = Resources.toByteArray(Resources.getResource(resource));
		return PdfFontFactory.createFont(ttf, PdfEncodings.WINANSI, true);
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
	public void createRegionPdfs(List<AvalancheBulletin> bulletins, Region region, String validityDateString, String publicationTimeString) {
		boolean daytimeDependency = AlbinaUtil.hasDaytimeDependency(bulletins);

		ArrayList<AvalancheBulletin> regionBulletins = new ArrayList<AvalancheBulletin>();
		for (AvalancheBulletin avalancheBulletin : bulletins) {
			if (avalancheBulletin.affectsRegionOnlyPublished(region))
				regionBulletins.add(avalancheBulletin);
		}
		if (regionBulletins.isEmpty()) {
			return;
		}

		for (LanguageCode lang : LanguageCode.ENABLED) {
			try {
				createPdf(regionBulletins, lang, region, ServerInstanceController.getInstance().getLocalServerInstance(), false, daytimeDependency, validityDateString, publicationTimeString, false);
				createPdf(regionBulletins, lang, region, ServerInstanceController.getInstance().getLocalServerInstance(), true, daytimeDependency, validityDateString, publicationTimeString, false);
			} catch (IOException e) {
				logger.error("PDF could not be created", e);
			}
		}
	}

	protected Color getColor(String hex) {
		int[] rgb = GlobalVariables.getRGB(hex);
		return new DeviceRgb(rgb[0], rgb[1], rgb[2]);
	}

	private void createPdfBulletinPage(AvalancheBulletin avalancheBulletin, Region region, LanguageCode lang, Document document,
			PdfDocument pdf, String tendencyDate, PdfWriter writer, boolean grayscale, String validityDateString,
			String publicationTimeString, String mapsPath) throws IOException {
		document.add(new AreaBreak());

		final Color blue = grayscale ? blueColorBw : getColor(region.getPdfColor());
		final Color greyVeryVeryLight = grayscale ? greyVeryVeryLightColorBw : greyVeryVeryLightColor;

		float leadingHeadline = 1.f;
		float leadingText = 1.2f;
		float paddingLeft = 10.f;
		float regionMapSize = 100;

		float[] columnWidths = { 1 };
		Table table = new Table(columnWidths).setBorder(null);
		table.setWidthPercent(100);
		Cell cell;

		Paragraph dangerRatingHeadline = new Paragraph(
				avalancheBulletin.getHighestDangerRating().toString(lang.getLocale(), true)).setFont(openSansBoldFont)
						.setFontSize(14)
						.setFontColor(getDangerRatingTextColor(avalancheBulletin.getHighestDangerRating(), grayscale))
						.setMultipliedLeading(leadingHeadline);
		cell = new Cell(1, 1).add(dangerRatingHeadline);
		cell.setBackgroundColor(getDangerRatingBackgroundColor(avalancheBulletin.getHighestDangerRating(), grayscale));
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setPaddingLeft(paddingLeft);
		cell.setBorder(Border.NO_BORDER);
		cell.setBorderLeft(
				new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
		table.addCell(cell);

		float[] secondColumnWidths;
		if (avalancheBulletin.isHasDaytimeDependency()) {
			secondColumnWidths = new float[]{1, 1, 1};
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			Paragraph am = new Paragraph("AM:").setFont(openSansBoldFont).setFontSize(14).setFontColor(blackColor);
			cell = new Cell(1, 1).add(am);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addCell(cell);
			ImageData regionAMImageDate;
			if (grayscale)
				regionAMImageDate = ImageDataFactory.create(mapsPath
						+ System.getProperty("file.separator") + avalancheBulletin.getValidityDateString()
						+ System.getProperty("file.separator") + publicationTimeString
						+ System.getProperty("file.separator") + avalancheBulletin.getId() + "_bw.jpg");
			else
				regionAMImageDate = ImageDataFactory.create(mapsPath
						+ System.getProperty("file.separator") + avalancheBulletin.getValidityDateString()
						+ System.getProperty("file.separator") + publicationTimeString
						+ System.getProperty("file.separator") + avalancheBulletin.getId() + ".jpg");
			Image regionAMImg = new Image(regionAMImageDate);
			regionAMImg.getAccessibilityProperties().setAlternateDescription(String.join(", ", avalancheBulletin.getPublishedRegions()));
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
				regionPMImageDate = ImageDataFactory.create(mapsPath
						+ System.getProperty("file.separator") + avalancheBulletin.getValidityDateString()
						+ System.getProperty("file.separator") + publicationTimeString
						+ System.getProperty("file.separator") + avalancheBulletin.getId() + "_PM_bw.jpg");
			else
				regionPMImageDate = ImageDataFactory.create(mapsPath
						+ System.getProperty("file.separator") + avalancheBulletin.getValidityDateString()
						+ System.getProperty("file.separator") + publicationTimeString
						+ System.getProperty("file.separator") + avalancheBulletin.getId() + "_PM.jpg");
			Image regionPMImg = new Image(regionPMImageDate);
			regionPMImg.getAccessibilityProperties().setAlternateDescription(String.join(", ", avalancheBulletin.getPublishedRegions()));
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
			cell = new Cell(1, 1);
			cell.add(secondTable);
		} else {
			secondColumnWidths = new float[]{1, 1};
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			ImageData regionImageDate;
			if (grayscale)
				regionImageDate = ImageDataFactory
						.create(mapsPath + System.getProperty("file.separator")
								+ validityDateString + System.getProperty("file.separator") + publicationTimeString
								+ System.getProperty("file.separator") + avalancheBulletin.getId() + "_bw.jpg");
			else
				regionImageDate = ImageDataFactory
						.create(mapsPath + System.getProperty("file.separator")
								+ validityDateString + System.getProperty("file.separator") + publicationTimeString
								+ System.getProperty("file.separator") + avalancheBulletin.getId() + ".jpg");
			Image regionImg = new Image(regionImageDate);
			regionImg.getAccessibilityProperties().setAlternateDescription(String.join(", ", avalancheBulletin.getPublishedRegions()));
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
			cell = new Cell(1, 1);
			cell.add(secondTable);
		}
		cell.setVerticalAlignment(VerticalAlignment.TOP);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setPaddingLeft(paddingLeft);
		cell.setPaddingRight(5);
		cell.setBorder(Border.NO_BORDER);
		cell.setBorderLeft(
				new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating(), grayscale), 4));
		table.addCell(cell);

		// avalanche activity
		if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null) {
			Paragraph avActivityHighlights = new Paragraph(
					replaceLinebreaks(avalancheBulletin.getAvActivityHighlightsIn(lang))).setFont(openSansRegularFont)
							.setFontSize(14).setFontColor(blackColor).setMultipliedLeading(leadingHeadline);
			cell = new Cell(1, 1).add(avActivityHighlights);
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
			cell = new Cell(1, 1).add(avActivityComment);
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
				cell = new Cell(1, 10).add(new Paragraph(lang.getBundleString("headline.snowpack"))
						.setFont(openSansRegularFont).setFontSize(14).setFontColor(blackColor).setMarginTop(5)
						.setMultipliedLeading(leadingHeadline));
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				cell.setBorderLeft(new SolidBorder(blue, 4));
				cell.setBackgroundColor(greyVeryVeryLight);
				table.addCell(cell);

				// add danger patterns
				if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null) {
					float[] dangerPatternColumnWidths = { 1, 1, 1 };
					Table dangerPatternTable = new Table(dangerPatternColumnWidths);
					Paragraph dangerPatternHeadline = new Paragraph(lang.getBundleString("headline.danger-patterns"))
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
					} else {
						cell = new Cell(1, 1).setBorder(null);
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
					} else {
						cell = new Cell(1, 1).setBorder(null);
						dangerPatternTable.addCell(cell);
					}

					cell = new Cell(1, 1).add(dangerPatternTable);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setPaddingLeft(paddingLeft);
					cell.setBorder(Border.NO_BORDER);
					cell.setBorderLeft(new SolidBorder(blue, 4));
					cell.setBackgroundColor(greyVeryVeryLight);
					table.addCell(cell);
				}

				if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null) {
					Paragraph snowpackStructureComment = new Paragraph(
							replaceLinebreaks(avalancheBulletin.getSnowpackStructureCommentIn(lang)))
									.setFont(openSansRegularFont).setFontSize(10).setFontColor(blackColor)
									.setMultipliedLeading(leadingText);
					cell = new Cell(1, 1).add(snowpackStructureComment);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setPaddingLeft(paddingLeft);
					cell.setBorder(Border.NO_BORDER);
					cell.setBorderLeft(new SolidBorder(blue, 4));
					cell.setBackgroundColor(greyVeryVeryLight);
					table.addCell(cell);
				}
			}

			if (avalancheBulletin.getTendencyCommentIn(lang) != null) {
				Paragraph tendencyHeadline = new Paragraph(lang.getBundleString("headline.tendency"))
						.setFont(openSansRegularFont).setFontSize(14).setFontColor(blackColor).setMarginTop(10)
						.setMultipliedLeading(leadingHeadline);
				cell = new Cell(1, 1).add(tendencyHeadline);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				cell.setBorderLeft(new SolidBorder(blue, 4));
				cell.setBackgroundColor(greyVeryVeryLight);
				table.addCell(cell);

				Paragraph tendencyComment = new Paragraph(
						replaceLinebreaks(avalancheBulletin.getTendencyCommentIn(lang))).setFont(openSansRegularFont)
								.setFontSize(10).setFontColor(blackColor).setMultipliedLeading(leadingText)
								.setMarginBottom(5);
				cell = new Cell(1, 1).add(tendencyComment);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				cell.setBorderLeft(new SolidBorder(blue, 4));
				cell.setBackgroundColor(greyVeryVeryLight);
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
			regionImg = getImage("images/warning_pictos/grey/level_" + AlbinaUtil.getWarningLevelId(daytimeBulletin) + ".png");
		else
			regionImg = getImage("images/warning_pictos/color/level_" + AlbinaUtil.getWarningLevelId(daytimeBulletin) + ".png");
		if (regionImg != null) {
			regionImg.getAccessibilityProperties().setAlternateDescription(AlbinaUtil.getDangerRatingText(daytimeBulletin, lang));
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
		if (daytimeBulletin.isHasElevationDependency()) {
			if (!(isAfternoon && avalancheBulletin.getAfternoon().getDangerRatingAbove()
					.equals(avalancheBulletin.getAfternoon().getDangerRatingBelow()))
					&& !(!isAfternoon && avalancheBulletin.getForenoon().getDangerRatingAbove()
							.equals(avalancheBulletin.getForenoon().getDangerRatingBelow()))) {
				if (daytimeBulletin.getTreeline()) {
					Paragraph paragraph = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
							.setFontColor(blackColor).setFontSize(8).setFont(openSansBoldFont);
					paragraph.setRelativePosition(-2, 2, 0, 0);
					cell.add(paragraph);
				} else if (daytimeBulletin.getElevation() > 0) {
					Paragraph paragraph = new Paragraph(daytimeBulletin.getElevation() + "m").setFontColor(blackColor)
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
			paragraph.add(
					new Text(avalancheBulletin.getTendency().toString(lang.getLocale())).setFont(openSansBoldFont));
			paragraph.add(new Text("\n"));
			paragraph.add(new Text(tendencyDate).setFont(openSansRegularFont));
			cell.add(paragraph);
			firstRowTable.addCell(cell);

			cell = new Cell(1, 1);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.setHeight(height);
			cell.setBorder(Border.NO_BORDER);
			Image tendencyImg = getImage(avalancheBulletin.getTendency().getSymbolPath(grayscale));
			if (tendencyImg != null) {
				tendencyImg.getAccessibilityProperties().setAlternateDescription(avalancheBulletin.getTendency().toString(lang.getLocale()));
				tendencyImg.scaleToFit(25, 20);
				tendencyImg.setMarginLeft(5);
				cell.add(tendencyImg);
			}

			firstRowTable.addCell(cell);
		} else {
			cell = new Cell().setBorder(null);
			firstRowTable.addCell(cell);
			cell = new Cell().setBorder(null);
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
		float[] columnWidths = { 1, 1, 1, 1 };
		Table table = new Table(columnWidths);

		if (daytimeBulletin.getAvalancheSituation1() != null
				&& daytimeBulletin.getAvalancheSituation1().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation1(), lang, table, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}
		if (daytimeBulletin.getAvalancheSituation2() != null
				&& daytimeBulletin.getAvalancheSituation2().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation2(), lang, table, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}
		if (daytimeBulletin.getAvalancheSituation3() != null
				&& daytimeBulletin.getAvalancheSituation3().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation3(), lang, table, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}
		if (daytimeBulletin.getAvalancheSituation4() != null
				&& daytimeBulletin.getAvalancheSituation4().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation4(), lang, table, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}
		if (daytimeBulletin.getAvalancheSituation5() != null
				&& daytimeBulletin.getAvalancheSituation5().getAvalancheSituation() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation5(), lang, table, document, writer,
					isAfternoon, hasDaytime, grayscale);
		}

		return table;
	}

	public Image getImage(String resourceName) {
		URL resource = Resources.getResource(resourceName);
		ImageData imageData = ImageDataFactory.create(resource);
		return new Image(imageData);
	}

	private void createAvalancheSituation(AvalancheSituation avalancheSituation, LanguageCode lang, Table table,
			Document document, PdfWriter writer, boolean isAfternoon, boolean hasDaytime,
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
				img = getImage("images/" + avalancheSituation.getAvalancheSituation().getSymbolPath(grayscale));
				if (img != null) {
					img.getAccessibilityProperties().setAlternateDescription(avalancheSituation.getAvalancheSituation().toString(lang.getLocale()));
					img.scaleToFit(60, 35);
					cell = new Cell(1, 1).add(img);
					cell.setBorder(Border.NO_BORDER);
					cell.setWidth(60);
					avalancheSituationTable.addCell(cell);
				}
				paragraph = new Paragraph(avalancheSituation.getAvalancheSituation().toString(lang.getLocale()))
						.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor)
						.setMultipliedLeading(1.0f);
				cell = new Cell(1, 1).add(paragraph);
				cell.setBorder(Border.NO_BORDER);
				avalancheSituationTable.addCell(cell);

				cell = new Cell(1, 1);
				cell.setBorder(Border.NO_BORDER);
				cell.setPadding(padding);
				cell.add(avalancheSituationTable);
				table.addCell(cell);
			} else {
				cell = new Cell().setBorder(null);
				table.addCell(cell);
			}

			if (avalancheSituation.getAspects() != null && avalancheSituation.getAspects().size() > 0) {
				Set<Aspect> aspects = avalancheSituation.getAspects();
				img = getImage("images/" + Aspect.getSymbolPath(aspects, grayscale));
				if (img != null) {
					img.getAccessibilityProperties().setAlternateDescription(AlbinaUtil.getAspectString(avalancheSituation.getAspects(), lang.getLocale()));
					img.scaleToFit(30, 30);
					cell = new Cell(1, 1).add(img);
					cell.setBorder(Border.NO_BORDER);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					table.addCell(cell);
				} else {
					cell = new Cell().setBorder(null);
					table.addCell(cell);
				}
			} else {
				cell = new Cell().setBorder(null);
				table.addCell(cell);
			}

			float[] elevationColumnWidths = { 1 };
			Table elevationTable = new Table(elevationColumnWidths);

			if (avalancheSituation.getTreelineHigh() || avalancheSituation.getElevationHigh() > 0) {
				if (avalancheSituation.getTreelineLow() || avalancheSituation.getElevationLow() > 0) {
					// elevation high and low set
					if (grayscale)
						img = getImage("images/elevation/grey/levels_middle_two.png");
					else
						img = getImage("images/elevation/color/levels_middle_two.png");
					if (img != null) {
						img.getAccessibilityProperties().setAlternateDescription(AlbinaUtil.getElevationString(avalancheSituation, lang));
						img.scaleToFit(70, 25);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(img);
						table.addCell(cell);
					} else {
						cell = new Cell().setBorder(null);
						table.addCell(cell);
					}
					if (avalancheSituation.getTreelineHigh()) {
						Paragraph paragraph2 = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
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
						Paragraph paragraph2 = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
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
						img = getImage("images/elevation/grey/levels_below.png");
					else
						img = getImage("images/elevation/color/levels_below.png");
					if (img != null) {
						img.getAccessibilityProperties().setAlternateDescription(AlbinaUtil.getElevationString(avalancheSituation, lang));
						img.scaleToFit(70, 25);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(img);
						table.addCell(cell);
					} else {
						cell = new Cell().setBorder(null);
						table.addCell(cell);
					}

					if (avalancheSituation.getTreelineHigh()) {
						paragraph = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
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
					img = getImage("images/elevation/grey/levels_above.png");
				else
					img = getImage("images/elevation/color/levels_above.png");
				if (img != null) {
					img.getAccessibilityProperties().setAlternateDescription(AlbinaUtil.getElevationString(avalancheSituation, lang));
					img.scaleToFit(70, 25);
					img.setMarginLeft(5);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.setPadding(padding);
					cell.add(img);
					table.addCell(cell);
				} else {
					cell = new Cell().setBorder(null);
					table.addCell(cell);
				}

				if (avalancheSituation.getTreelineLow()) {
					paragraph = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
							.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
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
					img = getImage("images/elevation/grey/levels_all.png");
				else
					img = getImage("images/elevation/color/levels_all.png");
				if (img != null) {
					img.getAccessibilityProperties().setAlternateDescription(AlbinaUtil.getElevationString(avalancheSituation, lang));
					img.scaleToFit(70, 25);
					img.setMarginLeft(5);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.setPadding(padding);
					cell.add(img);
					table.addCell(cell);
				} else {
					cell = new Cell().setBorder(null);
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
			PdfDocument pdf, Region region, boolean grayscale, boolean daytimeDependency, String validityDateString,
			String publicationTimeString, String mapsPath) throws MalformedURLException {
		PdfPage page = pdf.addNewPage();
		Rectangle pageSize = page.getPageSize();
		PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
		Canvas canvas = new Canvas(pdfCanvas, pdf, page.getPageSize());

		int mapY;
		int mapWidth;
		int mapHeight;

		// Add overview maps
		if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
			mapY = region.getPdfMapYAmPm();
			mapWidth = region.getPdfMapWidthAmPm();
			mapHeight = region.getPdfMapHeight();

			ImageData overviewMapAMImageData = ImageDataFactory.create(mapsPath
					+ System.getProperty("file.separator") + validityDateString + System.getProperty("file.separator")
					+ publicationTimeString + System.getProperty("file.separator")
					+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.am, grayscale));
			Image overviewMapAMImg = new Image(overviewMapAMImageData);
			overviewMapAMImg.getAccessibilityProperties().setAlternateDescription(lang.getBundleString("headline"));
			overviewMapAMImg.scaleToFit(mapWidth, 500);
			overviewMapAMImg.setFixedPosition(pageSize.getWidth() / 2 - mapWidth / 2, mapY + mapHeight + 40);
			canvas.add(overviewMapAMImg);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14)
					.moveText(pageSize.getWidth() / 2 - 240, mapY + mapHeight * 2 + 50).setColor(blackColor, true)
					.showText(lang.getBundleString("daytime.am.capitalized")).endText();

			ImageData overviewMapPMImageData = ImageDataFactory.create(mapsPath
					+ System.getProperty("file.separator") + validityDateString + System.getProperty("file.separator")
					+ publicationTimeString + System.getProperty("file.separator")
					+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.pm, grayscale));
			Image overviewMapPMImg = new Image(overviewMapPMImageData);
			overviewMapAMImg.getAccessibilityProperties().setAlternateDescription(lang.getBundleString("headline"));
			overviewMapPMImg.scaleToFit(mapWidth, 500);
			overviewMapPMImg.setFixedPosition(pageSize.getWidth() / 2 - mapWidth / 2, mapY);
			canvas.add(overviewMapPMImg);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14)
					.moveText(pageSize.getWidth() / 2 - 240, mapY + mapHeight + 10).setColor(blackColor, true)
					.showText(lang.getBundleString("daytime.pm.capitalized")).endText();
		} else {
			ImageData overviewMapImageData = ImageDataFactory.create(mapsPath
					+ System.getProperty("file.separator") + validityDateString + System.getProperty("file.separator")
					+ publicationTimeString + System.getProperty("file.separator")
					+ MapUtil.getOverviewMapFilename(region, DaytimeDependency.fd, grayscale));
			Image overviewMapImg = new Image(overviewMapImageData);
			overviewMapImg.getAccessibilityProperties().setAlternateDescription(lang.getBundleString("headline"));
			mapY = region.getPdfMapYFd();
			overviewMapImg.scaleToFit(region.getPdfMapWidthFd(), 500);
			overviewMapImg.setFixedPosition(pageSize.getWidth() / 2 - region.getPdfMapWidthFd() / 2, mapY);
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
		width = openSansRegularFont.getContentWidth(new PdfString(DangerRating.low.toString(lang.getLocale(), false)))
				* 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 - 2 * legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(DangerRating.low.toString(lang.getLocale(), false)).endText();
		width = openSansRegularFont
				.getContentWidth(new PdfString(DangerRating.moderate.toString(lang.getLocale(), false))) * 0.001f
				* fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 - legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(DangerRating.moderate.toString(lang.getLocale(), false)).endText();
		width = openSansRegularFont
				.getContentWidth(new PdfString(DangerRating.considerable.toString(lang.getLocale(), false))) * 0.001f
				* fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize).moveText(pageSize.getWidth() / 2 - width, y)
				.setColor(blackColor, true).showText(DangerRating.considerable.toString(lang.getLocale(), false))
				.endText();
		width = openSansRegularFont.getContentWidth(new PdfString(DangerRating.high.toString(lang.getLocale(), false)))
				* 0.001f * fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 + legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(DangerRating.high.toString(lang.getLocale(), false)).endText();
		width = openSansRegularFont
				.getContentWidth(new PdfString(DangerRating.very_high.toString(lang.getLocale(), false))) * 0.001f
				* fontSize / 2;
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
				.moveText(pageSize.getWidth() / 2 + 2 * legendEntryWidth - width, y).setColor(blackColor, true)
				.showText(DangerRating.very_high.toString(lang.getLocale(), false)).endText();

		canvas.close();
		pdfCanvas.release();
	}

	private String replaceLinebreaks(String text) {
		return text.replaceAll("[ ]*<br\\/>[ ]*", "\n");
	}
}

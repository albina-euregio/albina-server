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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.WebColors;
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
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.renderer.DocumentRenderer;

import eu.albina.map.MapImageFormat;
import eu.albina.map.MapUtil;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;

public class PdfUtil {

	private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

	public static final String MEDIA_TYPE = "application/pdf";

	public static final Color blackColor = ColorConstants.BLACK;
	public static final Color greyDarkColor = new DeviceRgb(85, 95, 96);
	public static final Color whiteColor = ColorConstants.WHITE;
	public static final Color greyVeryVeryLightColor = new DeviceRgb(242, 247, 250);
	public static final Color redColor = ColorConstants.RED;

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

	private PdfFont openSansRegularFont;
	private PdfFont openSansBoldFont;
	private PdfFont openSansLightFont;
	protected final AvalancheReport avalancheReport;
	private final LanguageCode lang;
	private final boolean grayscale;

	public PdfUtil(AvalancheReport avalancheReport, LanguageCode lang, boolean grayscale) {
		this.avalancheReport = avalancheReport;
		this.lang = lang;
		this.grayscale = grayscale;
	}

	public Path createPdf() throws IOException {
		Path path = getPath();
		Files.createDirectories(path.getParent());
		logger.info("Creating PDF {}", path);

		try (PdfWriter writer = new PdfWriter(path.toString(), new WriterProperties().addXmpMetadata());
			 PdfDocument pdf = new PdfDocument(writer);
			 Document document = new Document(pdf)) {
			pdf.setTagged();
			pdf.getCatalog().setLang(new PdfString(lang.toString()));
			pdf.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
			PdfDocumentInfo info = pdf.getDocumentInfo();
			info.setTitle(lang.getBundleString("website.name"));

			openSansRegularFont = createFont("fonts/open-sans/OpenSans-Regular.ttf");
			openSansBoldFont = createFont("fonts/open-sans/OpenSans-Bold.ttf");
			openSansLightFont = createFont("fonts/open-sans/OpenSans-Light.ttf");

			pdf.addEventHandler(PdfDocumentEvent.END_PAGE, event -> addHeaderFooter((PdfDocumentEvent) event));
			document.setRenderer(new DocumentRenderer(document));
			document.setMargins(110, 30, 60, 50);

			createPdfFrontPage(pdf);

			for (AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
				createPdfBulletinPage(bulletin, document);
			}

			return path;
		}
	}

	private void addHeaderFooter(PdfDocumentEvent docEvent) {
		PdfDocument pdfDoc = docEvent.getDocument();
		PdfPage page = docEvent.getPage();
		Rectangle pageSize = page.getPageSize();
		PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

		Region region = avalancheReport.getRegion();
		Color blue = grayscale ? blueColorBw : PdfUtil.getColor(region.getPdfColor());

		// Add headline
		String headline = lang.getBundleString("website.name", region);
		pdfCanvas.beginText().setFontAndSize(openSansLightFont, 14).moveText(20, pageSize.getTop() - 40)
			.setColor(greyDarkColor, true).showText(headline).endText();
		String date = avalancheReport.getDate(lang);
		if (BulletinStatus.isDraftOrUpdated(avalancheReport.getStatus())) {
			String preview = lang.getBundleString("preview");
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 16).moveText(20, pageSize.getTop() - 60)
				.setColor(redColor, true).showText(date + preview).endText();
		} else {
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 16).moveText(20, pageSize.getTop() - 60)
				.setColor(blue, true).showText(date).endText();
		}

		String publicationDate = avalancheReport.getPublicationDate(lang);
		if (!publicationDate.isEmpty()) {
			if (avalancheReport.isUpdate())
				pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, pageSize.getTop() - 75)
					.setColor(greyDarkColor, true).showText(lang.getBundleString("updated") + publicationDate)
					.endText();
			else
				pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, pageSize.getTop() - 75)
					.setColor(greyDarkColor, true).showText(lang.getBundleString("published") + publicationDate)
					.endText();
		}

		Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());

		// Add copyright
		String copyright = "";
		pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, 20).setColor(blue, true)
			.showText(copyright).endText();

		String urlString = lang.getBundleString("website.url.capitalized", region);
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
			ciImg = PdfUtil.this.getImage(region.getImageColorbarBwPath());
		else
			ciImg = PdfUtil.this.getImage(region.getImageColorbarColorPath());
		ciImg.scaleAbsolute(pageSize.getWidth(), 4);
		ciImg.setFixedPosition(0, pageSize.getHeight() - 4);
		canvas.add(ciImg);

		// Add logo
		Image logoImg;
		if (grayscale)
			logoImg = PdfUtil.this.getImage(lang.getBundleString("logo.path.bw", region));
		else
			logoImg = PdfUtil.this.getImage(lang.getBundleString("logo.path", region));
		logoImg.scaleToFit(130, 55);
		logoImg.setFixedPosition(pageSize.getWidth() - 110, pageSize.getHeight() - 75);
		canvas.add(logoImg);

		// Add secondary logo
		if (region.isPdfFooterLogo()) {
			Image footerImg = PdfUtil.this.getImage(grayscale ? region.getPdfFooterLogoBwPath() : region.getPdfFooterLogoColorPath());
			footerImg.scaleToFit(120, 40);
			footerImg.setFixedPosition(15, 5);
			canvas.add(footerImg);
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
	}

	public Path getPath() {
		final String filename = String.format("%s_%s_%s%s.pdf",
			avalancheReport.getValidityDateString(),
			avalancheReport.getRegion().getId(),
			lang.toString(),
			grayscale ? "_bw" : "");
		return avalancheReport.getPdfDirectory().resolve(filename);
	}

	public static PdfFont createFont(String resource) throws IOException {
		final byte[] ttf = Resources.toByteArray(Resources.getResource(resource));
		return PdfFontFactory.createFont(ttf, PdfEncodings.WINANSI);
	}

	/**
	 * Create PDFs for each province (TN, BZ, TI) containing an overview map and the
	 * detailed information about each aggregated region touching the province.
	 */
	public static void createRegionPdfs(AvalancheReport avalancheReport) {
		if (avalancheReport.getBulletins().isEmpty()) {
			return;
		}
		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			try {
				logger.info("Creating PDF for region {}, language {}", avalancheReport.getRegion().getId(), lang);
				new PdfUtil(avalancheReport, lang, false).createPdf();
				new PdfUtil(avalancheReport, lang, true).createPdf();
			} catch (IOException e) {
				logger.error("PDF could not be created", e);
			}
		}
	}

	protected static Color getColor(String hex) {
		return WebColors.getRGBColor(hex);
	}

	private void createPdfBulletinPage(AvalancheBulletin avalancheBulletin, Document document) throws IOException {
		document.add(new AreaBreak());

		final Color blue = grayscale ? blueColorBw : getColor(avalancheReport.getRegion().getPdfColor());
		final Color greyVeryVeryLight = grayscale ? greyVeryVeryLightColorBw : greyVeryVeryLightColor;

		float leadingHeadline = 1.f;
		float leadingText = 1.2f;
		float paddingLeft = 10.f;
		float regionMapSize = 100;

		float[] columnWidths = { 1 };
		Table table = new Table(columnWidths).setBorder(null);
		table.setWidth(UnitValue.createPercentValue(100));
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
			secondColumnWidths = new float[]{1, 1};
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			Paragraph am = new Paragraph(lang.getBundleString("valid-time-period.earlier"))
				.setFont(openSansRegularFont).setFontSize(10).setFontColor(blackColor);
			cell = new Cell(2, 1).add(am);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addHeaderCell(cell);
			ImageData regionAMImageDate = ImageDataFactory.create(getMapImage(DaytimeDependency.am, avalancheBulletin));
			Image regionAMImg = new Image(regionAMImageDate);
			regionAMImg.getAccessibilityProperties().setAlternateDescription(String.join(", ", avalancheBulletin.getPublishedRegions()));
			regionAMImg.scaleToFit(regionMapSize, regionMapSize);
			regionAMImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionAMImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, false));
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
			Paragraph pm = new Paragraph(lang.getBundleString("valid-time-period.later"))
				.setFont(openSansRegularFont).setFontSize(10).setFontColor(blackColor);
			cell = new Cell(2, 1).add(pm);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addHeaderCell(cell);
			ImageData regionPMImageDate = ImageDataFactory.create(getMapImage(DaytimeDependency.pm, avalancheBulletin));
			Image regionPMImg = new Image(regionPMImageDate);
			regionPMImg.getAccessibilityProperties().setAlternateDescription(String.join(", ", avalancheBulletin.getPublishedRegions()));
			regionPMImg.scaleToFit(regionMapSize, regionMapSize);
			regionPMImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionPMImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, true));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 1);
			cell.add(secondTable);
		} else {
			secondColumnWidths = new float[]{1, 1};
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			ImageData regionImageDate = ImageDataFactory.create(getMapImage(DaytimeDependency.fd, avalancheBulletin));
			Image regionImg = new Image(regionImageDate);
			regionImg.getAccessibilityProperties().setAlternateDescription(String.join(", ", avalancheBulletin.getPublishedRegions()));
			regionImg.scaleToFit(regionMapSize, regionMapSize);
			regionImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, false));
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
								avalancheBulletin.getDangerPattern1().toString(lang.getLocale()))
										.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor);
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
								avalancheBulletin.getDangerPattern2().toString(lang.getLocale()))
										.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor);
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

	private Table createSymbols(AvalancheBulletin avalancheBulletin, boolean isAfternoon) {
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
			regionImg = getImage("warning_pictos/grey/level_" + AlbinaUtil.getWarningLevelId(daytimeBulletin) + ".png");
		else
			regionImg = getImage("warning_pictos/color/level_" + AlbinaUtil.getWarningLevelId(daytimeBulletin) + ".png");
		if (regionImg != null) {
			regionImg.getAccessibilityProperties().setAlternateDescription(getDangerRatingText(daytimeBulletin, lang));
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
			if (!(isAfternoon && avalancheBulletin.getAfternoon().dangerRating(true)
					.equals(avalancheBulletin.getAfternoon().dangerRating(false)))
					&& !(!isAfternoon && avalancheBulletin.getForenoon().dangerRating(true)
							.equals(avalancheBulletin.getForenoon().dangerRating(false)))) {
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
			paragraph.add(new Text(avalancheReport.getTendencyDate(lang)).setFont(openSansRegularFont));
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

		cell = new Cell(1, 1).add(createAvalancheProblems(daytimeBulletin));
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);

		return table;
	}

	private Table createAvalancheProblems(AvalancheBulletinDaytimeDescription daytimeBulletin) {

		float[] columnWidths = { 1, 1, 1, 1, 1 };
		Table table = new Table(columnWidths);

		if (daytimeBulletin.getAvalancheProblem1() != null
				&& daytimeBulletin.getAvalancheProblem1().getAvalancheProblem() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheProblem(daytimeBulletin.getAvalancheProblem1(), table
			);
		}
		if (daytimeBulletin.getAvalancheProblem2() != null
				&& daytimeBulletin.getAvalancheProblem2().getAvalancheProblem() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheProblem(daytimeBulletin.getAvalancheProblem2(), table
			);
		}
		if (daytimeBulletin.getAvalancheProblem3() != null
				&& daytimeBulletin.getAvalancheProblem3().getAvalancheProblem() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheProblem(daytimeBulletin.getAvalancheProblem3(), table
			);
		}
		if (daytimeBulletin.getAvalancheProblem4() != null
				&& daytimeBulletin.getAvalancheProblem4().getAvalancheProblem() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheProblem(daytimeBulletin.getAvalancheProblem4(), table
			);
		}
		if (daytimeBulletin.getAvalancheProblem5() != null
				&& daytimeBulletin.getAvalancheProblem5().getAvalancheProblem() != null) {
			table.setBorderTop(new SolidBorder(blackColor, 0.5f));
			createAvalancheProblem(daytimeBulletin.getAvalancheProblem5(), table
			);
		}

		return table;
	}

	private void createAvalancheProblem(AvalancheProblem avalancheProblem, Table table) {
		if (avalancheProblem == null) {
			return;
		}
		table.addCell(getAvalancheProblemCell(avalancheProblem.getAvalancheProblem()));
		table.addCell(getAspectsCell(avalancheProblem.getAspects()));
		table.addCell(getElevationCell(avalancheProblem, table));
		table.addCell(getMatrixInformationCell(avalancheProblem.getAvalancheProblem(), avalancheProblem.getEawsMatrixInformation()));
	}

	private Cell getAvalancheProblemCell(eu.albina.model.enumerations.AvalancheProblem avalancheProblem) {
		if (avalancheProblem == null) {
			return new Cell().setBorder(null);
		}
		float[] avalancheProblemColumnWidths = {1};
		Table avalancheProblemTable = new Table(avalancheProblemColumnWidths).setBorder(Border.NO_BORDER);
		avalancheProblemTable.setMarginLeft(0);
		avalancheProblemTable.setMarginTop(5);
		avalancheProblemTable.setWidth(60);
		avalancheProblemTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
		Image img = getImage(avalancheProblem.getSymbolPath(grayscale));
		img.getAccessibilityProperties().setAlternateDescription(avalancheProblem.toString(lang.getLocale()));
		img.scaleToFit(60, 35);

		Cell cell = new Cell(1, 1).add(img);
		cell.setBorder(Border.NO_BORDER);
		cell.setWidth(60);
		avalancheProblemTable.addCell(cell);
		Paragraph paragraph = new Paragraph(avalancheProblem.toString(lang.getLocale()))
			.setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor)
			.setMultipliedLeading(1.0f);

		cell = new Cell(1, 1).add(paragraph);
		cell.setBorder(Border.NO_BORDER);
		avalancheProblemTable.addCell(cell);

		cell = new Cell(1, 1);
		cell.setBorder(Border.NO_BORDER);
		cell.setPadding(0);
		cell.add(avalancheProblemTable);
		return cell;

	}

	private Cell getAspectsCell(Set<Aspect> aspects) {
		if (aspects == null || aspects.size() <= 0) {
			return new Cell().setBorder(null);
		}
		Image img = getImage(Aspect.getSymbolPath(aspects, grayscale));
		img.getAccessibilityProperties().setAlternateDescription(getAspectString(aspects, lang.getLocale()));
		img.scaleToFit(30, 30);
		Cell cell = new Cell(1, 1).add(img);
		cell.setBorder(Border.NO_BORDER);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		return cell;
	}

	private Cell getElevationCell(AvalancheProblem avalancheProblem, Table table) {
		int padding = 0;
		Paragraph paragraph;
		Cell cell;
		Image img;

		float[] elevationColumnWidths = { 1 };
		Table elevationTable = new Table(elevationColumnWidths);

		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				if (grayscale)
					img = getImage("elevation/grey/levels_middle_two.png");
				else
					img = getImage("elevation/color/levels_middle_two.png");
				img.getAccessibilityProperties().setAlternateDescription(getElevationString(avalancheProblem, lang));
				img.scaleToFit(70, 25);
				cell = new Cell(1, 1);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
				cell.setBorder(Border.NO_BORDER);
				cell.setPadding(padding);
				cell.add(img);
				table.addCell(cell);
				if (avalancheProblem.getTreelineHigh()) {
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
				} else if (avalancheProblem.getElevationHigh() > 0) {
					Paragraph paragraph2 = new Paragraph(avalancheProblem.getElevationHigh() + "m")
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
				if (avalancheProblem.getTreelineLow()) {
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
				} else if (avalancheProblem.getElevationLow() > 0) {
					Paragraph paragraph2 = new Paragraph(avalancheProblem.getElevationLow() + "m")
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
				img.getAccessibilityProperties().setAlternateDescription(getElevationString(avalancheProblem, lang));
				img.scaleToFit(70, 25);
				cell = new Cell(1, 1);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
				cell.setBorder(Border.NO_BORDER);
				cell.setPadding(padding);
				cell.add(img);
				table.addCell(cell);

				if (avalancheProblem.getTreelineHigh()) {
					paragraph = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
						.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
					paragraph.setRelativePosition(-6, -4, 0, 0);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.add(paragraph);
					elevationTable.addCell(cell);
				} else if (avalancheProblem.getElevationHigh() > 0) {
					paragraph = new Paragraph(avalancheProblem.getElevationHigh() + "m").setFont(openSansBoldFont)
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
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			if (grayscale)
				img = getImage("elevation/grey/levels_above.png");
			else
				img = getImage("elevation/color/levels_above.png");
			img.getAccessibilityProperties().setAlternateDescription(getElevationString(avalancheProblem, lang));
			img.scaleToFit(70, 25);
			img.setMarginLeft(5);
			cell = new Cell(1, 1);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.setBorder(Border.NO_BORDER);
			cell.setPadding(padding);
			cell.add(img);
			table.addCell(cell);

			if (avalancheProblem.getTreelineLow()) {
				paragraph = new Paragraph(lang.getBundleString("elevation.treeline.capitalized"))
					.setFont(openSansBoldFont).setFontSize(8).setFontColor(blackColor);
				paragraph.setRelativePosition(-6, 4, 0, 0);
				cell = new Cell(1, 1);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
				cell.setBorder(Border.NO_BORDER);
				cell.add(paragraph);
				elevationTable.addCell(cell);
			} else if (avalancheProblem.getElevationLow() > 0) {
				paragraph = new Paragraph(avalancheProblem.getElevationLow() + "m").setFont(openSansBoldFont)
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
			img.getAccessibilityProperties().setAlternateDescription(getElevationString(avalancheProblem, lang));
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
		cell = new Cell(1, 1);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setBorder(Border.NO_BORDER);
		cell.add(elevationTable);
		cell.setPadding(0);
		return cell;
	}

	private Cell getMatrixInformationCell(eu.albina.model.enumerations.AvalancheProblem avalancheProblem, EawsMatrixInformation matrixInformation) {
		if (matrixInformation == null) {
			return new Cell().setBorder(null);
		}
		Paragraph paragraph = new Paragraph().setFont(openSansRegularFont).setFontSize(8).setFontColor(blackColor);
		if (avalancheProblem != eu.albina.model.enumerations.AvalancheProblem.gliding_snow && matrixInformation.getSnowpackStability() != null) {
			paragraph.add(new Text(lang.getBundleString("problem.snowpack-stability") + ": "));
			paragraph.add(new Text(lang.getBundleString("problem.snowpack-stability." + matrixInformation.getSnowpackStability()) + "\n")
				.setFontColor(getColor(avalancheReport.getRegion().getPdfColor())));
		}
		if (matrixInformation.getFrequency() != null) {
			paragraph.add(new Text(lang.getBundleString("problem.frequency") + ": "));
			paragraph.add(new Text(lang.getBundleString("problem.frequency." + matrixInformation.getFrequency()) + "\n")
				.setFontColor(getColor(avalancheReport.getRegion().getPdfColor())));
		}
		if (matrixInformation.getAvalancheSize() != null) {
			paragraph.add(new Text(lang.getBundleString("problem.avalanche-size") + ": "));
			paragraph.add(new Text(lang.getBundleString("problem.avalanche-size." + matrixInformation.getAvalancheSize()) + "\n")
				.setFontColor(getColor(avalancheReport.getRegion().getPdfColor())));
		}
		Cell cell = new Cell(1, 1);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setBorder(Border.NO_BORDER);
		cell.setPadding(0);
		cell.add(paragraph);
		return cell;
	}

	private final Map<String, Image> imageMap = new HashMap<>();

	public Image getImage(String name) {
		return imageMap.computeIfAbsent(name, resourceName -> {
			URL resource = Resources.getResource("images/" + resourceName);
			ImageData imageData = ImageDataFactory.create(resource);
			return new Image(imageData);
		});
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

	private void createPdfFrontPage(PdfDocument pdf) throws MalformedURLException {
		PdfPage page = pdf.addNewPage();
		Rectangle pageSize = page.getPageSize();
		PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
		Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());

		int mapY;
		int mapWidth;
		int mapHeight;
		Region region = avalancheReport.getRegion();

		// Add overview maps
		if (avalancheReport.hasDaytimeDependency()) {
			mapY = region.getPdfMapYAmPm();
			mapWidth = region.getPdfMapWidthAmPm();
			mapHeight = region.getPdfMapHeight();

			String generalHeadlineComment = avalancheReport.getGeneralHeadline(lang);
			if (generalHeadlineComment != null && !generalHeadlineComment.isEmpty()) {
				Text title = new Text(generalHeadlineComment)
					.setFont(openSansBoldFont)
					.setFontSize(14)
					.setFontColor(blackColor);
				Paragraph p = new Paragraph().add(title)
					.setMultipliedLeading(1.1f)
					.setFixedPosition(pageSize.getWidth() / 2 - 260, mapY + 2 * mapHeight * 0.88f + 80, mapWidth);
				canvas.add(p);
			}

			ImageData overviewMapAMImageData = ImageDataFactory.create(getMapImage(DaytimeDependency.am));
			Image overviewMapAMImg = new Image(overviewMapAMImageData);
			overviewMapAMImg.getAccessibilityProperties().setAlternateDescription(lang.getBundleString("headline"));
			overviewMapAMImg.scaleToFit(mapWidth, 500);
			overviewMapAMImg.setFixedPosition(pageSize.getWidth() / 2 - mapWidth / 2, mapY + mapHeight + 40);
			canvas.add(overviewMapAMImg);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14)
					.moveText(pageSize.getWidth() / 2 - 240, mapY + mapHeight * 2 + 50).setColor(blackColor, true)
					.showText(lang.getBundleString("valid-time-period.earlier")).endText();

			ImageData overviewMapPMImageData = ImageDataFactory.create(getMapImage(DaytimeDependency.pm));
			Image overviewMapPMImg = new Image(overviewMapPMImageData);
			overviewMapAMImg.getAccessibilityProperties().setAlternateDescription(lang.getBundleString("headline"));
			overviewMapPMImg.scaleToFit(mapWidth, 500);
			overviewMapPMImg.setFixedPosition(pageSize.getWidth() / 2 - mapWidth / 2, mapY);
			canvas.add(overviewMapPMImg);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14)
					.moveText(pageSize.getWidth() / 2 - 240, mapY + mapHeight + 10).setColor(blackColor, true)
					.showText(lang.getBundleString("valid-time-period.later")).endText();
		} else {
			ImageData overviewMapImageData = ImageDataFactory.create(getMapImage(DaytimeDependency.fd));
			Image overviewMapImg = new Image(overviewMapImageData);
			overviewMapImg.getAccessibilityProperties().setAlternateDescription(lang.getBundleString("headline"));
			mapY = region.getPdfMapYFd();

			String generalHeadlineComment = avalancheReport.getGeneralHeadline(lang);
			if (generalHeadlineComment != null && !generalHeadlineComment.isEmpty()) {
				Text title = new Text(generalHeadlineComment)
					.setFont(openSansBoldFont)
					.setFontSize(14)
					.setFontColor(blackColor);
				Paragraph p = new Paragraph().add(title)
					.setMultipliedLeading(1.1f)
					.setFixedPosition(pageSize.getWidth() / 2 - (float) region.getPdfMapWidthFd() / 2, region.getPdfMapYFd() + region.getPdfMapHeight() + 120, region.getPdfMapWidthFd());
				canvas.add(p);
			}

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

	protected String getMapImage(DaytimeDependency daytimeDependency, AvalancheBulletin avalancheBulletin) throws MalformedURLException {
		return String.format("%s/%s/%s/%s",
			avalancheReport.getServerInstance().getMapsPath(),
			avalancheReport.getValidityDateString(),
			avalancheReport.getPublicationTimeString(),
			MapUtil.filename(avalancheReport.getRegion(), avalancheBulletin, daytimeDependency, grayscale, MapImageFormat.jpg));
	}

	protected String getMapImage(DaytimeDependency daytimeDependency) throws MalformedURLException {
		return String.format("%s/%s/%s/%s",
			avalancheReport.getServerInstance().getMapsPath(),
			avalancheReport.getValidityDateString(),
			avalancheReport.getPublicationTimeString(),
			MapUtil.getOverviewMapFilename(avalancheReport.getRegion(), daytimeDependency, grayscale));
	}

	private String replaceLinebreaks(String text) {
		return text.replaceAll("[ ]*<br\\/>[ ]*", "\n");
	}

	public static String getDangerRatingText(AvalancheBulletinDaytimeDescription daytimeBulletin, LanguageCode lang) {
		String dangerRatingBelow;
		String dangerRatingAbove;
		if (daytimeBulletin.dangerRating(false) == null || daytimeBulletin.dangerRating(false).equals(DangerRating.missing) || daytimeBulletin.dangerRating(false).equals(DangerRating.no_rating) || daytimeBulletin.dangerRating(false).equals(DangerRating.no_snow)) {
			dangerRatingBelow = DangerRating.no_rating.toString(lang.getLocale(), true);
		} else {
			dangerRatingBelow = daytimeBulletin.dangerRating(false).toString(lang.getLocale(), true);
		}
		if (daytimeBulletin.dangerRating(true) == null || daytimeBulletin.dangerRating(true).equals(DangerRating.missing) || daytimeBulletin.dangerRating(true).equals(DangerRating.no_rating) || daytimeBulletin.dangerRating(true).equals(DangerRating.no_snow)) {
			dangerRatingAbove = DangerRating.no_rating.toString(lang.getLocale(), true);
		} else {
			dangerRatingAbove = daytimeBulletin.dangerRating(true).toString(lang.getLocale(), true);
		}

		if (daytimeBulletin.getTreeline()) {
			return MessageFormat.format(lang.getBundleString("danger-rating.elevation"), dangerRatingBelow, lang.getBundleString("elevation.treeline"), dangerRatingAbove, lang.getBundleString("elevation.treeline"));
		} else if (daytimeBulletin.getElevation() > 0) {
			String elevation = daytimeBulletin.getElevation() + lang.getBundleString("unit.meter");
			return MessageFormat.format(lang.getBundleString("danger-rating.elevation"), dangerRatingBelow, elevation, dangerRatingAbove, elevation);
		} else {
			return dangerRatingAbove;
		}
	}

	public static String getElevationString(AvalancheProblem avalancheProblem, LanguageCode lang) {
		if (avalancheProblem.getTreelineHigh() || avalancheProblem.getElevationHigh() > 0) {
			if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
				// elevation high and low set
				String low = "";
				String high = "";
				if (avalancheProblem.getTreelineLow()) {
					// elevation low treeline
					low = lang.getBundleString("elevation.treeline");
				} else if (avalancheProblem.getElevationLow() > 0) {
					// elevation low number
					low = avalancheProblem.getElevationLow() + lang.getBundleString("unit.meter");
				}
				if (avalancheProblem.getTreelineHigh()) {
					// elevation high treeline
					high = lang.getBundleString("elevation.treeline");
				} else if (avalancheProblem.getElevationHigh() > 0) {
					// elevation high number
					high = avalancheProblem.getElevationHigh() + lang.getBundleString("unit.meter");
				}
				return MessageFormat.format(lang.getBundleString("elevation.band"), low, high);
			} else {
				// elevation high set
				String high = "";
				if (avalancheProblem.getTreelineHigh()) {
					// elevation high treeline
					high = lang.getBundleString("elevation.treeline");
				} else if (avalancheProblem.getElevationHigh() > 0) {
					// elevation high number
					high = avalancheProblem.getElevationHigh() + lang.getBundleString("unit.meter");
				}
				return MessageFormat.format(lang.getBundleString("elevation.below"), high);
			}
		} else if (avalancheProblem.getTreelineLow() || avalancheProblem.getElevationLow() > 0) {
			// elevation low set
			String low = "";
			if (avalancheProblem.getTreelineLow()) {
				// elevation low treeline
				low = lang.getBundleString("elevation.treeline");
			} else if (avalancheProblem.getElevationLow() > 0) {
				// elevation low number
				low = avalancheProblem.getElevationLow() + lang.getBundleString("unit.meter");
			}
			return MessageFormat.format(lang.getBundleString("elevation.above"), low);
		} else {
			// no elevation set
			return lang.getBundleString("elevation.all");
		}
	}

	public static String getAspectString(Set<Aspect> aspects, Locale locale) {
		StringJoiner aspectString = new StringJoiner(", ");
		for (Aspect aspect : aspects) {
			aspectString.add(aspect.toString(locale));
		}
		return aspectString.toString();
	}
}

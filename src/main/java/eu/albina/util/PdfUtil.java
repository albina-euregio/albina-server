package eu.albina.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.ListItem;
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
import eu.albina.model.enumerations.Tendency;

public class PdfUtil {

	private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

	private static PdfUtil instance = null;

	public static final String OPEN_SANS_REGULAR = "/src/main/resources/fonts/open-sans/OpenSans-Regular.ttf";
	public static final String OPEN_SANS_BOLD = "/src/main/resources/fonts/open-sans/OpenSans-Bold.ttf";
	public static final String OPEN_SANS_LIGHT = "/src/main/resources/fonts/open-sans/OpenSans-Light.ttf";

	public static final Color blueColorCmyk = new DeviceCmyk(0.63f, 0.22f, 0.f, 0.f);
	public static final Color greyDarkColorCmyk = new DeviceCmyk(0.66f, 0.52f, 0.52f, 0.25f);
	public static final Color whiteColorCmyk = new DeviceCmyk(0.f, 0.f, 0.f, 0.f);
	public static final Color greyVeryVeryLightColorCmyk = new DeviceCmyk(0.4f, 0.1f, 0.1f, 0.f);
	public static final Color dangerLevel1ColorCmyk = new DeviceCmyk(0.23f, 0.f, 0.78f, 0.f);
	public static final Color dangerLevel2ColorCmyk = new DeviceCmyk(0.6f, 0.f, 0.96f, 0.f);
	public static final Color dangerLevel3ColorCmyk = new DeviceCmyk(0.f, 0.47f, 1.f, 0.f);
	public static final Color dangerLevel4ColorCmyk = new DeviceCmyk(0.f, 1.f, 1.f, 0.f);
	public static final Color dangerLevel5ColorCmyk = new DeviceCmyk(0.f, 1.f, 1.f, 0.f);

	public static final Color blueColor = new DeviceRgb(0, 172, 251);
	public static final Color greyDarkColor = new DeviceRgb(85, 95, 96);
	public static final Color whiteColor = new DeviceRgb(255, 255, 255);
	public static final Color greyVeryVeryLightColor = new DeviceRgb(242, 247, 250);
	public static final Color dangerLevel1Color = new DeviceRgb(197, 255, 118);
	public static final Color dangerLevel2Color = new DeviceRgb(255, 255, 70);
	public static final Color dangerLevel3Color = new DeviceRgb(255, 152, 44);
	public static final Color dangerLevel4Color = new DeviceRgb(255, 0, 23);
	public static final Color dangerLevel5ColorRed = new DeviceRgb(255, 0, 23);
	public static final Color dangerLevel5ColorBlack = new DeviceRgb(0, 0, 0);

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
	 */
	public void createOverviewPdfs(List<AvalancheBulletin> bulletins) {
		for (LanguageCode lang : GlobalVariables.languages)
			createOverviewPdf(bulletins, lang);
	}

	public void createOverviewPdf(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		try {
			PdfDocument pdf;
			PdfWriter writer;

			switch (lang) {
			case de:
				writer = new PdfWriter(GlobalVariables.pdfDirectory + "Lawinenvorhersage"
						+ AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				pdf = new PdfDocument(writer);
				break;
			case it:
				writer = new PdfWriter(GlobalVariables.pdfDirectory + "Previsione Valanghe"
						+ AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				pdf = new PdfDocument(writer);
				break;
			case en:
				writer = new PdfWriter(GlobalVariables.pdfDirectory + "Avalanche Forecast"
						+ AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				pdf = new PdfDocument(writer);
				break;
			default:
				writer = new PdfWriter(GlobalVariables.pdfDirectory + "Avalanche Forecast"
						+ AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				pdf = new PdfDocument(writer);
				break;
			}

			// PdfFontFactory.registerDirectory("./src/main/resources/fonts/open-sans");
			PdfFontFactory.registerDirectory(GlobalVariables.localFontsPath);
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

			pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new AvalancheBulletinEventHandler(lang, bulletins));
			Document document = new Document(pdf);
			document.setRenderer(new DocumentRenderer(document));
			document.setMargins(110, 30, 60, 50);

			createPdfFrontPage(bulletins, lang, document, pdf);

			for (AvalancheBulletin avalancheBulletin : bulletins) {
				createPdfBulletinPage(avalancheBulletin, lang, document, pdf,
						AlbinaUtil.getTendencyDate(bulletins, lang), writer);
			}

			document.close();
		} catch (FileNotFoundException e) {
			logger.error("PDF could not be created: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("PDF could not be created: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void createPdfBulletinPage(AvalancheBulletin avalancheBulletin, LanguageCode lang, Document document,
			PdfDocument pdf, String tendencyDate, PdfWriter writer) throws IOException {
		document.add(new AreaBreak());

		float leadingHeadline = 1.f;
		float leadingText = 1.2f;
		float paddingLeft = 15.f;
		float regionMapSize = 100;

		float[] columnWidths = { 1 };
		Table table = new Table(columnWidths).setBorder(null);
		table.setWidthPercent(100);
		Cell cell;

		Paragraph dangerRatingHeadline = new Paragraph(AlbinaUtil.getDangerRatingText(avalancheBulletin, lang))
				.setFont(openSansBoldFont).setFontSize(14)
				.setFontColor(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()))
				.setMultipliedLeading(leadingHeadline);
		cell = new Cell(1, 10).add(dangerRatingHeadline);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setPaddingLeft(paddingLeft);
		cell.setBorder(Border.NO_BORDER);
		cell.setBorderLeft(new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()), 4));
		table.addCell(cell);

		if (avalancheBulletin.isHasDaytimeDependency()) {
			float[] secondColumnWidths = { 1, 1, 1 };
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			Paragraph am = new Paragraph("AM:").setFont(openSansBoldFont).setFontSize(14).setFontColor(greyDarkColor);
			cell = new Cell(1, 1).add(am);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addCell(cell);
			ImageData regionAMImageDate = ImageDataFactory.create(IOUtils.toByteArray(this.getClass().getClassLoader()
					.getResourceAsStream(GlobalVariables.mapsPath + "bulletin-report-region.png")));
			Image regionAMImg = new Image(regionAMImageDate);
			regionAMImg.scaleToFit(regionMapSize, regionMapSize);
			regionAMImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionAMImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, false, lang, tendencyDate, pdf, document, writer));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 10);
			cell.add(secondTable);
			cell.setVerticalAlignment(VerticalAlignment.TOP);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setPaddingRight(5);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()), 4));
			table.addCell(cell);

			secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			secondTable.setBorderTop(new SolidBorder(greyDarkColor, 1));
			Paragraph pm = new Paragraph("PM:").setFont(openSansBoldFont).setFontSize(14).setFontColor(greyDarkColor);
			cell = new Cell(1, 1).add(pm);
			cell.setBorder(Border.NO_BORDER);
			cell.setTextAlignment(TextAlignment.LEFT);
			secondTable.addCell(cell);
			ImageData regionPMImageDate = ImageDataFactory.create(IOUtils.toByteArray(this.getClass().getClassLoader()
					.getResourceAsStream(GlobalVariables.mapsPath + "bulletin-report-region.png")));
			Image regionPMImg = new Image(regionPMImageDate);
			regionPMImg.scaleToFit(regionMapSize, regionMapSize);
			regionPMImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionPMImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, true, lang, tendencyDate, pdf, document, writer));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 10);
			cell.add(secondTable);
			cell.setVerticalAlignment(VerticalAlignment.TOP);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setPaddingRight(5);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()), 4));
			table.addCell(cell);
		} else {
			float[] secondColumnWidths = { 1, 1 };
			Table secondTable = new Table(secondColumnWidths).setBorder(Border.NO_BORDER);
			ImageData regionImageDate = ImageDataFactory.create(IOUtils.toByteArray(this.getClass().getClassLoader()
					.getResourceAsStream(GlobalVariables.mapsPath + "bulletin-report-region.png")));
			Image regionImg = new Image(regionImageDate);
			regionImg.scaleToFit(regionMapSize, regionMapSize);
			regionImg.setMarginRight(10);
			cell = new Cell(1, 1).add(regionImg);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			secondTable.addCell(cell);
			cell = new Cell(1, 1)
					.add(createSymbols(avalancheBulletin, false, lang, tendencyDate, pdf, document, writer));
			cell.setBorder(Border.NO_BORDER);
			secondTable.addCell(cell);
			cell = new Cell(1, 10);
			cell.add(secondTable);
			cell.setVerticalAlignment(VerticalAlignment.TOP);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setPaddingRight(5);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()), 4));
			table.addCell(cell);
		}

		// avalanche activity
		if (avalancheBulletin.getAvActivityHighlightsIn(lang) != null) {
			Paragraph avActivityHighlights = new Paragraph(avalancheBulletin.getAvActivityHighlightsIn(lang))
					.setFont(openSansRegularFont).setFontSize(14).setFontColor(greyDarkColor)
					.setMultipliedLeading(leadingHeadline);
			cell = new Cell(1, 10).add(avActivityHighlights);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()), 4));
			table.addCell(cell);
		}

		if (avalancheBulletin.getAvActivityCommentIn(lang) != null) {
			Paragraph avActivityComment = new Paragraph(avalancheBulletin.getAvActivityCommentIn(lang))
					.setFont(openSansRegularFont).setFontSize(10).setFontColor(greyDarkColor)
					.setMultipliedLeading(leadingText).setMarginBottom(5);
			cell = new Cell(1, 10).add(avActivityComment);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setPaddingLeft(paddingLeft);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(getDangerRatingColor(avalancheBulletin.getHighestDangerRating()), 4));
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
						.setFont(openSansRegularFont).setFontSize(14).setFontColor(greyDarkColor).setMarginTop(5)
						.setMultipliedLeading(leadingHeadline));
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				cell.setBorderLeft(new SolidBorder(blueColor, 4));
				cell.setBackgroundColor(greyVeryVeryLightColor);
				table.addCell(cell);

				// add danger patterns
				if (avalancheBulletin.getDangerPattern1() != null || avalancheBulletin.getDangerPattern2() != null) {
					float[] dangerPatternColumnWidths = { 1, 1, 1 };
					Table dangerPatternTable = new Table(dangerPatternColumnWidths);
					Paragraph dangerPatternHeadline = new Paragraph(GlobalVariables.getDangerPatternsHeadline(lang))
							.setFont(openSansBoldFont).setFontSize(8).setFontColor(greyDarkColor);
					cell = new Cell(1, 1).add(dangerPatternHeadline);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setBorder(Border.NO_BORDER);
					dangerPatternTable.addCell(cell);

					if (avalancheBulletin.getDangerPattern1() != null) {
						Paragraph paragraph = new Paragraph(
								AlbinaUtil.getDangerPatternText(avalancheBulletin.getDangerPattern1(), lang))
										.setFont(openSansRegularFont).setFontSize(8).setFontColor(greyDarkColor);
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
										.setFont(openSansRegularFont).setFontSize(8).setFontColor(greyDarkColor);
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
					cell.setBorderLeft(new SolidBorder(blueColor, 4));
					cell.setBackgroundColor(greyVeryVeryLightColor);
					table.addCell(cell);
				}

				if (avalancheBulletin.getSnowpackStructureCommentIn(lang) != null) {
					Paragraph snowpackStructureComment = new Paragraph(
							avalancheBulletin.getSnowpackStructureCommentIn(lang)).setFont(openSansRegularFont)
									.setFontSize(10).setFontColor(greyDarkColor).setMultipliedLeading(leadingText);
					cell = new Cell(1, 10).add(snowpackStructureComment);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setPaddingLeft(paddingLeft);
					cell.setBorder(Border.NO_BORDER);
					cell.setBorderLeft(new SolidBorder(blueColor, 4));
					cell.setBackgroundColor(greyVeryVeryLightColor);
					table.addCell(cell);
				}
			}

			if (avalancheBulletin.getTendencyCommentIn(lang) != null) {
				Paragraph tendencyHeadline = new Paragraph(GlobalVariables.getTendencyHeadline(lang))
						.setFont(openSansRegularFont).setFontSize(14).setFontColor(greyDarkColor).setMarginTop(10)
						.setMultipliedLeading(leadingHeadline);
				cell = new Cell(1, 10).add(tendencyHeadline);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				cell.setBorderLeft(new SolidBorder(blueColor, 4));
				cell.setBackgroundColor(greyVeryVeryLightColor);
				table.addCell(cell);

				Paragraph tendencyComment = new Paragraph(avalancheBulletin.getTendencyCommentIn(lang))
						.setFont(openSansRegularFont).setFontSize(10).setFontColor(greyDarkColor)
						.setMultipliedLeading(leadingText).setMarginBottom(5);
				cell = new Cell(1, 10).add(tendencyComment);
				cell.setTextAlignment(TextAlignment.LEFT);
				cell.setPaddingLeft(paddingLeft);
				cell.setBorder(Border.NO_BORDER);
				cell.setBorderLeft(new SolidBorder(blueColor, 4));
				cell.setBackgroundColor(greyVeryVeryLightColor);
				table.addCell(cell);
			}
		}

		document.add(table).setLeftMargin(50);
	}

	private Table createSymbols(AvalancheBulletin avalancheBulletin, boolean isAfternoon, LanguageCode lang,
			String tendencyDate, PdfDocument pdf, Document document, PdfWriter writer) throws MalformedURLException {
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

		Paragraph firstRow = new Paragraph("").setFont(openSansBoldFont).setFontSize(8).setFontColor(greyDarkColor);

		Image regionImg = getImage("warning_pictos/level_"
				+ AlbinaUtil.getWarningLevelId(daytimeBulletin, avalancheBulletin.isHasElevationDependency()) + ".png");
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
			if (avalancheBulletin.getTreeline()) {
				Paragraph paragraph = new Paragraph(GlobalVariables.getTreelineString(lang)).setFontColor(greyDarkColor)
						.setFontSize(8).setFont(openSansBoldFont);
				paragraph.setRelativePosition(-2, 2, 0, 0);
				cell.add(paragraph);
			} else if (avalancheBulletin.getElevation() > 0) {
				Paragraph paragraph = new Paragraph(avalancheBulletin.getElevation() + "m").setFontColor(greyDarkColor)
						.setFontSize(8).setFont(openSansBoldFont);
				paragraph.setRelativePosition(-2, 2, 0, 0);
				cell.add(paragraph);
			}
		}
		firstRowTable.addCell(cell);

		if (avalancheBulletin.getTendency() != null) {
			cell = new Cell(1, 1);
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.setHeight(height);
			cell.setBorder(Border.NO_BORDER);
			cell.setBorderLeft(new SolidBorder(greyDarkColor, 0.5f));

			Paragraph paragraph = new Paragraph().setFontSize(8).setFontColor(greyDarkColor).setMarginLeft(10)
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
			if (avalancheBulletin.getTendency() == Tendency.decreasing) {
				Image tendencyImg = getImage("tendency/tendency_decreasing_blue.png");
				if (tendencyImg != null) {
					tendencyImg.scaleToFit(25, 20);
					tendencyImg.setMarginLeft(5);
					cell.add(tendencyImg);
				}
			} else if (avalancheBulletin.getTendency() == Tendency.steady) {
				Image tendencyImg = getImage("tendency/tendency_steady_blue.png");
				if (tendencyImg != null) {
					tendencyImg.scaleToFit(25, 20);
					tendencyImg.setMarginLeft(5);
					cell.add(tendencyImg);
				}
			} else if (avalancheBulletin.getTendency() == Tendency.increasing) {
				Image tendencyImg = getImage("tendency/tendency_increasing_blue.png");
				if (tendencyImg != null) {
					tendencyImg.scaleToFit(25, 20);
					tendencyImg.setMarginLeft(5);
					cell.add(tendencyImg);
				}
			}

			firstRowTable.addCell(cell);
		}

		cell = new Cell(1, 1).add(firstRowTable);
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);

		cell = new Cell(1, 1).add(createAvalancheSituations(daytimeBulletin, lang, pdf, document, writer, isAfternoon,
				avalancheBulletin.isHasDaytimeDependency()));
		cell.setTextAlignment(TextAlignment.LEFT);
		cell.setBorder(Border.NO_BORDER);
		table.addCell(cell);

		return table;
	}

	private Table createAvalancheSituations(AvalancheBulletinDaytimeDescription daytimeBulletin, LanguageCode lang,
			PdfDocument pdf, Document document, PdfWriter writer, boolean isAfternoon, boolean hasDaytime)
			throws MalformedURLException {
		float[] columnWidths = { 1, 1, 1, 1, 1, 1, 1, 1 };
		Table table = new Table(columnWidths);

		table.setBorderTop(new SolidBorder(greyDarkColor, 0.5f));

		if (daytimeBulletin.getAvalancheSituation1() != null
				&& daytimeBulletin.getAvalancheSituation1().getAvalancheSituation() != null)
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation1(), lang, table, false, document, writer,
					isAfternoon, hasDaytime);
		if (daytimeBulletin.getAvalancheSituation2() != null
				&& daytimeBulletin.getAvalancheSituation2().getAvalancheSituation() != null)
			createAvalancheSituation(daytimeBulletin.getAvalancheSituation2(), lang, table, true, document, writer,
					isAfternoon, hasDaytime);

		return table;
	}

	private Image getImage(String path) {
		try {
			String name = GlobalVariables.localImagesPath + path;
			ImageData imageData = ImageDataFactory
					.create(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(name)));
			return new Image(imageData);
		} catch (IOException e) {
			logger.warn("Image could not be loaded: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private void createAvalancheSituation(AvalancheSituation avalancheSituation, LanguageCode lang, Table table,
			boolean isSecond, Document document, PdfWriter writer, boolean isAfternoon, boolean hasDaytime)
			throws MalformedURLException {
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
				img = getImage("avalanche_situations/color/" + avalancheSituation.getAvalancheSituation().toStringId()
						+ ".png");
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
					avalancheSituationTable.setBorderLeft(new SolidBorder(greyDarkColor, 0.5f));
				paragraph = new Paragraph(avalancheSituation.getAvalancheSituation().toString(lang))
						.setFont(openSansRegularFont).setFontSize(8).setFontColor(greyDarkColor)
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

				img = getImage("aspects/" + new Integer(result).toString() + ".png");
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
					img = getImage("elevation/levels_middle_two.png");
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
					if (avalancheSituation.getTreelineLow()) {
						Paragraph paragraph2 = new Paragraph(GlobalVariables.getTreelineString(lang))
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(greyDarkColor);
						paragraph2.setRelativePosition(-6, 2, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					} else if (avalancheSituation.getElevationLow() > 0) {
						Paragraph paragraph2 = new Paragraph(avalancheSituation.getElevationLow() + "m")
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(greyDarkColor);
						paragraph2.setRelativePosition(-6, 2, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					}
					if (avalancheSituation.getTreelineHigh()) {
						Paragraph paragraph2 = new Paragraph(GlobalVariables.getTreelineString(lang))
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(greyDarkColor);
						paragraph2.setRelativePosition(-6, -3, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.setPadding(padding);
						cell.add(paragraph2);
						elevationTable.addCell(cell);
					} else if (avalancheSituation.getElevationHigh() > 0) {
						Paragraph paragraph2 = new Paragraph(avalancheSituation.getElevationHigh() + "m")
								.setFont(openSansBoldFont).setFontSize(8).setFontColor(greyDarkColor);
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
					img = getImage("elevation/levels_below.png");
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
								.setFontSize(8).setFontColor(greyDarkColor);
						paragraph.setRelativePosition(-6, -4, 0, 0);
						cell = new Cell(1, 1);
						cell.setTextAlignment(TextAlignment.LEFT);
						cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
						cell.setBorder(Border.NO_BORDER);
						cell.add(paragraph);
						elevationTable.addCell(cell);
					} else if (avalancheSituation.getElevationHigh() > 0) {
						paragraph = new Paragraph(avalancheSituation.getElevationHigh() + "m").setFont(openSansBoldFont)
								.setFontSize(8).setFontColor(greyDarkColor);
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
				img = getImage("elevation/levels_above.png");
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
							.setFontSize(8).setFontColor(greyDarkColor);
					paragraph.setRelativePosition(-6, 4, 0, 0);
					cell = new Cell(1, 1);
					cell.setTextAlignment(TextAlignment.LEFT);
					cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
					cell.setBorder(Border.NO_BORDER);
					cell.add(paragraph);
					elevationTable.addCell(cell);
				} else if (avalancheSituation.getElevationLow() > 0) {
					paragraph = new Paragraph(avalancheSituation.getElevationLow() + "m").setFont(openSansBoldFont)
							.setFontSize(8).setFontColor(greyDarkColor);
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
				img = getImage("elevation/levels_all.png");
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

	private void createPdfFrontPage(List<AvalancheBulletin> bulletins, LanguageCode lang, Document document,
			PdfDocument pdf) {
		try {
			PdfPage page = pdf.addNewPage();
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
			Canvas canvas = new Canvas(pdfCanvas, pdf, page.getPageSize());

			// Add overview maps
			if (AlbinaUtil.hasDaytimeDependency(bulletins)) {
				ImageData overviewMapAMImageData = ImageDataFactory.create(IOUtils.toByteArray(this.getClass()
						.getClassLoader().getResourceAsStream(GlobalVariables.mapsPath + "bulletin-overview.jpg")));
				Image overviewMapAMImg = new Image(overviewMapAMImageData);
				overviewMapAMImg.scaleToFit(220, 500);
				overviewMapAMImg.setFixedPosition(pageSize.getWidth() / 2 - 230, 500);
				canvas.add(overviewMapAMImg);
				pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14).moveText(pageSize.getWidth() / 2 - 226, 714)
						.setColor(greyDarkColor, true).showText("AM").endText();

				ImageData overviewMapPMImageData = ImageDataFactory.create(IOUtils.toByteArray(this.getClass()
						.getClassLoader().getResourceAsStream(GlobalVariables.mapsPath + "bulletin-overview.jpg")));
				Image overviewMapPMImg = new Image(overviewMapPMImageData);
				overviewMapPMImg.scaleToFit(220, 500);
				overviewMapPMImg.setFixedPosition(pageSize.getWidth() / 2 + 10, 500);
				canvas.add(overviewMapPMImg);
				pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 14).moveText(pageSize.getWidth() / 2 + 14, 714)
						.setColor(greyDarkColor, true).showText("PM").endText();
			} else {
				ImageData overviewMapImageData = ImageDataFactory.create(IOUtils.toByteArray(this.getClass()
						.getClassLoader().getResourceAsStream(GlobalVariables.mapsPath + "bulletin-overview.jpg")));
				Image overviewMapImg = new Image(overviewMapImageData);
				overviewMapImg.scaleToFit(220, 500);
				overviewMapImg.setFixedPosition(pageSize.getWidth() / 2 - 110, 500);
				canvas.add(overviewMapImg);
			}

			// add legend
			int legendEntryWidth = 50;
			int legendEntryHeight = 8;
			int y = 478;
			Rectangle dangerLevel1Rectangle = new Rectangle(
					pageSize.getWidth() / 2 - 2 * legendEntryWidth - legendEntryWidth / 2, y, legendEntryWidth,
					legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel1Rectangle).setColor(dangerLevel1Color, true).fill();
			Rectangle dangerLevel2Rectangle = new Rectangle(
					pageSize.getWidth() / 2 - legendEntryWidth - legendEntryWidth / 2, y, legendEntryWidth,
					legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel2Rectangle).setColor(dangerLevel2Color, true).fill();
			Rectangle dangerLevel3Rectangle = new Rectangle(pageSize.getWidth() / 2 - legendEntryWidth / 2, y,
					legendEntryWidth, legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel3Rectangle).setColor(dangerLevel3Color, true).fill();
			Rectangle dangerLevel4Rectangle = new Rectangle(pageSize.getWidth() / 2 + legendEntryWidth / 2, y,
					legendEntryWidth, legendEntryHeight);
			pdfCanvas.rectangle(dangerLevel4Rectangle).setColor(dangerLevel4Color, true).fill();

			for (int j = 0; j < 2; j++) {
				for (int i = 0; i < 12; i++) {
					Rectangle dangerLevel5Rectangle = new Rectangle(
							pageSize.getWidth() / 2 + legendEntryWidth + legendEntryWidth / 2 + i * 4, y + j * 4, 4, 4);
					if ((i + j) % 2 == 0)
						pdfCanvas.rectangle(dangerLevel5Rectangle).setColor(dangerLevel5ColorRed, true).fill();
					else
						pdfCanvas.rectangle(dangerLevel5Rectangle).setColor(dangerLevel5ColorBlack, true).fill();
				}
			}

			float width;
			float fontSize = 8;
			y = 468;
			width = openSansBoldFont.getContentWidth(new PdfString("1")) * 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
					.moveText(pageSize.getWidth() / 2 - 2 * legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText("1").endText();
			width = openSansBoldFont.getContentWidth(new PdfString("2")) * 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
					.moveText(pageSize.getWidth() / 2 - legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText("2").endText();
			width = openSansBoldFont.getContentWidth(new PdfString("3")) * 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
					.moveText(pageSize.getWidth() / 2 - width, y).setColor(greyDarkColor, true).showText("3").endText();
			width = openSansBoldFont.getContentWidth(new PdfString("4")) * 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
					.moveText(pageSize.getWidth() / 2 + legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText("4").endText();
			width = openSansBoldFont.getContentWidth(new PdfString("5")) * 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, fontSize)
					.moveText(pageSize.getWidth() / 2 + 2 * legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText("5").endText();

			y = 459;
			width = openSansRegularFont
					.getContentWidth(new PdfString(GlobalVariables.getDangerRatingText(DangerRating.low, lang)))
					* 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
					.moveText(pageSize.getWidth() / 2 - 2 * legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText(GlobalVariables.getDangerRatingText(DangerRating.low, lang)).endText();
			width = openSansRegularFont
					.getContentWidth(new PdfString(GlobalVariables.getDangerRatingText(DangerRating.moderate, lang)))
					* 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
					.moveText(pageSize.getWidth() / 2 - legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText(GlobalVariables.getDangerRatingText(DangerRating.moderate, lang)).endText();
			width = openSansRegularFont.getContentWidth(
					new PdfString(GlobalVariables.getDangerRatingText(DangerRating.considerable, lang))) * 0.001f
					* fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
					.moveText(pageSize.getWidth() / 2 - width, y).setColor(greyDarkColor, true)
					.showText(GlobalVariables.getDangerRatingText(DangerRating.considerable, lang)).endText();
			width = openSansRegularFont
					.getContentWidth(new PdfString(GlobalVariables.getDangerRatingText(DangerRating.high, lang)))
					* 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
					.moveText(pageSize.getWidth() / 2 + legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText(GlobalVariables.getDangerRatingText(DangerRating.high, lang)).endText();
			width = openSansRegularFont
					.getContentWidth(new PdfString(GlobalVariables.getDangerRatingText(DangerRating.very_high, lang)))
					* 0.001f * fontSize / 2;
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, fontSize)
					.moveText(pageSize.getWidth() / 2 + 2 * legendEntryWidth - width, y).setColor(greyDarkColor, true)
					.showText(GlobalVariables.getDangerRatingText(DangerRating.very_high, lang)).endText();

			// adding content
			float marginRight = 50.f;
			float marginLeft = 30.f;
			Paragraph title = new Paragraph("How To").setFont(openSansRegularFont).setFontSize(14).setMarginTop(290)
					.setMarginRight(marginRight).setMarginLeft(marginLeft);
			document.add(title);
			Paragraph subtitle1 = new Paragraph(
					"Ich bin Blindtext. Von Geburt an. Es hat lange gedauert, bis ich begriffen habe, was es bedeutet, ein blinder Text zu sein:")
							.setFont(openSansBoldFont).setFontSize(10).setMarginRight(marginRight)
							.setMarginLeft(marginLeft);
			document.add(subtitle1);
			com.itextpdf.layout.element.List list1 = new com.itextpdf.layout.element.List().setSymbolIndent(12)
					.setMarginLeft(10 + marginLeft).setListSymbol("\u2022").setFont(openSansRegularFont).setFontSize(10)
					.setMarginRight(marginRight);
			list1.add(new ListItem("Oft wird man gar nicht erst gelesen"))
					.add(new ListItem("Man wirkt hier und da aus dem Zusammenhang gerissen"))
					.add(new ListItem("Aber bin ich deshalb ein schlechter Text?"));
			document.add(list1);
			Paragraph subtitle2 = new Paragraph("Man macht gar keinen Sinn, Nullkommajosef:").setFont(openSansBoldFont)
					.setFontSize(10).setMarginRight(marginRight).setMarginLeft(marginLeft);
			document.add(subtitle2);
			com.itextpdf.layout.element.List list2 = new com.itextpdf.layout.element.List().setSymbolIndent(12)
					.setMarginLeft(10 + marginLeft).setListSymbol("\u2022").setFont(openSansRegularFont).setFontSize(10)
					.setMarginRight(marginRight);
			list2.add(new ListItem("Ich weiß, dass ich nie die Chance haben werde im Stern zu erscheinen"))
					.add(new ListItem("Man wirkt hier und da aus dem Zusammenhang gerissen"));
			document.add(list2);

			PdfLinkAnnotation annotation = new PdfLinkAnnotation(new Rectangle(0, 0))
					.setAction(PdfAction.createURI("http://www.avalanches.org/"));
			Link link = new Link("www.avalanches.org", annotation);
			Paragraph normalText = new Paragraph("Weitere Fachbegriffe und Definitionen finden Sie im Glossar unter ")
					.add(link.setFontColor(blueColor)).setFont(openSansRegularFont).setFontSize(10)
					.setMarginRight(marginRight).setMarginLeft(marginLeft);
			document.add(normalText);

			canvas.close();
			pdfCanvas.release();
		} catch (MalformedURLException e) {
			logger.error("PDF front page could not be created: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("PDF front page could not be created: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Create PDFs for each province (TN, BZ, TI) containing an overview map and the
	 * detailed information about each aggregated region touching the province.
	 * 
	 * @param bulletins
	 *            The bulletins to create the region PDFs of.
	 */
	public void createRegionPdfs(List<AvalancheBulletin> bulletins) {
		// TODO Implement creation of PDFs for each province.
	}
}

package eu.albina.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class AvalancheBulletinEventHandler implements IEventHandler {

	private List<AvalancheBulletin> bulletins;
	private LanguageCode lang;

	public static final String OPEN_SANS_REGULAR = "./src/main/resources/fonts/open-sans/OpenSans-Regular.ttf";
	public static final String OPEN_SANS_BOLD = "./src/main/resources/fonts/open-sans/OpenSans-Bold.ttf";
	public static final String OPEN_SANS_LIGHT = "./src/main/resources/fonts/open-sans/OpenSans-Light.ttf";

	public static final Color blueColor = new DeviceCmyk(0.63f, 0.22f, 0.f, 0.f);
	public static final Color greyDarkColor = new DeviceCmyk(0.66f, 0.52f, 0.52f, 0.25f);
	public static final Color whiteColor = new DeviceCmyk(0.f, 0.f, 0.f, 0.f);

	public AvalancheBulletinEventHandler(LanguageCode lang, List<AvalancheBulletin> bulletins) {
		this.lang = lang;
		this.bulletins = bulletins;
	}

	public void handleEvent(Event event) {
		try {
			PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
			PdfDocument pdfDoc = docEvent.getDocument();
			PdfPage page = docEvent.getPage();
			Rectangle pageSize = page.getPageSize();
			PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

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
					.setColor(greyDarkColor, true).showText(headline);
			String date = AlbinaUtil.getDate(bulletins, lang);
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 16).moveText(20, pageSize.getTop() - 60)
					.setColor(blueColor, true).showText(date);

			String publicationDate = AlbinaUtil.getPublicationDate(bulletins, lang);
			if (!publicationDate.isEmpty())
				pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, pageSize.getTop() - 75)
						.setColor(greyDarkColor, true)
						.showText(GlobalVariables.getPublishedText(lang) + publicationDate);

			Canvas canvas = new Canvas(pdfCanvas, pdfDoc, page.getPageSize());

			// Add copyright
			String copyright = GlobalVariables.getCopyrightText(lang);
			pdfCanvas.beginText().setFontAndSize(openSansRegularFont, 8).moveText(20, 20).setColor(blueColor, true)
					.showText(copyright);

			String urlString = AlbinaUtil.getUrl(lang);
			Rectangle buttonRectangle = new Rectangle(pageSize.getWidth() - 150, 12, 130, 24);
			pdfCanvas.rectangle(buttonRectangle).setColor(blueColor, true).fill();
			pdfCanvas.beginText().setFontAndSize(openSansBoldFont, 8)
					.moveText(buttonRectangle.getLeft() + 15, buttonRectangle.getBottom() + 9)
					.setColor(whiteColor, true).showText(urlString);

			// Draw lines
			pdfCanvas.setLineWidth(1).setStrokeColor(blueColor).moveTo(0, pageSize.getHeight() - 90)
					.lineTo(pageSize.getWidth(), pageSize.getHeight() - 90).stroke();
			pdfCanvas.setLineWidth(1).setStrokeColor(blueColor).moveTo(0, 48).lineTo(pageSize.getWidth(), 48).stroke();

			// Add CI
			Image ciImg = PdfUtil.getInstance().getImage("Colorbar.gif");
			ciImg.scaleAbsolute(pageSize.getWidth(), 4);
			ciImg.setFixedPosition(0, pageSize.getHeight() - 4);
			canvas.add(ciImg);

			// Add logo
			Image logoImg = PdfUtil.getInstance().getImage(GlobalVariables.getLogoPath(lang));
			logoImg.scaleToFit(130, 55);
			logoImg.setFixedPosition(pageSize.getWidth() - 100, pageSize.getHeight() - 72);
			canvas.add(logoImg);

			canvas.close();

			pdfCanvas.release();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
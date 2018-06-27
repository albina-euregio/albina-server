package eu.albina.util;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.enumerations.LanguageCode;

public class PdfUtil {

	/**
	 * Create a PDF containing all the information for the EUREGIO.
	 * 
	 * @param bulletins
	 *            The bulletins to create the PDF of.
	 */
	public static void createOverviewPdfs(List<AvalancheBulletin> bulletins) {
		for (LanguageCode lang : GlobalVariables.languages)
			createOverviewPdf(bulletins, lang);
	}

	public static void createOverviewPdf(List<AvalancheBulletin> bulletins, LanguageCode lang) {
		// Create a new empty document
		PDDocument document = new PDDocument();
		PDDocumentInformation documentInformation = document.getDocumentInformation();
		switch (lang) {
		case de:
			documentInformation.setTitle("Lawinenvorhersage, " + AlbinaUtil.getDate(bulletins, lang));
			break;
		case it:
			documentInformation.setTitle("Previsione Valanghe, " + AlbinaUtil.getDate(bulletins, lang));
			break;
		case en:
			documentInformation.setTitle("Avalanche Forecast, " + AlbinaUtil.getDate(bulletins, lang));
			break;
		default:
			documentInformation.setTitle("Avalanche Forecast, " + AlbinaUtil.getDate(bulletins, LanguageCode.en));
			break;
		}

		try {

			createFrontPage(document, lang);

			// Save the newly created document
			switch (lang) {
			case de:
				document.save("Lawinenvorhersage " + AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				break;
			case it:
				document.save("Previsione Valanghe " + AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				break;
			case en:
				document.save("Avalanche Forecast " + AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				break;
			default:
				document.save("Avalanche Forecast " + AlbinaUtil.getFilenameDate(bulletins, lang) + ".pdf");
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				document.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void createFrontPage(PDDocument doc, LanguageCode lang) throws IOException {
		PDPage page = new PDPage();
		doc.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(doc, page);

		// Colorbar
		PDImageXObject ci = PDImageXObject.createFromFile("D:\\images\\Colorbar.gif", doc);
		contentStream.drawImage(ci, 0, 788, 612, 4);
		// contentStream.drawImage(ci, 0, 0, 612, 4);

		// Begin the Content stream
		contentStream.beginText();

		// Setting the font to the Content stream
		contentStream.setFont(PDType1Font.HELVETICA, 16);

		// Setting the position for the line
		contentStream.newLineAtOffset(20, 755);

		String text = "";
		switch (lang) {
		case de:
			text = "Lawinenvorhersage";
			break;
		case it:
			text = "Previsione Valanghe";
			break;
		case en:
			text = "Avalanche Forecast";
			break;
		default:
			text = "Avalanche Forecast";
			break;
		}

		// Adding text in the form of string
		contentStream.showText(text);

		// Ending the content stream
		contentStream.endText();

		PDImageXObject overviewMap = PDImageXObject.createFromFile("D:\\images\\bulletin-overview.jpg", doc);
		contentStream.drawImage(overviewMap, 60, 62, 300, 313);
		contentStream.close();
	}

	/**
	 * Create PDFs for each province (TN, BZ, TI) containing an overview map and the
	 * detailed information about each aggregated region touching the province.
	 * 
	 * @param bulletins
	 *            The bulletins to create the region PDFs of.
	 */
	public static void createRegionPdfs(List<AvalancheBulletin> bulletins) {
		// TODO Implement creation of PDFs for each province.
	}

}

package eu.albina.util;

import java.util.List;

import eu.albina.model.AvalancheBulletin;

public class PdfUtil {

	/**
	 * Create a PDF containing all the information for the EUREGIO.
	 * 
	 * @param bulletins
	 *            The bulletins to create the PDF of.
	 */
	public static void createOverviewPdf(List<AvalancheBulletin> bulletins) {
		// TODO implement creation of overview PDF
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

package eu.albina.thread;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.util.PdfUtil;

public class PdfThread implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(PdfThread.class);

	private List<String> avalancheReportIds;
	private List<AvalancheBulletin> bulletins;

	public PdfThread(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		this.avalancheReportIds = avalancheReportIds;
		this.bulletins = bulletins;
	}

	@Override
	public void run() {
		try {
			logger.info("PDF production started");
			PdfUtil.getInstance().createOverviewPdfs(bulletins);
			PdfUtil.getInstance().createRegionPdfs(bulletins);
			AvalancheReportController.getInstance().setAvalancheReportPdfFlag(avalancheReportIds);
		} catch (IOException e) {
			logger.error("Error creating pdfs:" + e.getMessage());
			e.printStackTrace();
		} catch (URISyntaxException e) {
			logger.error("Error creating pdfs:" + e.getMessage());
			e.printStackTrace();
		} finally {
			logger.info("Map production finished");
		}
	}

}

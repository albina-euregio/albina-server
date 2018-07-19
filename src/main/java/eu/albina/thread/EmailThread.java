package eu.albina.thread;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.util.EmailUtil;

public class EmailThread implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(EmailThread.class);

	private List<String> avalancheReportIds;
	private List<String> regions;
	private List<AvalancheBulletin> bulletins;

	public EmailThread(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins, List<String> regions) {
		this.avalancheReportIds = avalancheReportIds;
		this.bulletins = bulletins;
		this.regions = regions;
	}

	@Override
	public void run() {
		try {
			logger.info("Email production started");
			EmailUtil.getInstance().sendBulletinEmails(bulletins, regions);
			AvalancheReportController.getInstance().setAvalancheReportEmailFlag(avalancheReportIds);
		} catch (IOException e) {
			logger.error("Error preparing emails:" + e.getMessage());
			e.printStackTrace();
		} catch (URISyntaxException e) {
			logger.error("Error preparing emails:" + e.getMessage());
			e.printStackTrace();
		} finally {
			logger.info("Email production finished");
		}
	}

}

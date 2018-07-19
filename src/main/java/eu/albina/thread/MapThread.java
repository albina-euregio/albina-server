package eu.albina.thread;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.AvalancheReportController;
import eu.albina.model.AvalancheBulletin;
import eu.albina.util.MapUtil;

public class MapThread implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(MapThread.class);

	private List<String> avalancheReportIds;
	private List<AvalancheBulletin> bulletins;

	public MapThread(List<String> avalancheReportIds, List<AvalancheBulletin> bulletins) {
		this.avalancheReportIds = avalancheReportIds;
		this.bulletins = bulletins;
	}

	@Override
	public void run() {
		logger.info("Map production started");
		MapUtil.createDangerRatingMaps(bulletins);
		AvalancheReportController.getInstance().setAvalancheReportMapFlag(avalancheReportIds);
		logger.info("Map production finished");
	}

}

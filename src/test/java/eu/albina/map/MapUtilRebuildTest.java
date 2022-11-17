package eu.albina.map;

import com.google.common.io.Resources;
import eu.albina.controller.AvalancheReportController;
import eu.albina.controller.RegionController;
import eu.albina.exception.AlbinaException;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.util.AlbinaUtil;
import eu.albina.util.HibernateUtil;
import eu.albina.util.PdfUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public class MapUtilRebuildTest {

	private static final Logger logger = LoggerFactory.getLogger(MapUtilRebuildTest.class);
	private Region regionEuregio;
	private ServerInstance serverInstance;

	@Before
	public void setUp() throws Exception {
		serverInstance = new ServerInstance();
		serverInstance.setMapsPath("/tmp/bulletins/");
		serverInstance.setPdfDirectory("/tmp/bulletins/");
		serverInstance.setMapProductionUrl("../avalanche-warning-maps/");

		Region regionTyrol = Region.readRegion(Resources.getResource("region_AT-07.json"));
		Region regionSouthTyrol = Region.readRegion(Resources.getResource("region_IT-32-BZ.json"));
		Region regionTrentino = Region.readRegion(Resources.getResource("region_IT-32-TN.json"));
		regionEuregio = Region.readRegion(Resources.getResource("region_EUREGIO.json"));
		regionEuregio.addSubRegion(regionTyrol);
		regionEuregio.addSubRegion(regionSouthTyrol);
		regionEuregio.addSubRegion(regionTrentino);

		HibernateUtil.getInstance().setUp();
	}

	@Ignore
	@Test
	public void rebuildMaps() throws Exception {
		for (LocalDate date = LocalDate.parse("2022-01-20"); date.isBefore(LocalDate.parse("2022-05-10")); date = date.plusDays(1)) {
			rebuildMap(date);
		}
	}

	private void rebuildMap(LocalDate date) throws AlbinaException {
		logger.info("{}", date);
		Instant instant = ZonedDateTime.of(date.atTime(0, 0, 0), AlbinaUtil.localZone()).toInstant();
		List<AvalancheBulletin> result = AvalancheReportController.getInstance()
			.getPublishedBulletins(instant, RegionController.getInstance().getPublishBulletinRegions());
		AvalancheReport avalancheReport = AvalancheReport.of(result, regionEuregio, serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);
		PdfUtil.createRegionPdfs(avalancheReport);
	}
}

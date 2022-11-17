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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MapUtilRebuildTest {

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
		final ExecutorService hibernate = Executors.newSingleThreadExecutor();
		final ExecutorService render = Executors.newSingleThreadExecutor();
		CompletableFuture.allOf(
			Stream.iterate(LocalDate.parse("2022-01-20"), date -> date.plusDays(1))
				.limit(365)
				.filter(date -> date.isBefore(LocalDate.parse("2022-01-30")))
				.map(date -> CompletableFuture.supplyAsync(() -> fetch(date), hibernate).thenAcceptAsync(this::render, render))
				.toArray(CompletableFuture[]::new)
		).get();
	}

	private AvalancheReport fetch(LocalDate date) {
		try {
			Instant instant = ZonedDateTime.of(date.atTime(0, 0, 0), AlbinaUtil.localZone()).toInstant();
			List<AvalancheBulletin> bulletins = AvalancheReportController.getInstance().getPublishedBulletins(instant, RegionController.getInstance().getPublishBulletinRegions());
			return AvalancheReport.of(bulletins, regionEuregio, serverInstance);
		} catch (AlbinaException e) {
			throw new RuntimeException(e);
		}
	}

	private void render(AvalancheReport avalancheReport) {
		MapUtil.createMapyrusMaps(avalancheReport);
		PdfUtil.createRegionPdfs(avalancheReport);
	}
}

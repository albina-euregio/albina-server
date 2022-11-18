package eu.albina.map;

import com.google.common.io.Resources;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.util.PdfUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MapUtilRebuildTest {

	private List<Region> regions;
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
		Region regionEuregio = Region.readRegion(Resources.getResource("region_EUREGIO.json"));
		regionEuregio.addSubRegion(regionTyrol);
		regionEuregio.addSubRegion(regionSouthTyrol);
		regionEuregio.addSubRegion(regionTrentino);
		regions = Arrays.asList(regionEuregio, regionTyrol, regionSouthTyrol, regionTrentino);
	}

	@Ignore
	@Test
	public void rebuildMaps() throws Exception {
		final ExecutorService fetch = Executors.newSingleThreadExecutor();
		final ExecutorService render = Executors.newSingleThreadExecutor();
		CompletableFuture.allOf(
			Stream.iterate(LocalDate.parse("2022-01-20"), date -> date.plusDays(1))
				.limit(365)
				.filter(date -> date.isBefore(LocalDate.parse("2022-01-21")))
				.flatMap(date -> regions.stream().map(region ->
					CompletableFuture.supplyAsync(() -> fetch(date, region), fetch).thenAcceptAsync(this::render, render))
				)
				.toArray(CompletableFuture[]::new)
		).get();
	}

	private AvalancheReport fetch(LocalDate date, Region region) {
		try {
			URL url = new URL("https://static.avalanche.report/bulletins/" + date + "/avalanche_report.json");
			List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(url);
			return AvalancheReport.of(bulletins, region, serverInstance);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void render(AvalancheReport avalancheReport) {
		LoggerFactory.getLogger(getClass()).info("Rendering {}", avalancheReport.getPublicationTimeString());
		avalancheReport.setServerInstance(serverInstance);
		MapUtil.createMapyrusMaps(avalancheReport);
		PdfUtil.createRegionPdfs(avalancheReport);
	}
}

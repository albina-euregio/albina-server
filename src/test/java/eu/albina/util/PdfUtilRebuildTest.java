package eu.albina.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import eu.albina.RegionTestUtils;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.ServerInstance;
import eu.albina.model.enumerations.DaytimeDependency;
import eu.albina.model.enumerations.LanguageCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PdfUtilRebuildTest {
	private static final Logger logger = LoggerFactory.getLogger(PdfUtilRebuildTest.class);

	private List<Region> regions;
	private ServerInstance serverInstance;

	@BeforeEach
	public void setUp() throws Exception {
		serverInstance = new ServerInstance();
		serverInstance.setMapsPath("https://static.avalanche.report/bulletins");
		serverInstance.setPdfDirectory("/tmp/bulletins/");
		serverInstance.setMapProductionUrl("../avalanche-warning-maps/");

		Region regionTyrol = RegionTestUtils.readRegion(Resources.getResource("region_AT-07.json"));
		Region regionSouthTyrol = RegionTestUtils.readRegion(Resources.getResource("region_IT-32-BZ.json"));
		Region regionTrentino = RegionTestUtils.readRegion(Resources.getResource("region_IT-32-TN.json"));
		Region regionEuregio = RegionTestUtils.readRegion(Resources.getResource("region_EUREGIO.json"));
		regionEuregio.addSubRegion(regionTyrol);
		regionEuregio.addSubRegion(regionSouthTyrol);
		regionEuregio.addSubRegion(regionTrentino);
		regions = Arrays.asList(regionEuregio, regionTyrol, regionSouthTyrol, regionTrentino);
	}

	@Disabled
	@Test
	public void rebuildMaps() {
		Stream.iterate(LocalDate.parse("2022-01-20"), date -> date.plusDays(1))
			.limit(365)
			.filter(date -> date.isBefore(LocalDate.parse("2022-05-02")))
			.forEach(this::rebuildMaps);
	}

	private void rebuildMaps(LocalDate date) {
		List<AvalancheReport> reports = regions.stream().map(region -> fetch(date, region)).collect(Collectors.toList());
		reports.forEach(PdfUtilRebuildTest::createRegionPdfs);
	}

	private AvalancheReport fetch(LocalDate date, Region region) {
		try {
			URL url = new URL("https://static.avalanche.report/bulletins/" + date + "/avalanche_report.json");
			logger.info("Fetching bulletins from {}", url);
			List<AvalancheBulletin> bulletins = AvalancheBulletin.readBulletins(url);
			AvalancheReport avalancheReport = AvalancheReport.of(bulletins, region, serverInstance);
			avalancheReport.setServerInstance(serverInstance);
			return avalancheReport;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void createRegionPdfs(AvalancheReport avalancheReport) {
		for (LanguageCode lang : avalancheReport.getRegion().getEnabledLanguages()) {
			try {
				logger.info("Creating PDF for region {}, language {}", avalancheReport.getRegion().getId(), lang);
				new RebuildPdfUtil(avalancheReport, lang, false).createPdf();
				new RebuildPdfUtil(avalancheReport, lang, true).createPdf();
			} catch (IOException e) {
				logger.error("PDF could not be created", e);
			}
		}
	}

	static class RebuildPdfUtil extends PdfUtil{
		RebuildPdfUtil(AvalancheReport avalancheReport, LanguageCode lang, boolean grayscale) {
			super(avalancheReport, lang, grayscale);
		}

		@Override
		public Path getPath() {
			final Path path = super.getPath();
			final String filename = path.getFileName().toString().replace("_EUREGIO", "");
			return path.resolveSibling(filename);
		}

		@Override
		protected String getMapImage(DaytimeDependency daytimeDependency, AvalancheBulletin avalancheBulletin) throws MalformedURLException {
			return super.getMapImage(daytimeDependency, avalancheBulletin).replace(avalancheReport.getRegion().getId() + "_", "");
		}

		@Override
		protected String getMapImage(DaytimeDependency daytimeDependency) throws MalformedURLException {
			Region region = avalancheReport.getRegion();
			return super.getMapImage(daytimeDependency).replace(region.getId(), ImmutableMap.of(
				"EUREGIO", "albina",
				"AT-07", "tyrol",
				"IT-32-BZ", "southtyrol",
				"IT-32-TN", "trentino"
			).getOrDefault(region.getId(), region.getId()));
		}
	}

}

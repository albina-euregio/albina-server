package eu.albina.controller;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.albina.model.AvalancheReport;
import eu.albina.model.enumerations.BulletinStatus;

@MicronautTest
class AvalancheReportControllerTest {

	public static final List<AvalancheReport> AVALANCHE_REPORT = List.of(
		report("2026-02-20T16:00:00Z", "2026-02-20T15:31:43Z", 4),
		report("2026-02-20T16:00:00Z", "2026-02-20T15:30:47Z", 5),
		report("2026-02-20T16:00:00Z", "2026-02-20T12:00:00Z", -1),
		report("2026-02-19T16:00:00Z", "2026-02-20T06:40:07Z", 0),
		report("2026-02-19T16:00:00Z", "2026-02-20T06:21:11Z", 1),
		report("2026-02-19T16:00:00Z", "2026-02-20T06:21:08Z", 2),
		report("2026-02-19T16:00:00Z", "2026-02-20T06:18:14Z", 0),
		report("2026-02-19T16:00:00Z", "2026-02-20T06:04:50Z", 1),
		report("2026-02-19T16:00:00Z", "2026-02-20T06:02:21Z", 2),
		report("2026-02-19T16:00:00Z", "2026-02-19T16:00:00Z", 3),
		report("2026-02-19T16:00:00Z", "2026-02-19T15:22:00Z", 4),
		report("2026-02-19T16:00:00Z", "2026-02-19T15:20:56Z", 5)
	);

	private static AvalancheReport report(String date, String timestamp, int status) {
		AvalancheReport report = new AvalancheReport();
		report.setDate(ZonedDateTime.parse(date));
		report.setTimestamp(ZonedDateTime.parse(timestamp));
		if (status>=0) {
			report.setStatus(BulletinStatus.values()[status]);
		}
		return report;
	}

	@Test
	void getHighestStatus() {
		ArrayList<AvalancheReport> reports = new ArrayList<>(AVALANCHE_REPORT.subList(3, AVALANCHE_REPORT.size()));
		Collections.shuffle(reports);
		AvalancheReport expected = AVALANCHE_REPORT.get(3);
		Assertions.assertEquals(expected, AvalancheReportController.getHighestStatus(reports));
	}

	@Test
	void getHighestStatusMap() {
		ArrayList<AvalancheReport> reports = new ArrayList<>(AVALANCHE_REPORT);
		Collections.shuffle(reports);
		Map<Instant, AvalancheReport> expected = Map.of(
			Instant.parse("2026-02-20T16:00:00Z"), AVALANCHE_REPORT.get(0),
			Instant.parse("2026-02-19T16:00:00Z"), AVALANCHE_REPORT.get(3)
		);
		Assertions.assertEquals(expected, AvalancheReportController.getHighestStatusMap(reports));
	}
}

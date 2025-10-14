package eu.albina.controller;

import eu.albina.model.AvalancheReport;
import eu.albina.model.Region;
import eu.albina.model.enumerations.BulletinStatus;
import eu.albina.util.AlbinaUtil;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.repository.CrudRepository;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public interface AvalancheReportRepository extends CrudRepository<AvalancheReport, String> {

	@Join(value = "user", type = Join.Type.FETCH)
	List<AvalancheReport> findByDateAndRegion(ZonedDateTime date, Region region);

	default List<AvalancheReport> findByDateAndRegion(Instant date, Region region) {
		return findByDateAndRegion(AlbinaUtil.getZonedDateTimeUtc(date), region);
	}

	@Join(value = "user", type = Join.Type.FETCH)
	List<AvalancheReport> findByDateBetweenAndRegion(ZonedDateTime startDate, ZonedDateTime endDate, Region region);

	default List<AvalancheReport> findByDateBetweenAndRegion(Instant startDate, Instant endDate, Region region) {
		return findByDateBetweenAndRegion(AlbinaUtil.getZonedDateTimeUtc(startDate), AlbinaUtil.getZonedDateTimeUtc(endDate), region);
	}

	AvalancheReport findFirstByStatusInOrderByDateDesc(Set<BulletinStatus> status);

}

package eu.albina.controller;

import eu.albina.model.AvalancheBulletin;
import eu.albina.util.AlbinaUtil;
import io.micronaut.data.repository.CrudRepository;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

public interface AvalancheBulletinRepository extends CrudRepository<AvalancheBulletin, String> {
	List<AvalancheBulletin> findByValidFromOrValidUntil(ZonedDateTime startDate, ZonedDateTime endDate);

	default List<AvalancheBulletin> findByValidFromOrValidUntil(Instant startDate, Instant endDate) {
		return findByValidFromOrValidUntil(AlbinaUtil.getZonedDateTimeUtc(startDate), AlbinaUtil.getZonedDateTimeUtc(endDate));
	}
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.DangerSource;
import io.micronaut.data.annotation.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DangerSourceRepository extends CrudRepository<DangerSource, String> {
	List<DangerSource> findByCreationDateBetweenAndOwnerRegion(Instant startDate, Instant endDate, String region);
}

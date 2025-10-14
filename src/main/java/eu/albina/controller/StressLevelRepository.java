// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.StressLevel;
import eu.albina.model.StressLevel.StressLevelID;
import eu.albina.model.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.hibernate.HibernateException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface StressLevelRepository extends CrudRepository<StressLevel, StressLevel.StressLevelID> {

	List<StressLevel> findByUserInAndDateBetween(Collection<User> users, LocalDate startDate, LocalDate endDate);

	default StressLevel updateOrSave(StressLevel stressLevel) throws HibernateException {
		StressLevelID id = new StressLevelID(stressLevel.getUser(), stressLevel.getDate());
		StressLevel existing = findById(id).orElse(null);
		if (existing != null) {
			existing.setStressLevel(stressLevel.getStressLevel());
			existing.setLastUpdated(Instant.now());
			update(existing);
		} else {
			stressLevel.setLastUpdated(Instant.now());
			save(stressLevel);
		}
		return stressLevel;
	}
}

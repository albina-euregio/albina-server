/*******************************************************************************
 * Copyright (C) 2021 albina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.controller;

import eu.albina.model.StressLevel;
import eu.albina.model.StressLevel.StressLevelID;
import eu.albina.model.User;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StressLevelController {

	static List<StressLevel> get(Collection<User> users, LocalDate startDate, LocalDate endDate) {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<StressLevel> select = criteriaBuilder.createQuery(StressLevel.class);
			Root<StressLevel> root = select.from(StressLevel.class);
			select.where(
				root.get("user").in(users),
				criteriaBuilder.between(root.get("date"), startDate, endDate)
			);
			return entityManager.createQuery(select).getResultList();
		});
	}

	static StressLevel create(StressLevel stressLevel) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			StressLevelID id = new StressLevelID(stressLevel.getUser(), stressLevel.getDate());
			StressLevel existing = entityManager.find(StressLevel.class, id);
			if (existing != null) {
				existing.setStressLevel(stressLevel.getStressLevel());
				existing.setLastUpdated(Instant.now());
				entityManager.merge(existing);
			} else {
				stressLevel.setUser(entityManager.find(User.class, stressLevel.getUser().getEmail()));
				stressLevel.setLastUpdated(Instant.now());
				entityManager.persist(stressLevel);
			}
			return stressLevel;
		});
	}
}

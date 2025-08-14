// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;

import eu.albina.model.StressLevel;
import eu.albina.model.StressLevel.StressLevelID;
import eu.albina.model.User;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public interface StressLevelController {

	static List<StressLevel> get(Collection<User> users, LocalDate startDate, LocalDate endDate) {
		return HibernateUtil.getInstance().run(entityManager -> {
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

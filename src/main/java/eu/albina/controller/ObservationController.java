/*******************************************************************************
 * Copyright (C) 2021 albina-euregio
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

import eu.albina.model.Observation;
import eu.albina.util.HibernateUtil;
import org.hibernate.HibernateException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.util.List;

public interface ObservationController {

	static Long save(Observation observation) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(observation);
			return observation.getId();
		});
	}

	static Observation get(long id) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager ->
			entityManager.find(Observation.class, id));
	}

	static List<Observation> get(OffsetDateTime startDate, OffsetDateTime endDate) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Observation> select = criteriaBuilder.createQuery(Observation.class);
			Root<Observation> root = select.from(Observation.class);
			select.where(criteriaBuilder.between(root.get("eventDate"), startDate, endDate));
			return entityManager.createQuery(select).getResultList();
		});
	}
}

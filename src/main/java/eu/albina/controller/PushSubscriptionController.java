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

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;

public interface PushSubscriptionController {

	static Long create(PushSubscription subscription) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(subscription);
			return subscription.getId();
		});
	}

	static List<PushSubscription> get(LanguageCode lang, Collection<String> regions) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PushSubscription> select = criteriaBuilder.createQuery(PushSubscription.class);
			Root<PushSubscription> root = select.from(PushSubscription.class);
			select.where(
				criteriaBuilder.equal(root.get("language"), lang),
				root.get("region").in(regions));
			return entityManager.createQuery(select).getResultList();
		});
	}

	static void delete(PushSubscription subscription) throws HibernateException {
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaDelete<PushSubscription> delete = criteriaBuilder.createCriteriaDelete(PushSubscription.class);
			Root<PushSubscription> root = delete.from(PushSubscription.class);
			delete.where(criteriaBuilder.equal(root.get("endpoint"), subscription.getEndpoint()));
			delete.where(criteriaBuilder.equal(root.get("auth"), subscription.getAuth()));
			delete.where(criteriaBuilder.equal(root.get("p256dh"), subscription.getP256dh()));
			entityManager.createQuery(delete).executeUpdate();
			return null;
		});
	}

	static void incrementFailedCount(PushSubscription subscription) throws HibernateException {
		subscription.setFailedCount(subscription.getFailedCount() + 1);
		HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.merge(subscription);
			return null;
		});
	}
}

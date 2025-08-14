// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public interface PushSubscriptionController {

	static Long create(PushSubscription subscription) throws HibernateException {
		return HibernateUtil.getInstance().runTransaction(entityManager -> {
			entityManager.persist(subscription);
			return subscription.getId();
		});
	}

	static List<PushSubscription> get(LanguageCode lang, Collection<String> regionIds) throws HibernateException {
		return HibernateUtil.getInstance().run(entityManager -> {
			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<PushSubscription> select = criteriaBuilder.createQuery(PushSubscription.class);
			Root<PushSubscription> root = select.from(PushSubscription.class);
			select.where(
				criteriaBuilder.equal(root.get("language"), lang),
				root.get("region").in(regionIds));
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

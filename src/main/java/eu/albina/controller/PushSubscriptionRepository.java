// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends CrudRepository<PushSubscription, Long> {

	List<PushSubscription> findByLanguageAndRegionInList(LanguageCode lang, Collection<String> regionIds);

	Optional<PushSubscription> findByEndpointAndAuthAndP256dh(String endpoint, String auth, String p256dh);

	default void incrementFailedCount(PushSubscription subscription) throws HibernateException {
		subscription.setFailedCount(subscription.getFailedCount() + 1);
		update(subscription);
	}

}

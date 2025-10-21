// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.Subscriber;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface SubscriberRepository extends CrudRepository<Subscriber, String> {
}

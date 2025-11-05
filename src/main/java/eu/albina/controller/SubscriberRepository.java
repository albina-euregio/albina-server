// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.Subscriber;
import io.micronaut.data.annotation.Repository;

@Repository
public interface SubscriberRepository extends CrudRepository<Subscriber, String> {
}

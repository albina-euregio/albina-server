// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.ServerInstance;
import io.micronaut.data.annotation.Repository;

import java.util.List;

@Repository
public interface ServerInstanceRepository extends CrudRepository<ServerInstance, Long> {
	List<ServerInstance> findByExternalServerTrue();

	default List<ServerInstance> getExternalServerInstances() {
		return findByExternalServerTrue();
	}

}

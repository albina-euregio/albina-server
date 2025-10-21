// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.ServerInstance;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface ServerInstanceRepository extends CrudRepository<ServerInstance, Long> {
	ServerInstance findByExternalServerFalse();

	default ServerInstance getLocalServerInstance() {
		return findByExternalServerFalse();
	}

	List<ServerInstance> findByExternalServerTrue();

	default List<ServerInstance> getExternalServerInstances() {
		return findByExternalServerTrue();
	}

}

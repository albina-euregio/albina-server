// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.exception.AlbinaException;
import eu.albina.model.Region;
import io.micronaut.data.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends CrudRepository<Region, String> {

	default List<Region> getPublishBulletinRegions() {
		return findAll().stream().filter(Region::isPublishBulletins).toList();
	}

	default List<Region> getPublishBlogRegions() {
		return findAll().stream().filter(Region::isPublishBlogs).toList();
	}

	default List<Region> getRegionsOrBulletinRegions(List<String> regionIds) {
		if (regionIds.isEmpty()) {
			return getPublishBulletinRegions();
		}
		return regionIds.stream().map(this::findById).map(Optional::orElseThrow).toList();
	}

	default Region findByIdOrElseThrow(String id) throws AlbinaException {
		return findById(id).orElseThrow(() -> new AlbinaException("No region with id: " + id));
	}
}

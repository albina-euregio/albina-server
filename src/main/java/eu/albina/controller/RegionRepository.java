// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.Region;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface RegionRepository extends CrudRepository<Region, String> {

	default List<Region> getRegions() {
		return findAll().stream().filter(region -> !region.getServerInstance().isExternalServer()).collect(Collectors.toList());
	}

	default List<Region> getPublishBulletinRegions() {
		return findAll().stream().filter(region -> !region.getServerInstance().isExternalServer() && region.isPublishBulletins()).collect(Collectors.toList());
	}

	default List<Region> getPublishBlogRegions() {
		return findAll().stream().filter(region -> !region.getServerInstance().isExternalServer() && region.isPublishBlogs()).collect(Collectors.toList());
	}

	default List<Region> getRegionsOrBulletinRegions(List<String> regionIds) {
		if (regionIds.isEmpty()) {
			return getPublishBulletinRegions();
		}
		return regionIds.stream().map(this::findById).map(Optional::orElseThrow).toList();
	}
}

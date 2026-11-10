// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.albina.model.Region;

class RegionRepositoryTest {

	@Test
	void getRegionsOrBulletinRegions() {
		Region region = new Region("AT-07");
		RegionRepository repository = mock(RegionRepository.class, CALLS_REAL_METHODS);
		doReturn(Optional.of(region)).when(repository).findById("AT-07");
		doReturn(Optional.empty()).when(repository).findById("XX-99");

		Assertions.assertEquals(List.of(region), repository.getRegionsOrBulletinRegions(List.of("AT-07")));
		HttpStatusException e = Assertions.assertThrows(HttpStatusException.class,
			() -> repository.getRegionsOrBulletinRegions(List.of("AT-07", "XX-99")));
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		Assertions.assertEquals("No region with id: XX-99", e.getMessage());
	}
}

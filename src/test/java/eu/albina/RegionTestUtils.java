// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Resources;

import eu.albina.model.Region;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RegionTestUtils {

	@Inject
	ObjectMapper objectMapper;

	public Region regionEuregio() {
		return readRegion(Resources.getResource("region_EUREGIO.json"));
	}

	public Region regionTyrol() {
		return readRegion(Resources.getResource("region_AT-07.json"));
	}

	public Region regionCarinthia() {
		return readRegion(Resources.getResource("region_AT-02.json"));
	}

	public Region regionSouthTyrol() {
		return readRegion(Resources.getResource("region_IT-32-BZ.json"));
	}

	public Region regionTrentino() {
		return readRegion(Resources.getResource("region_IT-32-TN.json"));
	}

	public Region regionAran() {
		return readRegion(Resources.getResource("region_ES-CT-L.json"));
	}

	public Region readRegion(URL resource) throws UncheckedIOException {
		try {
			String json = Resources.toString(resource, StandardCharsets.UTF_8);
			return objectMapper.readValue(json, Region.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

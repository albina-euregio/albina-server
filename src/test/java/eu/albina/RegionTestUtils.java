// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Resources;

import eu.albina.model.Region;

public interface RegionTestUtils {

	Region regionEuregio = readRegion(Resources.getResource("region_EUREGIO.json"));
	Region regionTyrol = readRegion(Resources.getResource("region_AT-07.json"));
	Region regionSouthTyrol = readRegion(Resources.getResource("region_IT-32-BZ.json"));
	Region regionTrentino = readRegion(Resources.getResource("region_IT-32-TN.json"));
	Region regionAran = readRegion(Resources.getResource("region_ES-CT-L.json"));

	static Region readRegion(URL resource) throws UncheckedIOException {
		try {
			return new Region(Resources.toString(resource, StandardCharsets.UTF_8), Region::new);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}

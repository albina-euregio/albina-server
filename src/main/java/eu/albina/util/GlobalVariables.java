// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Singleton
public class GlobalVariables {

	@Value("${albina.conf.git.version}")
	public String version;

}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;

@Serdeable
public class DangerSourceVariantsStatus {
	public Instant date;
	public boolean forecast;
	public boolean analysis;
}

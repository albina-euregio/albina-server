// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.Instant;

import eu.albina.model.enumerations.BulletinStatus;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record BulletinUpdate(String region, Instant date, BulletinStatus status) {
}

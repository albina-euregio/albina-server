/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package eu.albina.model;

import eu.albina.model.enumerations.BulletinStatus;
import io.micronaut.core.annotation.Introspected;

import java.time.ZonedDateTime;

/**
 * DTO Projection for {@link eu.albina.model.AvalancheReport}
 */
@Introspected
public record AvalancheReportStatus(ZonedDateTime date, ZonedDateTime timestamp, BulletinStatus status) {
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package org.caaml.v6;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Expected avalanche type.
 */
@Serdeable
public enum AvalancheTypeType {
    slab, loose, glide;

}

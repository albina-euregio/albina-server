// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.DangerSourceVariantText;
import eu.albina.model.enumerations.AvalancheType;
import io.micronaut.data.annotation.Repository;

import java.util.List;

@Repository
public interface DangerSourceVariantTextRepository extends CrudRepository<DangerSourceVariantText, String> {
	/**
	 * Returns all danger source variant texts for the given {@code avalanche type}.
	 *
	 * @param avalancheType
	 *                      the type of avalanche
	 * @return all danger source variant texts for the given avalanche type
	 */
	List<DangerSourceVariantText> findByAvalancheType(AvalancheType avalancheType);
}

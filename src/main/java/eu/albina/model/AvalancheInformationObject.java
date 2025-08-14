// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.github.openjson.JSONObject;

public interface AvalancheInformationObject {

	/**
	 * This method serializes the object to JSON.
	 *
	 * @return Returns a string in JSON format representing the object.
	 */
	public abstract JSONObject toJSON();

}

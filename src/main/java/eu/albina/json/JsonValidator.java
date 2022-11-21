/*******************************************************************************
 * Copyright (C) 2019 Norbert Lanzanasto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package eu.albina.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * Provide static helper methods to validate against different JSON schema
 * files.
 *
 * @author Norbert Lanzanasto
 *
 */
public class JsonValidator {

	public static Set<ValidationMessage> validateCAAMLv6(String caaml) {
		try {
			JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
			JsonSchema jsonSchema1 = jsonSchemaFactory.getSchema(Resources.getResource("CAAMLv6_BulletinEAWS.json").openStream());
			return jsonSchema1.validate(new ObjectMapper().readTree(caaml));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * Validates a JSON string against the avalanche bulletin JSON schema.
	 *
	 * @param avalancheBulletin
	 *            The string representing the avalanche bulletin in JSON format.
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	public static Set<ValidationMessage> validateAvalancheBulletin(String avalancheBulletin) {
		return validate(avalancheBulletin, Resources.getResource("avalancheBulletin.json"));
	}

	/**
	 * Validates a JSON string against the avalanche incident JSON schema.
	 *
	 * @param avalancheIncident
	 *            The string representing the avalanche incident in JSON format.
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	public static Set<ValidationMessage> validateAvalancheIncident(String avalancheIncident) {
		return validate(avalancheIncident, Resources.getResource("avalancheIncident.json"));
	}

	/**
	 * Validates a JSON string against the snow profile JSON schema.
	 *
	 * @param snowProfile
	 *            The string representing the snow profile in JSON format.
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	public static Set<ValidationMessage> validateSnowProfile(String snowProfile) {
		return validate(snowProfile, Resources.getResource("snowProfile.json"));
	}

	/**
	 * Validates a JSON string against the news JSON schema.
	 *
	 * @param news
	 *            The string representing the news in JSON format.
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	public static Set<ValidationMessage> validateNews(String news) {
		return validate(news, Resources.getResource("news.json"));
	}

	/**
	 * Validates a JSON string against an JSON Schema.
	 *
	 * @param jsonData
	 *            The JSON string to be validated.
	 * @param jsonSchema
	 *            The type of the schema (snowProfile, avalancheBulletin or
	 *            avalancheIncident).
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	private static Set<ValidationMessage> validate(String jsonData, URL jsonSchema) {
		try {
			JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
			JsonSchema jsonSchema1 = jsonSchemaFactory.getSchema(jsonSchema.openStream());
			return jsonSchema1.validate(new ObjectMapper().readTree(jsonData));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}

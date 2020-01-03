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
import java.net.URL;
import java.util.Iterator;

import com.google.common.io.Resources;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * Provide static helper methods to validate against different JSON schema
 * files.
 *
 * @author Norbert Lanzanasto
 *
 */
public class JsonValidator {

	private static Logger logger = LoggerFactory.getLogger(JsonValidator.class);

	/**
	 * Validates a JSON string against the avalanche bulletin JSON schema.
	 *
	 * @param avalancheBulletin
	 *            The string representing the avalanche bulletin in JSON format.
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	public static JSONObject validateAvalancheBulletin(String avalancheBulletin) {
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
	public static JSONObject validateAvalancheIncident(String avalancheIncident) {
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
	public static JSONObject validateSnowProfile(String snowProfile) {
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
	public static JSONObject validateNews(String news) {
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
	private static JSONObject validate(String jsonData, URL jsonSchema) {
		JSONObject result = new JSONObject();
		ProcessingReport report = null;
		try {
			logger.debug("Applying schema: @<@<" + jsonSchema + ">@>@ to data: #<#<" + jsonData + ">#>#");
			JsonNode schemaNode = JsonLoader.fromURL(jsonSchema);
			JsonNode data = JsonLoader.fromString(jsonData);
			JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
			JsonSchema schema = factory.getJsonSchema(schemaNode);
			report = schema.validate(data);

			if (report != null && !report.isSuccess()) {
				JSONArray errors = new JSONArray();
				Iterator<ProcessingMessage> iter = report.iterator();
				while (iter.hasNext()) {
					ProcessingMessage pm = iter.next();
					errors.put(pm.getMessage());
					logger.error("Processing Message: " + pm.getMessage());
				}
				result.append("errors", errors);
			}
		} catch (IOException | ProcessingException ex) {
			result.put("message", ex.getMessage());
			logger.error(ex.getMessage(), ex);
			return result;
		}

		return result;
	}
}

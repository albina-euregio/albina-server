package org.avalanches.albina.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.avalanches.albina.util.GlobalVariables;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

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
		return validate(avalancheBulletin, "avalancheBulletin");
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
		return validate(avalancheIncident, "avalancheIncident");
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
		return validate(snowProfile, "snowProfile");
	}

	/**
	 * Validates a JSON string against an JSON Schema.
	 * 
	 * @param jsonData
	 *            The JSON string to be validated.
	 * @param type
	 *            The type of the schema (snowProfile, avalancheBulletin or
	 *            avalancheIncident).
	 * @return Returns a JSON object containing the error messages (or empty for
	 *         success).
	 */
	private static JSONObject validate(String jsonData, String type) {
		JSONObject result = new JSONObject();
		ProcessingReport report = null;
		try {
			String jsonSchema = GlobalVariables.getFileString(type);

			logger.debug("Applying schema: @<@<" + jsonSchema + ">@>@ to data: #<#<" + jsonData + ">#>#");
			JsonNode schemaNode = JsonLoader.fromString(jsonSchema);
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
		} catch (JsonParseException jpex) {
			result.put("message", jpex.getMessage());
			logger.error("JsonParseException: " + jpex.getMessage());
			return result;
		} catch (ProcessingException pex) {
			result.put("message", pex.getMessage());
			logger.error("ProcessingException: " + pex.getMessage());
			return result;
		} catch (FileNotFoundException fnfe) {
			result.put("message", fnfe.getMessage());
			logger.error("FileNotFoundException: " + fnfe.getMessage());
			return result;
		} catch (IOException e) {
			result.put("message", e.getMessage());
			logger.error("IOException: " + e.getMessage());
			return result;
		}

		return result;
	}

}

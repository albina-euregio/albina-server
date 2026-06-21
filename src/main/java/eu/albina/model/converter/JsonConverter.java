// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores a JSON object as a string in the database and exposes it as a parsed
 * {@link com.fasterxml.jackson.databind.JsonNode} (via {@link ObjectMapper#readTree}).
 */
@Converter
public class JsonConverter implements AttributeConverter<Object, String> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Object attribute) {
		if (attribute == null) return null;
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to serialize JSON column", e);
		}
	}

	@Override
	public Object convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) return null;
		try {
			return objectMapper.readTree(dbData);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to parse JSON column", e);
		}
	}
}

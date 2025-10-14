// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

public class JsonUtil {

	public interface Views {
		class Public {
		}

		class Internal {
		}
	}

	// When fetching an entity lazily, Hibernate returns a proxy object instead of the real entity, this causes problems when serializing.
	// Moreover transient properties should never be serialized.
	public static final Hibernate6Module hbm = new Hibernate6Module()
		.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false)
		.configure(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION, true);

	public static final ObjectMapper ALBINA_OBJECT_MAPPER = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.registerModule(hbm)
		.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
		.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
		.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public static <T> T parseUsingJackson(String json, Class<T> valueType) {
		try {
			return ALBINA_OBJECT_MAPPER.readValue(json, valueType);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String writeValueUsingJackson(Object value) {
		try {
			return ALBINA_OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String writeValueUsingJackson(Object value, Class<?> view) {
		try {
			return ALBINA_OBJECT_MAPPER.writerWithView(view).writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}

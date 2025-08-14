// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model.converter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;

public abstract class EnumSetToStringConverter<E extends Enum<E>> implements AttributeConverter<Set<E>, String> {

	protected abstract Class<E> getEnumClass();

	@Override
	public String convertToDatabaseColumn(Set<E> attribute) {
		if (attribute == null || attribute.isEmpty()) return null;
		return attribute.stream()
			.map(Enum::name)
			.collect(Collectors.joining(","));
	}

	@Override
	public Set<E> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) return Set.of();
		return Arrays.stream(dbData.split(","))
			.map(String::trim)
			.map(name -> Enum.valueOf(getEnumClass(), name))
			.collect(Collectors.toSet());
	}
}


package eu.albina.controller;

import java.util.function.Function;

public interface CrudRepository<E, ID> extends io.micronaut.data.repository.CrudRepository<E, ID> {

	default E saveOrUpdate(E entity, Function<E, ID> idFunction) {
		ID id = idFunction.apply(entity);
		if (id == null || !existsById(id)) {
			return save(entity);
		} else {
			return update(entity);
		}

	}
}

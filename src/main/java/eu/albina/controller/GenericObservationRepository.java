package eu.albina.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.micronaut.data.annotation.Repository;

import eu.albina.model.GenericObservation;
import eu.albina.model.GenericObservation.GenericObservationId;

@Repository
public interface GenericObservationRepository extends CrudRepository<GenericObservation, GenericObservationId> {
	List<GenericObservation> findByEventDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}

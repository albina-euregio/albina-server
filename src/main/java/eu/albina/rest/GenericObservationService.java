package eu.albina.rest;

import java.util.List;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import eu.albina.controller.GenericObservationRepository;
import eu.albina.model.GenericObservation;
import eu.albina.model.enumerations.Role;

@Controller("/observations")
@Tag(name = "observations")
public class GenericObservationService {

	@Inject
	GenericObservationRepository genericObservationRepository;

	@Get
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public List<GenericObservation> getGenericObservations(
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String start,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String end) {

		return genericObservationRepository.findByEventDateBetween(DateControllerUtil.parseDateOrNull(start), DateControllerUtil.parseDateOrNull(end));
	}

	@Get("/:source/:id")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public @NonNull GenericObservation getGenericObservation(String source, String id) {

		return genericObservationRepository.findById(new GenericObservation.GenericObservationId(source, id)).orElseThrow();
	}

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void saveOrUpdateGenericObservation(@Body GenericObservation genericObservation) {

		genericObservationRepository.saveOrUpdate(genericObservation, GenericObservation::getId);
	}

	@Delete
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	public void deleteGenericObservation(@Body GenericObservation genericObservation) {

		genericObservationRepository.delete(genericObservation);
	}
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import eu.albina.controller.CrudRepository;
import eu.albina.controller.RegionRepository;
import eu.albina.model.Incident;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Role;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.data.annotation.Repository;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;

@Controller("/incidents")
@Tag(name = "incidents")
@Transactional
public class IncidentService {

	private static final Logger logger = LoggerFactory.getLogger(IncidentService.class);

	@Repository
	public interface IncidentRepository extends CrudRepository<Incident, String> {
		List<Incident> findByRegionId(String regionId);
	}

	@Inject
	IncidentRepository incidentRepository;

	@Inject
	RegionRepository regionRepository;

	@Inject
	ObjectMapper objectMapper;

	@Get
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List incidents for a region")
	public List<IncidentView> getIncidents(@QueryValue("region") String regionId) {
		return incidentRepository.findByRegionId(regionId).stream()
			.map(i -> IncidentView.of(i, objectMapper))
			.toList();
	}

	@Get("/{id}")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get an incident by ID")
	public IncidentView getIncident(@PathVariable String id) {
		return incidentRepository.findById(id)
			.map(i -> IncidentView.of(i, objectMapper))
			.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
	}

	@Post
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create an incident")
	public HttpResponse<IncidentView> createIncident(
		@QueryValue("region") String regionId,
		@Body String body) {
		try {
			objectMapper.readTree(body); // validate JSON
			Region region = regionRepository.findById(regionId)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No region with id: " + regionId));
			Incident incident = new Incident();
			incident.setRegion(region);
			incident.setData(body);
			return HttpResponse.created(IncidentView.of(incidentRepository.save(incident), objectMapper));
		} catch (IOException e) {
			logger.warn("Invalid JSON body for incident", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Put("/{id}")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update an incident")
	public IncidentView updateIncident(@PathVariable String id, @Body String body) {
		try {
			objectMapper.readTree(body); // validate JSON
			Incident incident = incidentRepository.findById(id)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
			incident.setData(body);
			return IncidentView.of(incidentRepository.update(incident), objectMapper);
		} catch (IOException e) {
			logger.warn("Invalid JSON body for incident", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Delete("/{id}")
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete an incident")
	public HttpResponse<Void> deleteIncident(@PathVariable String id) {
		if (!incidentRepository.existsById(id)) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id);
		}
		incidentRepository.deleteById(id);
		return HttpResponse.noContent();
	}

	@Serdeable
	public record IncidentView(String id, String regionId, Instant createdAt, Instant updatedAt, JsonNode data) {
		static IncidentView of(Incident i, ObjectMapper objectMapper) {
			try {
				return new IncidentView(i.getId(), i.getRegion().getId(), i.getCreatedAt(), i.getUpdatedAt(),
					objectMapper.readTree(i.getData()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}

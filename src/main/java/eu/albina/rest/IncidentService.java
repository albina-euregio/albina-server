// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import io.swagger.v3.oas.annotations.Parameter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.SystemFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import eu.albina.controller.CrudRepository;
import eu.albina.controller.RegionRepository;
import eu.albina.model.Incident;
import eu.albina.model.IncidentAttachment;
import eu.albina.model.Region;
import eu.albina.model.enumerations.Role;
import eu.albina.util.GlobalVariables;

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
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller("/incidents")
@Tag(name = "incidents")
@Transactional
public class IncidentService {

	private static final Logger logger = LoggerFactory.getLogger(IncidentService.class);

	@Repository
	public interface IncidentRepository extends CrudRepository<Incident, String> {
		List<Incident> findByRegionIdAndDateTimeBetween(String regionId, String startInstant, String endInstant);
	}

	@Inject
	IncidentRepository incidentRepository;

	@Inject
	RegionRepository regionRepository;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	GlobalVariables globalVariables;

	@Get
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List incidents for a region")
	public List<Incident> getIncidents(
		@QueryValue("region") String regionId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate
	) {
		String startInstant = DateControllerUtil.parseDate(startDate).toString();
		String endInstant = DateControllerUtil.parseDate(endDate).toString();
		return incidentRepository.findByRegionIdAndDateTimeBetween(regionId, startInstant, endInstant);
	}

	@Get("/public")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "List published incidents for a region (public report)")
	public List<Object> getPublicIncidents(
		@QueryValue("region") String regionId,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("startDate") String startDate,
		@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("endDate") String endDate
	) {
		String startInstant = DateControllerUtil.parseDate(startDate).toString();
		String endInstant = DateControllerUtil.parseDate(endDate).toString();
		return incidentRepository.findByRegionIdAndDateTimeBetween(regionId, startInstant, endInstant)
			.stream()
			.map(Incident::getPublicData)
			.filter(Objects::nonNull)
			.toList();
	}

	@Get("/{id}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get an incident by ID")
	public Incident getIncident(@PathVariable UUID id) {
		return incidentRepository.findById(id.toString())
			.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
	}

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Create an incident")
	public HttpResponse<Incident> createIncident(
		@QueryValue("region") String regionId,
		@Body String body) {
		try {
			JsonNode data = objectMapper.readTree(body);
			Region region = regionRepository.findById(regionId)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No region with id: " + regionId));
			Incident incident = new Incident();
			incident.setRegion(region);
			incident.setData(data);
			return HttpResponse.created(incidentRepository.save(incident));
		} catch (JacksonException e) {
			logger.warn("Invalid JSON body for incident", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Put("/{id}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update an incident")
	public Incident updateIncident(@PathVariable UUID id, @Body String body) {
		try {
			JsonNode data = objectMapper.readTree(body);
			Incident incident = incidentRepository.findById(id.toString())
				.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
			incident.setData(data);
			return incidentRepository.update(incident);
		} catch (JacksonException e) {
			logger.warn("Invalid JSON body for incident", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/{id}/publish")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Publish an incident")
	public Incident publishIncident(@PathVariable UUID id, @Body String body) {
		try {
			JsonNode publicData = objectMapper.readTree(body);
			Incident incident = incidentRepository.findById(id.toString())
				.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
			incident.setPublishedAt(Instant.now().truncatedTo(ChronoUnit.SECONDS));
			incident.setPublicData(publicData);
			return incidentRepository.update(incident);
		} catch (JacksonException e) {
			logger.warn("Invalid JSON body for incident", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Delete("/{id}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete an incident")
	public HttpResponse<Void> deleteIncident(@PathVariable UUID id) {
		if (!incidentRepository.existsById(id.toString())) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id);
		}
		incidentRepository.deleteById(id.toString());
		return HttpResponse.noContent();
	}

	@Get("/{id}/attachment/{attachmentId}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get incident attachment")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ExecuteOn(TaskExecutors.IO)
	public SystemFile getIncidentAttachment(@PathVariable UUID id, @PathVariable UUID attachmentId) {
		if (!incidentRepository.existsById(id.toString())) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id);
		}
		Path attachment = getAttachmentPath(id, attachmentId);
		return new SystemFile(attachment.toFile(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
	}

	@Delete("/{id}/attachment/{attachmentId}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Delete incident attachment")
	@ExecuteOn(TaskExecutors.IO)
	public HttpResponse<Void> deleteIncidentAttachment(@PathVariable UUID id, @PathVariable UUID attachmentId) {
		if (!incidentRepository.existsById(id.toString())) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id);
		}
		Path attachment = getAttachmentPath(id, attachmentId);
		try {
			Files.delete(attachment);
			logger.info("Deleted attachment {} for incident {}", attachmentId, id);
			return HttpResponse.noContent();
		} catch (IOException e) {
			logger.warn("Failed to delete incident attachment", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post("/{id}/attachment")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Upload incident attachment")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ExecuteOn(TaskExecutors.IO)
	public IncidentAttachment uploadIncidentAttachment(
		@PathVariable UUID id,
		@Part("file") CompletedFileUpload file) {
		if (!incidentRepository.existsById(id.toString())) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id);
		}
		UUID uuid = UUID.randomUUID();
		try {
			Path attachment = getAttachmentPath(id, uuid);
			Files.createDirectories(attachment.getParent());
			Files.copy(file.getInputStream(), attachment, StandardCopyOption.REPLACE_EXISTING);
			logger.info("Uploaded attachment {} for incident {} to {} ({} bytes)",
				file.getFilename(), id, attachment, Files.size(attachment));
			String mediaType = file.getContentType().map(MediaType::getName).orElse(null);
			return new IncidentAttachment(uuid, Instant.now(), file.getFilename(), mediaType);
		} catch (IOException e) {
			logger.warn("Failed to save incident attachment", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private Path getAttachmentPath(UUID id, UUID attachmentId) {
		return Path.of(globalVariables.getIncidentsPath()).resolve(id.toString()).resolve(attachmentId.toString());
	}
}

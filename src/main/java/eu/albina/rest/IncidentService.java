// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.SystemFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

	@Inject
	GlobalVariables globalVariables;

	@Get
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "List incidents for a region")
	public List<IncidentView> getIncidents(@QueryValue("region") String regionId) {
		return incidentRepository.findByRegionId(regionId).stream()
			.map(i -> IncidentView.of(i, objectMapper))
			.toList();
	}

	@Get("/{id}")
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER, Role.Str.FOREMAN, Role.Str.OBSERVER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get an incident by ID")
	public IncidentView getIncident(@PathVariable UUID id) {
		return incidentRepository.findById(id.toString())
			.map(i -> IncidentView.of(i, objectMapper))
			.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
	}

	@Post
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
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
	@Secured({Role.Str.ADMIN, Role.Str.FORECASTER})
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Update an incident")
	public IncidentView updateIncident(@PathVariable UUID id, @Body String body) {
		try {
			objectMapper.readTree(body); // validate JSON
			Incident incident = incidentRepository.findById(id.toString())
				.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No incident with id: " + id));
			incident.setData(body);
			return IncidentView.of(incidentRepository.update(incident), objectMapper);
		} catch (IOException e) {
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

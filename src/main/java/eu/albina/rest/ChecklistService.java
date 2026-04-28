// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.security.Principal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.albina.controller.CrudRepository;
import eu.albina.controller.RegionRepository;
import eu.albina.controller.UserRepository;
import eu.albina.exception.AlbinaException;
import eu.albina.model.PublicationChecklist;
import eu.albina.model.PublicationChecklistItem;
import eu.albina.model.Region;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import io.micronaut.data.annotation.Repository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Controller("/checklist")
@Tag(name = "checklist")
public class ChecklistService {

	private static final Logger logger = LoggerFactory.getLogger(ChecklistService.class);

	@Inject
	private UserRepository userRepository;

	@Inject
	private RegionRepository regionRepository;

	@Inject
	private PublicationChecklistRepository publicationChecklistRepository;

	@Repository
	public interface PublicationChecklistRepository extends CrudRepository<PublicationChecklist, String> {
		List<PublicationChecklist> findByDateAndRegionOrderByTimestampDesc(ZonedDateTime date, Region region);
	}

	@Serdeable
	public record Checklist(String checklistId, Instant timestamp, List<PublicationChecklistItem> checklistItems,
			String user) {
	}

	@Get
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get checklists for a given date and region")
	public List<Checklist> getChecklists(
			Principal principal,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
			@QueryValue("region") String regionId) {

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (!user.hasPermissionForRegion(region.getId())) {
				throw new AlbinaException("User is not authorized for this region!");
			}

			ZonedDateTime checklistDate = startDate.atZone(ZoneOffset.UTC);
			return publicationChecklistRepository.findByDateAndRegionOrderByTimestampDesc(checklistDate, region)
					.stream()
					.map(this::toChecklist)
					.toList();
		} catch (AlbinaException e) {
			logger.warn("Error loading checklist", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@Post
	@Secured({ Role.Str.ADMIN, Role.Str.FORECASTER })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Store checklist for a given date and region")
	@Transactional
	public Checklist saveChecklist(
			Principal principal,
			@Parameter(description = DateControllerUtil.DATE_FORMAT_DESCRIPTION) @QueryValue("date") String date,
			@QueryValue("region") String regionId,
			@Body Checklist checklistPayload) {

		try {
			Instant startDate = DateControllerUtil.parseDateOrThrow(date);
			User user = userRepository.findByIdOrElseThrow(principal);
			Region region = regionRepository.findById(regionId).orElseThrow();

			if (!user.hasPermissionForRegion(region.getId())) {
				throw new AlbinaException("User is not authorized for this region!");
			}

			ZonedDateTime checklistDate = startDate.atZone(ZoneOffset.UTC);
			List<PublicationChecklistItem> items = checklistPayload == null || checklistPayload.checklistItems() == null
					? List.of()
					: checklistPayload.checklistItems();

			PublicationChecklist checklistEntity = getOrCreateChecklist(checklistPayload, checklistDate, region, user);
			checklistEntity.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).withNano(0));
			checklistEntity.setUser(user);
			checklistEntity.setItems(items);
			final PublicationChecklist savedChecklist = publicationChecklistRepository.save(checklistEntity);
			logger.info("Stored {} checklist items for date={} region={} checklistId={}", items.size(),
					checklistDate, regionId, savedChecklist.getId());

			return new Checklist(savedChecklist.getId(), savedChecklist.getTimestamp().toInstant(),
					savedChecklist.getItems(), user.getEmail());
		} catch (AlbinaException e) {
			logger.warn("Error storing checklist", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private Checklist toChecklist(PublicationChecklist checklist) {
		return new Checklist(checklist.getId(), checklist.getTimestamp().toInstant(), checklist.getItems(),
				checklist.getUser().getEmail());
	}

	private PublicationChecklist getOrCreateChecklist(Checklist checklistPayload, ZonedDateTime checklistDate,
			Region region, User user)
			throws AlbinaException {
		String checklistId = checklistPayload == null ? null : checklistPayload.checklistId();
		if (checklistId == null || checklistId.isBlank()) {
			PublicationChecklist checklist = new PublicationChecklist();
			checklist.setDate(checklistDate);
			checklist.setRegion(region);
			checklist.setUser(user);
			return checklist;
		}

		PublicationChecklist checklist = publicationChecklistRepository.findById(checklistId).orElseGet(() -> {
			PublicationChecklist newChecklist = new PublicationChecklist();
			newChecklist.setId(checklistId);
			newChecklist.setDate(checklistDate);
			newChecklist.setRegion(region);
			newChecklist.setUser(user);
			return newChecklist;
		});

		if (!region.getId().equals(checklist.getRegion().getId())) {
			throw new AlbinaException("Checklist does not belong to region " + region.getId() + "!");
		}
		if (!checklistDate.equals(checklist.getDate())) {
			throw new AlbinaException("Checklist date mismatch!");
		}

		return checklist;
	}
}
// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import eu.albina.exception.AlbinaException;
import eu.albina.jobs.ChannelStatusJob;
import eu.albina.model.StatusInformation;
import eu.albina.model.enumerations.Role;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/status")
@Tag(name = "status")
public class StatusService {

	@Inject
	private ChannelStatusJob channelStatusJob;

	@Get("/whatsapp")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get WhatsApp channel status")
	public StatusInformation getWhatsAppStatus(@QueryValue("region") String regionId) {
		try {
			return channelStatusJob.getOrTriggerWhatsAppStatus(regionId);
		} catch (AlbinaException e) {
			return new StatusInformation(false, e.getMessage());
		}
	}

	@Get("/telegram")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get Telegram channel status")
	public StatusInformation getTelegramStatus(@QueryValue("region") String regionId) {
		try {
			return channelStatusJob.getOrTriggerTelgramStatus(regionId);
		} catch (AlbinaException e) {
			return new StatusInformation(false, e.getMessage());
		}
	}

	@Get("/blog")
	@Secured({ Role.Str.SUPERADMIN, Role.Str.ADMIN })
	@SecurityRequirement(name = AuthenticationService.SECURITY_SCHEME)
	@Operation(summary = "Get blog status")
	public StatusInformation getBlogStatus(@QueryValue("region") String regionId) {
		try {
			return channelStatusJob.getOrTriggerBlogStatus(regionId);
		} catch (AlbinaException e) {
			return new StatusInformation(false, e.getMessage());
		}
	}
}

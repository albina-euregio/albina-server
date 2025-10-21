/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package eu.albina.controller.publication;

import eu.albina.controller.publication.TelegramController.TelegramConfigurationRepository;
import eu.albina.model.publication.TelegramConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest
class TelegramControllerTest {

	@Inject
	TelegramConfigurationRepository telegramConfigurationRepository;

	@Inject
	TelegramController telegramController;

	@Disabled
	@Test
	void testSend() throws Exception {
		TelegramConfiguration configuration = telegramConfigurationRepository.findById(1L).orElseThrow();
		if (!"@aws_test".equals(configuration.getChatId())) {
			throw new IllegalStateException();
		}
		telegramController.sendPhoto(configuration, "Avalanche Forecast", "https://static.avalanche.report/bulletins_dev/2025-10-17/fd_EUREGIO_map.jpg");
	}
}

// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.albina.controller.publication.PushNotificationUtil.PushSubscriptionRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;

public class PushSubscriptionControllerTest {

	@Inject
	PushSubscriptionRepository pushSubscriptionRepository;

	@Test
	@Disabled
	public void get() {
		List<PushSubscription> subscriptions = pushSubscriptionRepository.findByLanguageAndRegionInList(LanguageCode.de, Collections.singletonList("AT-07"));
		Assertions.assertFalse(subscriptions.isEmpty());
		subscriptions = pushSubscriptionRepository.findByLanguageAndRegionInList(LanguageCode.de, Arrays.asList("AT-07", "IT-32-BZ"));
		Assertions.assertFalse(subscriptions.isEmpty());
	}
}

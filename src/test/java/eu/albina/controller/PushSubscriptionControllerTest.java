// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import eu.albina.util.HibernateUtil;

public class PushSubscriptionControllerTest {

	@Test
	@Disabled
	public void get() {
		HibernateUtil.getInstance().setUp();
		List<PushSubscription> subscriptions = PushSubscriptionController.get(LanguageCode.de, Collections.singletonList("AT-07"));
		Assertions.assertTrue(subscriptions.size() > 0);
		subscriptions = PushSubscriptionController.get(LanguageCode.de, Arrays.asList("AT-07", "IT-32-BZ"));
		Assertions.assertTrue(subscriptions.size() > 0);
	}
}

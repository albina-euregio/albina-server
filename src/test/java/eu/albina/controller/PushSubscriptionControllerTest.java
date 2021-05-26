package eu.albina.controller;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PushSubscriptionControllerTest {

	@Test
	@Ignore
	public void get() {
		List<PushSubscription> subscriptions = PushSubscriptionController.get(LanguageCode.de, Collections.singletonList("AT-07"));
		Assert.assertTrue(subscriptions.size() > 0);
		subscriptions = PushSubscriptionController.get(LanguageCode.de, Arrays.asList("AT-07", "IT-32-BZ"));
		Assert.assertTrue(subscriptions.size() > 0);
	}
}

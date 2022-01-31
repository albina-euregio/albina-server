package eu.albina.util;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PushNotificationUtilTest {

	@Before
	public void setUp() throws Exception {
		HibernateUtil.getInstance().setUp();
	}

	@After
	public void shutDown() throws Exception {
		HibernateUtil.getInstance().shutDown();
	}

	@Test
	public void test() throws Exception {
		PushSubscription subscription = new PushSubscription();
		subscription.setAuth("wnAO8hfJGyGtdK3uUmVI8g");
		subscription.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAABgHwSx9txJscXfY5Dz82G5Xs7b6U0zROFXDPDhSM9D4KCTEmGxJTLfZ7arYnRlS3BexTWFeLA8pfzDEHjd8tX9UBmLuUaR3Xnim3Q-2Xa3UddaHRbh4NT2mKFMGBDmIZ4208OgpVECiuoI8UANC9B3IOf2CpduP58fUz1VE857gyNeHsw");
		subscription.setLanguage(LanguageCode.de);
		subscription.setP256dh("BEoQn2VR93GQ9gBxOo4pvdmgOyO1eiSDjUy7blwez1Vu_99PDswkEtV6m7cuwB60A8WlYq6lGKTZLet7PbnAEow");
		subscription.setRegion("AT-07");

		Response response = mock(Response.class);
		when(response.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(201));
		Invocation.Builder builder = mock(Invocation.Builder.class);
		when(builder.post(any())).thenReturn(response);
		WebTarget webTarget = mock(WebTarget.class);
		when(webTarget.request()).thenReturn(builder);
		Client client = mock(Client.class);
		when(client.target(eq(URI.create(subscription.getEndpoint())))).thenReturn(webTarget);

		new PushNotificationUtil(client).sendWelcomePushMessage(subscription);

		verify(builder).header(eq("Content-Type"), eq("application/octet-stream"));
		verify(builder).header(eq("TTL"), eq("180"));
		verify(builder).header(eq("Content-Encoding"), eq("aes128gcm"));
		verify(builder).header(eq("Authorization"), startsWith("vapid t=ey"));
	}
}

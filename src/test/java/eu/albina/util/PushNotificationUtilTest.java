package eu.albina.util;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.security.GeneralSecurityException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PushNotificationUtilTest {

	@Test
	public void test() throws Exception {
		PushSubscription subscription = new PushSubscription();
		subscription.setAuth("wnAO8hfJGyGtdK3uUmVI8g");
		subscription.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAABgHwSx9txJscXfY5Dz82G5Xs7b6U0zROFXDPDhSM9D4KCTEmGxJTLfZ7arYnRlS3BexTWFeLA8pfzDEHjd8tX9UBmLuUaR3Xnim3Q-2Xa3UddaHRbh4NT2mKFMGBDmIZ4208OgpVECiuoI8UANC9B3IOf2CpduP58fUz1VE857gyNeHsw");
		subscription.setLanguage(LanguageCode.de);
		subscription.setP256dh("BEoQn2VR93GQ9gBxOo4pvdmgOyO1eiSDjUy7blwez1Vu_99PDswkEtV6m7cuwB60A8WlYq6lGKTZLet7PbnAEow");
		subscription.setRegion("AT-07");

		StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(201);
		HttpResponse httpResponse = mock(HttpResponse.class);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		HttpClient httpClient = mock(HttpClient.class);
		when(httpClient.execute(any())).thenReturn(httpResponse);

		new PushNotificationUtil(httpClient) {
			@Override
			protected PushService getPushService() throws GeneralSecurityException {
				return new PushService(
					"BEgjVJRSctSvDIJ_7Hvo7ZjAXeSOO2fFGkiofbXh41O4FWqh6aIgVDI8Wp9fU2HRv-7qglih19Ba2GRXHUh5jTo",
					"uAICF2Y8mCGJpSfVLm6L1SOlxb59jAT819-g3Xj5uL0");
			}
		}.sendWelcomePushMessage(subscription);

		ArgumentCaptor<HttpUriRequest> argument = ArgumentCaptor.forClass(HttpUriRequest.class);
		verify(httpClient).execute(argument.capture());
		verifyNoMoreInteractions(httpClient);

		HttpPost httpPost = (HttpPost) argument.getValue();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		httpPost.getEntity().writeTo(bytes);

		Assert.assertEquals("POST", httpPost.getMethod());
		Assert.assertEquals(URI.create(subscription.getEndpoint()), httpPost.getURI());
		Assert.assertTrue(bytes.toByteArray().length > 140);
	}
}

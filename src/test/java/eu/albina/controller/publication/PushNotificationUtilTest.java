// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import eu.albina.util.JsonUtil;
import io.micronaut.http.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import ch.rasc.webpush.ServerKeys;
import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;

public class PushNotificationUtilTest {

	@Test
	public void test() throws Exception {
		ServerKeys serverKeys = new ServerKeys("BEgjVJRSctSvDIJ_7Hvo7ZjAXeSOO2fFGkiofbXh41O4FWqh6aIgVDI8Wp9fU2HRv-7qglih19Ba2GRXHUh5jTo",
			"uAICF2Y8mCGJpSfVLm6L1SOlxb59jAT819-g3Xj5uL0");

		PushSubscription subscription = new PushSubscription();
		subscription.setAuth("wnAO8hfJGyGtdK3uUmVI8g");
		subscription.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAABgHwSx9txJscXfY5Dz82G5Xs7b6U0zROFXDPDhSM9D4KCTEmGxJTLfZ7arYnRlS3BexTWFeLA8pfzDEHjd8tX9UBmLuUaR3Xnim3Q-2Xa3UddaHRbh4NT2mKFMGBDmIZ4208OgpVECiuoI8UANC9B3IOf2CpduP58fUz1VE857gyNeHsw");
		subscription.setLanguage(LanguageCode.de);
		subscription.setP256dh("BEoQn2VR93GQ9gBxOo4pvdmgOyO1eiSDjUy7blwez1Vu_99PDswkEtV6m7cuwB60A8WlYq6lGKTZLet7PbnAEow");
		subscription.setRegion("AT-07");

		HttpResponse response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(201);
		when(response.body()).thenReturn("");
		HttpClient client = mock(HttpClient.class);
		when(client.send(any(), any())).thenReturn(response);

		PushNotificationUtil.Message payload = new PushNotificationUtil.Message(
			"Avalanche.report",
			"Hello World!",
			null,
			null
		);
		Assertions.assertEquals("{\"title\":\"Avalanche.report\",\"body\":\"Hello World!\",\"image\":null,\"url\":null}", JsonUtil.writeValueUsingJackson(payload));
		new PushNotificationUtil(client, null).sendPushMessage(subscription, payload, serverKeys);

		ArgumentCaptor<HttpRequest> argumentCaptor = getEntityArgumentCaptor();
		verify(client).send(argumentCaptor.capture(), any());
		Assertions.assertEquals("application/octet-stream", getHeader(argumentCaptor, HttpHeaders.CONTENT_TYPE));
		Assertions.assertEquals("aes128gcm", getHeader(argumentCaptor, HttpHeaders.CONTENT_ENCODING));
		MatcherAssert.assertThat(getHeader(argumentCaptor, HttpHeaders.AUTHORIZATION), Matchers.startsWith("vapid t=ey"));
		Assertions.assertEquals("180", getHeader(argumentCaptor, "TTL"));
	}

	private static ArgumentCaptor<HttpRequest> getEntityArgumentCaptor() {
		return ArgumentCaptor.forClass(HttpRequest.class);
	}

	private static String getHeader(ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor, String header) {
		return httpRequestArgumentCaptor.getValue().headers().firstValue(header).orElseThrow();
	}
}

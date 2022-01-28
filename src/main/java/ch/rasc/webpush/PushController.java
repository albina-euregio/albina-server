package ch.rasc.webpush;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.rasc.webpush.dto.Subscription;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web push and signed JWTs
 * @apiNote https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol#web_push_and_signed_jwts
 */
public class PushController {

	public static final Logger logger = LoggerFactory.getLogger(PushController.class);
	private final ServerKeys serverKeys;

	private final Algorithm jwtAlgorithm;

	public PushController(ServerKeys serverKeys) {
		this.serverKeys = serverKeys;
		this.jwtAlgorithm = Algorithm.ECDSA256(this.serverKeys.getPublicKey(), this.serverKeys.getPrivateKey());
	}

	private String getToken(String origin) {
		Date expires = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12));
		return JWT.create()
			.withAudience(origin)
			.withExpiresAt(expires)
			.withSubject("mailto:example@example.com")
			.sign(this.jwtAlgorithm);
	}

	private String getAuthorization(URI endpointURI) {
		String origin = endpointURI.getScheme() + "://" + endpointURI.getHost();
		final String token = getToken(origin);
		return "vapid t=" + token + ", k=" + this.serverKeys.getPublicKeyBase64();
	}

	public HttpUriRequest prepareRequest(Subscription subscription, byte[] encryptedPayload) {
		URI endpointURI = URI.create(subscription.getEndpoint());
		return RequestBuilder.post(endpointURI)
			.setEntity(new ByteArrayEntity(encryptedPayload))
			.addHeader("Content-Type", "application/octet-stream")
			.addHeader("Content-Encoding", "aes128gcm")
			.addHeader("TTL", "180")
			.addHeader("Authorization", getAuthorization(endpointURI))
			.build();
	}

}

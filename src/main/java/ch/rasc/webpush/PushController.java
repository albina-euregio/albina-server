package ch.rasc.webpush;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
		this.jwtAlgorithm = serverKeys.toJwtAlgorithm();
	}

	private String getToken(String origin) {
		Date expires = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12));
		return JWT.create()
			.withAudience(origin)
			.withExpiresAt(expires)
			.withSubject("mailto:example@example.com")
			.sign(this.jwtAlgorithm);
	}

	public String getAuthorization(URI endpointURI) {
		String origin = endpointURI.getScheme() + "://" + endpointURI.getHost();
		final String token = getToken(origin);
		return "vapid t=" + token + ", k=" + this.serverKeys.getPublicKeyBase64();
	}

}

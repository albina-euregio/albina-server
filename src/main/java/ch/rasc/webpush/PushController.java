package ch.rasc.webpush;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Web push and signed JWTs
 *
 * @apiNote https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol#web_push_and_signed_jwts
 */
public record PushController(ServerKeys serverKeys) {

	private String getToken(String origin) {
		Date expires = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12));
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.audience(origin)
			.expirationTime(expires)
			.subject("mailto:example@example.com")
			.build();

		try {
			CryptoService cryptoService = new CryptoService();
			ECPublicKey publicKey = cryptoService.fromUncompressedECPublicKey(serverKeys.publicKeyBase64());
			ECPrivateKey privateKey = cryptoService.fromUncompressedECPrivateKey(serverKeys.privateKeyBase64(), publicKey);
			JWSSigner signer = new ECDSASigner(privateKey);
			JWSHeader header = new JWSHeader(JWSAlgorithm.ES256);
			SignedJWT signedJWT = new SignedJWT(header, claims);
			signedJWT.sign(signer);
			return signedJWT.serialize();
		} catch (JOSEException | GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public String getAuthorization(URI endpointURI) {
		String origin = endpointURI.getScheme() + "://" + endpointURI.getHost();
		final String token = getToken(origin);
		return "vapid t=" + token + ", k=" + this.serverKeys.publicKeyBase64();
	}

}

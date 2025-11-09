package eu.albina.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Serdeable
// https://developers.google.com/identity/protocols/oauth2/service-account#httprest
public record GoogleCredentials(
	String type,
	String project_id,
	String private_key_id,
	String private_key,
	String client_email,
	String client_id,
	String auth_uri,
	String token_uri,
	String auth_provider_x509_cert_url,
	String client_x509_cert_url,
	String universe_domain
) {

	@Serdeable
	record AccessToken(
		String access_token,
		String scope,
		String token_type,
		Integer expires_in
	) {
	}

	static GoogleCredentials ofEnv(ObjectMapper objectMapper) throws IOException {
		String credentialsFile = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
		byte[] bytes = Files.readAllBytes(Paths.get(credentialsFile));
		return objectMapper.readValue(bytes, GoogleCredentials.class);
	}

	String jwt() throws GeneralSecurityException, JOSEException {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.audience("https://oauth2.googleapis.com/token")
			.claim("scope", TextToSpeech.API_AUTH_SCOPE)
			.expirationTime(Date.from(Instant.now().plusSeconds(60)))
			.issueTime(Date.from(Instant.now()))
			.issuer(client_email)
			.build();
		byte[] decodedKey = Base64.getDecoder().decode(private_key
			.replace("-----BEGIN PRIVATE KEY-----", "")
			.replace("-----END PRIVATE KEY-----", "")
			.replaceAll("\\s+", ""));
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		JWSSigner signer = new RSASSASigner(privateKey);
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		SignedJWT signedJWT = new SignedJWT(header, claims);
		signedJWT.sign(signer);
		return signedJWT.serialize();
	}

	AccessToken fetchAccessToken(HttpClient httpClient, ObjectMapper objectMapper) throws GeneralSecurityException, JOSEException, IOException, InterruptedException {
		URI uri = URI.create("https://oauth2.googleapis.com/token");
		String form = "grant_type=%s&assertion=%s".formatted(
			URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", StandardCharsets.UTF_8),
			URLEncoder.encode(jwt(), StandardCharsets.UTF_8)
		);
		HttpRequest request = HttpRequest.newBuilder(uri)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.POST(HttpRequest.BodyPublishers.ofString(form))
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), AccessToken.class);
	}

}

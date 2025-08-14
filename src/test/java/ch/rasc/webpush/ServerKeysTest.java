// SPDX-License-Identifier: AGPL-3.0-or-later
package ch.rasc.webpush;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import eu.albina.controller.AuthenticationController;

class ServerKeysTest {
	@Test
	void toJwtAlgorithm() throws Exception {
		Algorithm algorithm = new ServerKeys(
			// npx web-push generate-vapid-keys
			"BFJU-8n4q4I-kdbRHpy3ZVx1ymjh-CGsGXhzxCXS4QdD8SDO7jcf-pwkJfCc8yktFlklQG9HneYkRGZBHe9HO-Y",
			"Ug4-DhNs_lDEKKjQyjZPVJhSKKV3RccjgOXfA8aQnGE"
		).toJwtAlgorithm();

		String token = AuthenticationController.issueAccessToken(algorithm, "foobar");
		System.out.println(token);
		assertNotNull(JWT.require(algorithm).withIssuer("albina").build().verify(token));
	}
}

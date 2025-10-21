package eu.albina.rest;

import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class OpenApiServiceTest {

	@Test
	void openApi(@Client("/albina/api") HttpClient httpClient) {
		BlockingHttpClient client = httpClient.toBlocking();
		assertDoesNotThrow(() -> client.exchange("/openapi.yml"));
	}

}

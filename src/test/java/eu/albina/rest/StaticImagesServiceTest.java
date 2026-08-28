package eu.albina.rest;

import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class StaticImagesServiceTest {

	@Test
	void anonymousCanAccessImages(@Client("/albina/api") HttpClient httpClient) {
		BlockingHttpClient client = httpClient.toBlocking();
		assertDoesNotThrow(() -> client.exchange("/images/bg_checkered.png"));
	}

	@Test
	void anonymousCanAccessNestedImages(@Client("/albina/api") HttpClient httpClient) {
		BlockingHttpClient client = httpClient.toBlocking();
		assertDoesNotThrow(() -> client.exchange("/images/aspects/color/0.png"));
	}

}

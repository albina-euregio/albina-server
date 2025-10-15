package eu.albina;

import com.google.common.io.Resources;
import eu.albina.model.AvalancheBulletin;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Singleton
public class AvalancheBulletinTestUtils {

	@Inject
	ObjectMapper objectMapper;

	public List<AvalancheBulletin> readBulletins(final URL resource) throws IOException {
		final String validBulletinStringFromResource = Resources.toString(resource, StandardCharsets.UTF_8);
		final AvalancheBulletin[] bulletins = objectMapper.readValue(validBulletinStringFromResource, AvalancheBulletin[].class);
		return List.of(bulletins);
	}
}

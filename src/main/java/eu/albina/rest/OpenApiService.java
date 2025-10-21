package eu.albina.rest;

import com.google.common.io.Resources;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Controller
@Tag(name = "openapi")
public class OpenApiService {

	@Get
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.TEXT_HTML)
	public String index() {
		return """
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>albina-server</title>
  <script type="module" src="https://unpkg.com/rapidoc@9.3.8/dist/rapidoc-min.js"></script>
</head>
<body>
  <rapi-doc spec-url="./api/openapi.json" theme = "dark"></rapi-doc>
</body>
</html>
			""";
	}

	@Get("/openapi.yml")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.APPLICATION_YAML)
	public String getOpenApi() throws IOException {
		URL resource = Resources.getResource("META-INF/swagger/albina-server-0.0.yml");
		return Resources.toString(resource, StandardCharsets.UTF_8);
	}

	@Get("/openapi.json")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Produces(MediaType.APPLICATION_JSON)
	public Object getOpenApiJson() throws Exception {
		return new Yaml().load(getOpenApi());
	}
}

package eu.albina.rest.mn;

import eu.albina.controller.UserController;
import eu.albina.model.User;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Singleton;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AuthenticationProviderUserPassword<B> implements HttpRequestAuthenticationProvider<B> {
	@Override
	public @NonNull AuthenticationResponse authenticate(@Nullable HttpRequest<B> requestContext, @NonNull AuthenticationRequest<String, String> authRequest) {
		User user = UserController.getInstance().getUser(String.valueOf(authRequest.getIdentity()));
		if (BCrypt.checkpw(String.valueOf(authRequest.getSecret()), user.getPassword())) {
			List<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());
			return AuthenticationResponse.success(user.getName(), roles);
		} else {
			return AuthenticationResponse.failure();
		}
	}
}

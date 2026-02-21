package com.gmavrommatis.config.security;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestReactiveAuthenticationProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

@Singleton // registers this bean in the Micronaut context
@Slf4j
public class AuthenticationProvider<B> implements HttpRequestReactiveAuthenticationProvider<B> {

  @Inject IdentityStore store;

  /**
   * The B generic matches whatever HTTP body type your route uses. Even if you don’t use it here,
   * Micronaut will forward the parsed HttpRequest<B> so you *could* inspect headers/body for custom
   * logic.
   */
  @Override
  public Publisher<AuthenticationResponse> authenticate(
      @Nullable HttpRequest<B> httpRequest, // <1>
      @NonNull AuthenticationRequest<String, String> authenticationRequest // <2>
      ) {

    // --- LOGGING METADATA  ---
    if (httpRequest != null) {
      // HTTP method and URI
      log.info(
          "Auth attempt → method={}, uri={}, user={}",
          httpRequest.getMethod(),
          httpRequest.getUri(),
          authenticationRequest.getIdentity());

      String ua = httpRequest.getHeaders().get("User-Agent");
      log.debug("User-Agent header: {}", ua);
      log.trace("All headers: {}", httpRequest.getHeaders().asMap());

      // Any cookies sent
      log.debug("Cookies: {}", httpRequest.getCookies().asMap());
    }
    return reactor.core.publisher.Mono.fromCallable(
        () -> {
          var user = authenticationRequest.getIdentity();
          var pass = authenticationRequest.getSecret();

          /*because store now is in memory we don't need reactive code, but if it was in another system,
           * we should make it react, or offload to IO thread the entire process.*/
          var storedPassword = store.getUserPassword(user);
          var storedRole = store.getUserRole(user);
          var attrs = store.getAttributes(user);

          if (storedPassword != null && storedPassword.equals(pass)) {
            return AuthenticationResponse.success(user, List.of(storedRole), attrs);
          }
          return AuthenticationResponse.failure(
              AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        });
  }
}

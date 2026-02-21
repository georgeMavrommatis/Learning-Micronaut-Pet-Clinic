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

  /** Strong RNG for token IDs. */
  private static final java.security.SecureRandom RNG = new java.security.SecureRandom();

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
          attrs.put("jti", generateJti());

          if (storedPassword != null && storedPassword.equals(pass)) {
            return AuthenticationResponse.success(user, List.of(storedRole), attrs);
          }
          return AuthenticationResponse.failure(
              AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        });
  }

  /**
   * Generates a cryptographically strong, URL-safe JWT ID ({@code jti}).
   *
   * <p>Implementation details:
   *
   * <ul>
   *   <li>Draws <b>256 bits</b> of entropy from {@link SecureRandom}.
   *   <li>Encodes the bytes with Base64URL <b>without padding</b>, so the result is URL/cookie
   *       safe.
   *   <li>Output length is <b>43 characters</b> (32 bytes → 43 Base64URL chars, no {@code =}
   *       padding).
   * </ul>
   *
   * <p>Collision probability is effectively negligible: with uniform 256-bit values, the birthday
   * bound gives an approximate collision chance of {@code n^2 / 2^257}. Even issuing {@code 10^12}
   * IDs total, the probability is ~<b>6×10⁻⁵⁴</b>; at {@code 10^15} IDs it’s
   * ~<b>6×10⁻⁴⁸</b>—astronomically small. In practice this is “unique enough”; still, keep a
   * <b>UNIQUE</b> constraint on the {@code jti} column so a freak collision (or a bug) fails fast
   * and can be retried.
   *
   * @return URL-safe Base64 string containing 256 bits of random data (the new {@code jti})
   */
  private String generateJti() {
    byte[] bytes = new byte[32]; // 256-bit
    RNG.nextBytes(bytes);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

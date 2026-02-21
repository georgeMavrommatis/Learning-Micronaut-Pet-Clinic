package com.gmavrommatis.controller;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.security.authentication.*;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Test-only AuthenticationProvider replacement used to short-circuit real authentication during
 * automated tests.
 *
 * <p><strong>Why we need this in tests</strong>:
 *
 * <ul>
 *   <li>Integration/unit tests should not depend on external identity providers (Keycloak, OAuth,
 *       JWT issuers). Those external dependencies make tests slow, brittle, and
 *       environment-dependent.
 *   <li>Replacing the real authentication provider with a deterministic test stub allows tests to
 *       exercise authenticated code paths quickly and reliably by returning a known authenticated
 *       principal and roles.
 *   <li>Using a replacement bean keeps the test application context realistic (beans are wired as
 *       in real app) while avoiding token verification and network calls.
 * </ul>
 *
 * <p><strong>How to use</strong>: Put this class on the test classpath (e.g. under {@code
 * src/test/java}). The {@code @Replaces} annotation instructs Micronaut to replace the real
 * AuthenticationProvider with this stub while running tests.
 *
 * <p><strong>Notes / alternatives</strong>:
 *
 * <ul>
 *   <li>Prefer putting test replacements under the test source set so they do not ship to
 *       production.
 *   <li>For per-test isolation consider {@code @MockBean} inside a {@code @MicronautTest} class
 *       instead of a global {@code @Replaces} bean.
 *   <li>The raw {@code AuthenticationProvider} API was deprecated in some Micronaut 4.x releases in
 *       favor of typed/generic forms (e.g. {@code AuthenticationProvider<HttpRequest<?>>}). If you
 *       see deprecation warnings, update the implemented interface to the recommended generic type
 *       to remove warnings.
 * </ul>
 */
@Singleton
@Replaces(
    AuthenticationProvider
        .class) // replace real providers (Keycloak/JWT) in the test application context
public class TestAuthProvider implements AuthenticationProvider {

  /**
   * Authenticate method used by Micronaut security during authentication flows.
   *
   * <p>In tests we always return a successful authentication response synchronously using {@code
   * Mono.just(...)}. This makes secured endpoints behave as if the incoming request were
   * authenticated as "test-user" with role "ADMIN".
   *
   * @param httpRequest the incoming request object (ignored by this test provider)
   * @param authenticationRequest original authentication request (ignored by this test provider)
   * @return a {@link Publisher} that emits a successful {@link AuthenticationResponse}
   */
  @Override
  public @NonNull Publisher<AuthenticationResponse> authenticate(
      Object httpRequest, AuthenticationRequest authenticationRequest) {

    // Return a successful AuthenticationResponse for every request.
    // - "test-user" is the principal id used in tests.
    // - List.of("ADMIN") gives the test user the ADMIN role; adjust roles to match test needs.
    // - Map.of() supplies optional attributes (left empty here).
    //
    // Using a reactive Publisher (Mono.just) matches Micronaut's reactive expectations.
    return Mono.just(AuthenticationResponse.success("test-user", List.of("ADMIN"), Map.of()));
  }
}

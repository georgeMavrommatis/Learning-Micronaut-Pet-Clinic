package com.gmavrommatis.config.security;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.endpoints.introspection.IntrospectionResponse;
import io.micronaut.security.token.jwt.validator.ReactiveJsonWebTokenValidator;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.Base64;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Reactive JWT validator that delegates token validity to Keycloak's introspection endpoint and, if
 * active, parses the token and builds a Micronaut {@link Authentication} from common Keycloak claim
 * shapes.
 *
 * <p>This bean replaces Micronaut's default {@link ReactiveJsonWebTokenValidator} so that:
 *
 * <ul>
 *   <li>Tokens are treated as valid only if Keycloak reports them as {@code active}.
 *   <li>Realm and client roles from {@code realm_access} and {@code resource_access} are collected
 *       into the user's authorities.
 * </ul>
 *
 * The approach is useful when you prefer server-side introspection (revocation-aware) over pure
 * JWKS signature verification.
 *
 * <p><strong>Security notes:</strong>
 *
 * <ul>
 *   <li>Client credentials are sent via HTTP Basic auth to the introspection endpoint.
 *   <li>Errors/timeouts fail closed and yield an empty result, denying the request.
 *   <li>A short timeout is applied to avoid tying up request threads.
 * </ul>
 *
 * @author gewrgios mavrommatis
 * @since 1.0
 */
@Singleton
@Replaces(bean = ReactiveJsonWebTokenValidator.class)
public class KeycloakReactiveJwtValidator
    implements ReactiveJsonWebTokenValidator<JWT, HttpRequest<?>> {

  private final KeycloakIntrospectionClient client;
  private final String clientId;
  private final String clientSecret;

  /**
   * @param client HTTP client to call Keycloak's introspection endpoint
   * @param clientId OAuth2 client identifier used for introspection auth
   * @param clientSecret OAuth2 client secret used for introspection auth
   */
  public KeycloakReactiveJwtValidator(
      KeycloakIntrospectionClient client,
      @Value("${keycloak.client-id:pet-clinic}") String clientId,
      @Value("${keycloak.client-secret:petclinic-secret}") String clientSecret) {
    this.client = client;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /**
   * Verifies the token is active via Keycloak and then parses it into a Nimbus {@link JWT}.
   * Returning {@code Mono.empty()} indicates validation failure.
   */
  @Override
  public @NonNull Publisher<JWT> validate(@NonNull String token, HttpRequest<?> request) {
    return introspectActive(token)
        .flatMap(
            active -> {
              if (!active) {
                return Mono.empty();
              }
              try {
                return Mono.just(JWTParser.parse(token));
              } catch (ParseException e) {
                return Mono.empty();
              }
            });
  }

  /**
   * Validates the token and, if active, builds a Micronaut {@link Authentication}:
   *
   * <ul>
   *   <li>Principal name prefers {@code sub}, then {@code username}, else {@code anonymous}.
   *   <li>Authorities are gathered from Keycloak's {@code realm_access.roles} and {@code
   *       resource_access[clientId].roles}.
   *   <li>Selected metadata (client_id, jti, token_type, exp, iat) is exposed as attributes.
   * </ul>
   *
   * Any error or timeout results in an empty publisher (deny access).
   */
  @Override
  public @NonNull Publisher<Authentication> validateToken(
      @NonNull String token, HttpRequest<?> request) {
    String basic =
        "Basic "
            + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

    Map<String, String> body = new LinkedHashMap<>();
    body.put("token", token);
    body.put("token_type_hint", "access_token");

    return Mono.from(client.introspect(body, basic))
        .timeout(Duration.ofSeconds(2))
        .flatMap(
            resp -> {
              if (!resp.isActive()) {
                return Mono.empty();
              }

              // Collect roles from realm and client sections
              Set<String> roles = new LinkedHashSet<>();
              Map<String, Object> ext = resp.getExtensions();
              if (ext != null) {
                Object ra = ext.get("realm_access");
                if (ra instanceof Map<?, ?> m) {
                  Object r = m.get("roles");
                  if (r instanceof Collection<?> c) {
                    c.forEach(v -> roles.add(Objects.toString(v)));
                  }
                }
                Object res = ext.get("resource_access");
                if (res instanceof Map<?, ?> m) {
                  Object client = m.get(clientId);
                  if (client instanceof Map<?, ?> mm) {
                    Object rr = mm.get("roles");
                    if (rr instanceof Collection<?> c) {
                      c.forEach(v -> roles.add(Objects.toString(v)));
                    }
                  }
                }
              }

              String name =
                  resp.getSub() != null
                      ? resp.getSub()
                      : (resp.getUsername() != null ? resp.getUsername() : "anonymous");

              Map<String, Object> attrs = new LinkedHashMap<>();
              attrs.put("client_id", resp.getClientId());
              attrs.put("jti", resp.getJti());
              attrs.put("token_type", resp.getTokenType());
              attrs.put("exp", resp.getExp());
              attrs.put("iat", resp.getIat());

              return Mono.just(Authentication.build(name, roles, attrs));
            })
        // Fail closed: on error or timeout, treat token as invalid
        .onErrorResume(e -> Mono.empty());
  }

  /**
   * Calls the introspection endpoint and returns {@code true} only if the token is active. Errors
   * and timeouts are treated as inactive to avoid granting access on uncertainty.
   */
  private Mono<Boolean> introspectActive(String token) {
    String basic =
        "Basic "
            + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

    Map<String, String> body = Map.of("token", token, "token_type_hint", "access_token");

    return Mono.from(client.introspect(body, basic))
        .timeout(Duration.ofSeconds(2))
        .map(IntrospectionResponse::isActive)
        .onErrorReturn(false);
  }
}

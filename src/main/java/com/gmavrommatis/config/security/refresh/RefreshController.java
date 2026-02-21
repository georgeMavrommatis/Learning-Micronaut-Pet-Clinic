package com.gmavrommatis.config.security.refresh;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Exposes a minimal OAuth-compatible endpoint for exchanging a Keycloak {@code refresh_token} for a
 * new {@code access_token}.
 *
 * <p>This controller delegates to {@link KeycloakTokenClient} and returns a compact JSON payload
 * suited for SPAs/mobile clients that cannot call Keycloak directly.
 *
 * <h3>Behavior</h3>
 *
 * <ul>
 *   <li>Validates presence of {@code refresh_token}.
 *   <li>Calls Keycloak {@code /protocol/openid-connect/token} with {@code
 *       grant_type=refresh_token}.
 *   <li>Returns {@code access_token}, {@code token_type} (default {@code Bearer}), {@code
 *       expires_in}, and {@code refresh_token} (only if provided).
 * </ul>
 *
 * <h3>Security notes</h3>
 *
 * <ul>
 *   <li>Prefer HTTPS; do not log token values.
 *   <li>Consider rate limiting and CSRF strategy based on how this is invoked.
 *   <li>Scope to your client origins via CORS.
 * </ul>
 *
 * @author gewrgios mavrommatis
 * @since 1.0
 */
@Singleton
@Controller("/oauth")
@Secured(
    SecurityRule.IS_ANONYMOUS) /*Usually we refresh a token when is expired. Thus we expect to be
unauthorized for our Use case sometimes.*/
public class RefreshController {

  private final KeycloakTokenClient kc;
  private final String realm;
  private final String clientId;
  private final String clientSecret;

  public RefreshController(
      KeycloakTokenClient kc,
      @Value("${keycloak.realm:pet-clinic}") String realm,
      @Value("${keycloak.client-id:pet-clinic}") String clientId,
      @Value("${keycloak.client-secret:petclinic-secret}") String clientSecret) {
    this.kc = kc;
    this.realm = realm;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /**
   * Exchanges a {@code refresh_token} for a fresh {@code access_token}.
   *
   * @param req JSON body containing {@code refresh_token}
   * @return 200 with token payload, or 400 if input is invalid
   */
  @Post(
      value = "/access_token",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public Publisher<HttpResponse<Map<String, Object>>> refresh(@Body RefreshRequest req) {
    if (req == null || req.refresh_token() == null || req.refresh_token().isBlank()) {
      return Mono.just(
          HttpResponse.badRequest(
              Map.of(
                  "error", "invalid_request",
                  "error_description", "refresh_token is missing")));
    }

    Map<String, String> form = new HashMap<>();
    form.put("grant_type", "refresh_token");
    form.put("refresh_token", req.refresh_token());
    form.put("client_id", clientId);
    form.put("client_secret", clientSecret);

    return Mono.from(kc.token(realm, form))
        .map(
            dto -> {
              // Build response without inserting nulls (Map.of(...) forbids nulls)
              Map<String, Object> body = new LinkedHashMap<>();
              body.put("access_token", dto.access_token());
              body.put("token_type", dto.token_type() != null ? dto.token_type() : "Bearer");
              body.put("expires_in", dto.expires_in() != null ? dto.expires_in() : 0);
              if (dto.refresh_token() != null && !dto.refresh_token().isBlank()) {
                body.put("refresh_token", dto.refresh_token());
              }
              return HttpResponse.ok(body)
                  .header("Cache-Control", "no-store")
                  .header("Pragma", "no-cache");
            });
  }

  // Example request payload:
  // public record RefreshRequest(String refresh_token) {}
}

package com.gmavrommatis.config.security.logout;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Singleton
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED) // require a valid access token in Authorization header
public class LogoutController {

  private final KeycloakLogoutClient kc;
  private final String realm;
  private final String clientId;
  private final String clientSecret;

  public LogoutController(
      KeycloakLogoutClient kc,
      @Value("${keycloak.realm:pet-clinic}") String realm,
      @Value("${keycloak.client-id:pet-clinic}") String clientId,
      @Value("${keycloak.client-secret:petclinic-secret}") String clientSecret) {
    this.kc = kc;
    this.realm = realm;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Post("/logout")
  public Publisher<HttpResponse<?>> logout(@Body LogoutRequest body) {
    if (body == null || body.refresh_token() == null || body.refresh_token().isBlank()) {
      return Mono.just(
          HttpResponse.badRequest(
              Map.of(
                  "error", "invalid_request",
                  "error_description", "refresh_token is required")));
    }

    // Keycloak end-session (recommended for user logout)
    Map<String, String> form = new HashMap<>();
    form.put("client_id", clientId);
    form.put("client_secret", clientSecret);
    form.put("refresh_token", body.refresh_token());

    return Mono.from(kc.logout(realm, form)).thenReturn(HttpResponse.noContent()); // 204 No Content
  }
}

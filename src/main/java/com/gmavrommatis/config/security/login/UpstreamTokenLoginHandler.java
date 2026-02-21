package com.gmavrommatis.config.security.login;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.handlers.LoginHandler;
import jakarta.inject.Singleton;
import java.util.Map;

/**
 * A {@link LoginHandler} replacement that returns upstream Identity Provider (e.g. Keycloak) tokens
 * as a JSON payload instead of performing the default redirect / session-based flow.
 *
 * <p>This is practical when the service acts as a thin OAuth2/OIDC façade or gateway and the client
 * (SPA/mobile/other service) expects raw tokens to store and use for subsequent calls. The handler
 * assumes a previous step (custom authenticator or filter) populated the {@link Authentication}
 * attributes:
 *
 * <ul>
 *   <li>{@code kc_access_token}
 *   <li>{@code kc_refresh_token}
 *   <li>{@code kc_token_type}
 *   <li>{@code kc_expires_in}
 * </ul>
 *
 * Values are echoed back to the caller in a compact, provider-agnostic shape.
 *
 * <p><strong>Notes:</strong>
 *
 * <ul>
 *   <li>Use HTTPS and avoid logging the returned tokens.
 *   <li>If you do not intend to issue refresh tokens to browsers, omit them.
 *   <li>Consider CORS and cache headers appropriate for your client type.
 * </ul>
 *
 * @author gewrgios mavrommatis
 * @since 1.0
 */
@Singleton
@Replaces(io.micronaut.security.handlers.LoginHandler.class)
public class UpstreamTokenLoginHandler implements LoginHandler {

  /**
   * On successful login, returns a JSON body with tokens taken from the authenticated principal's
   * attributes. If {@code kc_token_type} or {@code kc_expires_in} are missing, sensible defaults
   * are applied ({@code Bearer}, {@code 0}).
   *
   * @param authentication the authenticated principal carrying upstream token attributes
   * @param request the HTTP request (unused here)
   * @return 200 OK with a map containing {@code access_token}, {@code refresh_token}, {@code
   *     token_type}, and {@code expires_in}
   */
  @Override
  public Object loginSuccess(Authentication authentication, Object request) {
    Map<String, Object> a = authentication.getAttributes();
    return HttpResponse.ok(
        Map.of(
            "access_token", a.get("kc_access_token"),
            "refresh_token", a.get("kc_refresh_token"),
            "token_type", a.getOrDefault("kc_token_type", "Bearer"),
            "expires_in", a.getOrDefault("kc_expires_in", 0)));
  }

  /**
   * Refresh handling is not used in this setup. Returning 401 instructs clients to use the upstream
   * provider or a dedicated refresh endpoint, if any.
   *
   * @param authentication the current authentication
   * @param refreshToken the presented refresh token
   * @param request the HTTP request (unused)
   * @return 401 Unauthorized
   */
  @Override
  public Object loginRefresh(Authentication authentication, String refreshToken, Object request) {
    return HttpResponse.unauthorized();
  }

  /**
   * Returns 401 on failed login attempts.
   *
   * @param authenticationResponse the failure details
   * @param request the HTTP request (unused)
   * @return 401 Unauthorized
   */
  @Override
  public Object loginFailed(AuthenticationResponse authenticationResponse, Object request) {
    return HttpResponse.unauthorized();
  }
}

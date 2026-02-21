package com.gmavrommatis.controller;

import com.gmavrommatis.config.r2dbc.repository.AccessTokenRepository;
import com.gmavrommatis.config.r2dbc.repository.RefreshTokenRepository;
import com.gmavrommatis.utils.JwtUtils;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * HTTP endpoints for revoking issued JWT-based credentials.
 *
 * <ul>
 *   <li><strong>{@code POST /logout}</strong> — Revokes the <em>current</em> access token
 *       identified by its {@code jti} and any refresh tokens that reference that {@code jti} in
 *       their attributes. Returns {@code 204 No Content} on successful revocation and {@code 404
 *       Not Found} if nothing was revoked.
 * </ul>
 *
 * <p><strong>Notes</strong>:
 *
 * <ul>
 *   <li>Revocation is idempotent: repeating the same call after a token is revoked is a no-op.
 *   <li>These actions affect server-side allow/deny state; already-issued JWTs remain
 *       cryptographically valid but will be rejected by the application based on repository checks.
 *   <li>Use POST rather than GET due to side effects.
 * </ul>
 *
 * @author gewrgios mavrommatis
 */
@Controller
@Secured(SecurityRule.IS_AUTHENTICATED)
@Singleton
public class LogoutController {

  private final AccessTokenRepository accessTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  /**
   * Creates a new controller instance.
   *
   * @param accessTokenRepository repository managing access-token revocation state
   * @param refreshTokenRepository repository managing refresh-token revocation state
   */
  public LogoutController(
      AccessTokenRepository accessTokenRepository, RefreshTokenRepository refreshTokenRepository) {
    this.accessTokenRepository = accessTokenRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /**
   * Revokes the access token used in this request and any refresh tokens linked to its {@code jti}.
   *
   * <p>The {@code jti} is extracted from the authenticated principal's JWT claims via {@link
   * JwtUtils#requireJti(Authentication)}. If either the access token or at least one refresh token
   * is revoked, the endpoint returns {@code 204 No Content}. If nothing was revoked (e.g., unknown
   * {@code jti} or already revoked), it returns {@code 404 Not Found}.
   *
   * @param authentication the authenticated principal for this request; must carry a JWT with a
   *     {@code jti}
   * @return a reactive {@link Publisher} that completes with {@code 204 No Content} on success or
   *     {@code 404 Not Found} if no records were updated
   * @implNote This method performs two independent updates:
   *     <ol>
   *       <li>{@link AccessTokenRepository#revokeByJti(String)}
   *       <li>{@link RefreshTokenRepository#revokeByJtiInAttributes(String)}
   *     </ol>
   *     The combined result determines the HTTP status.
   * @see AccessTokenRepository#revokeByJti(String)
   * @see RefreshTokenRepository#revokeByJtiInAttributes(String)
   */
  @Post("/logout")
  public Publisher<MutableHttpResponse<?>> logout(Authentication authentication) {
    // jti from claims; username not used directly here but available if needed for auditing
    String jti = JwtUtils.requireJti(authentication);

    return Mono.zip(
            Mono.from(accessTokenRepository.revokeByJti(jti)), // rows updated (0/1)
            Mono.from(refreshTokenRepository.revokeByJtiInAttributes(jti)) // rows updated (>=0)
            )
        .map(
            tuple -> {
              long accessRows = tuple.getT1();
              long refreshRows = tuple.getT2();
              return (accessRows > 0 || refreshRows > 0)
                  ? HttpResponse.noContent()
                  : HttpResponse.status(HttpStatus.NOT_FOUND);
            });
  }
}
